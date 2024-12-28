package com.genir.aitweaks.core.playerassist

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponGroupAPI
import com.fs.starfarer.api.loading.WeaponGroupType
import com.genir.aitweaks.core.Obfuscated
import com.genir.aitweaks.core.debug.Debug
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.BaseShipAIPlugin
import com.genir.aitweaks.core.shipai.BasicEngineController
import com.genir.aitweaks.core.shipai.WeaponGroup
import com.genir.aitweaks.core.shipai.autofire.*
import com.genir.aitweaks.core.state.State
import com.genir.aitweaks.core.state.VanillaKeymap
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.Rotation.Companion.rotated
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class AimAssistAI(private val manager: AimAssistManager) : BaseShipAIPlugin() {
    private var prevPlayerShip: ShipAPI? = null
    private var engineController: BasicEngineController? = null

    private var currentTarget: CombatEntityAPI? = null
    private var currentAlternatingWeapon: WeaponAPI? = null

    override fun advance(dt: Float) {
        val ship = Global.getCombatEngine().playerShip

        // Decide if aim assist should run.
        when {
            ship == null -> return
            !ship.isAlive -> return
            !ship.isUnderManualControl -> return

            // Is aim assist enabled.
            !manager.enableAimAssist -> return
        }

        // Estimate player target selection.
        val target: CombatEntityAPI? = selectTarget()
        target?.let { Debug.drawCircle(it.location, it.collisionRadius / 2, Color.YELLOW) }

        if (manager.strafeModeOn && State.state.config.aimAssistRotateShip) aimShip(dt, ship, target)

        // Override weapon behavior if there is a target.
        // Otherwise, let vanilla control the weapons.
        if (target != null) aimWeapons(ship, target)

        this.currentTarget = target
    }

    private fun aimShip(dt: Float, ship: ShipAPI, target: CombatEntityAPI?) {
        // Update engine controller if player ship changed.
        if (ship != prevPlayerShip) {
            prevPlayerShip = ship
            engineController = BasicEngineController(ship)
        }

        controlShipHeading(dt, ship)
        controlShipFacing(dt, ship, target)
    }

    private fun controlShipHeading(dt: Float, ship: ShipAPI) {
        // Do not attempt to override the ship movement if any of the movement commands is blocked.
        val commands = arrayOf(VanillaShipCommand.STRAFE_LEFT, VanillaShipCommand.STRAFE_RIGHT, VanillaShipCommand.ACCELERATE, VanillaShipCommand.ACCELERATE_BACKWARDS)
        val blockedCommands = (ship as Obfuscated.Ship).blockedCommands
        if (commands.any { blockedCommands.contains(it.obfuscated) }) return

        // Remove vanilla move commands.
        clearVanillaCommands(ship, *commands)

        // Compensate ship movement for the fact the ship
        // is not necessary facing the target directly.
        val r = Rotation((mousePosition() - ship.location).facing)
        val front = Vector2f(1e4f, 0f).rotated(r)
        val back = Vector2f(-1e4f, 0f).rotated(r)
        val left = Vector2f(0f, 1e4f).rotated(r)
        val right = Vector2f(0f, -1e4f).rotated(r)

        var direction = Vector2f()
        if (VanillaKeymap.isKeyDown(VanillaKeymap.PlayerAction.SHIP_ACCELERATE)) direction += front
        if (VanillaKeymap.isKeyDown(VanillaKeymap.PlayerAction.SHIP_ACCELERATE_BACKWARDS)) direction += back
        if (VanillaKeymap.isKeyDown(VanillaKeymap.PlayerAction.SHIP_TURN_LEFT) || VanillaKeymap.isKeyDown(VanillaKeymap.PlayerAction.SHIP_STRAFE_LEFT_NOTURN)) direction += left
        if (VanillaKeymap.isKeyDown(VanillaKeymap.PlayerAction.SHIP_TURN_RIGHT) || VanillaKeymap.isKeyDown(VanillaKeymap.PlayerAction.SHIP_STRAFE_RIGHT_NOTURN)) direction += right

        if (direction.isNonZero) {
            val heading = ship.location + direction
            engineController!!.heading(dt, heading, Vector2f())
        }
    }

    private fun controlShipFacing(dt: Float, ship: ShipAPI, target: CombatEntityAPI?) {
        // Do not attempt to override the ship movement if any of the movement commands is blocked.
        val commands = arrayOf(VanillaShipCommand.TURN_LEFT, VanillaShipCommand.TURN_RIGHT)
        val blockedCommands = (ship as Obfuscated.Ship).blockedCommands
        if (commands.any { blockedCommands.contains(it.obfuscated) }) return

        // Remove vanilla move commands.
        clearVanillaCommands(ship, *commands)

        // Continue aiming with an alternating weapon until it finishes its firing sequence.
        val holdCurrent = currentAlternatingWeapon?.isInFiringSequence == true
        if (!holdCurrent) currentAlternatingWeapon = null

        // Select weapons to aim.
        val selectedGroup = ship.selectedGroupAPI
        val activeWeapon = selectedGroup?.activeWeapon
        val weapons: List<WeaponAPI> = when {
            holdCurrent -> listOf(currentAlternatingWeapon!!)
            selectedGroup == null -> listOf()
            selectedGroup.type == WeaponGroupType.LINKED -> selectedGroup.weaponsCopy.filter { it.shouldAim }
            selectedGroup.type == WeaponGroupType.ALTERNATING && activeWeapon?.shouldAim == true -> {
                currentAlternatingWeapon = activeWeapon
                listOf(activeWeapon)
            }

            else -> listOf()
        }

        // Aim the weapons by rotating the ship.
        val weaponGroup = WeaponGroup(ship, weapons.filter { it.shouldAim })
        if (target != null) {
            // Rotate ship to face the target intercept with the selected weapon group.
            val expectedFacing = weaponGroup.attackFacing(BallisticTarget(mousePosition(), target.velocity, 0f))
            val facingChange = angularVelocity(target.location - ship.location, target.velocity - ship.velocity)
            engineController!!.facing(dt, expectedFacing, facingChange)
        } else {
            // If there's no target, rotate the ship to face the mouse position with the selected weapon group.
            val expectedFacing = (mousePosition() - ship.location).facing - weaponGroup.defaultFacing
            engineController!!.facing(dt, expectedFacing, 0f)
        }
    }

    private fun aimWeapons(ship: ShipAPI, target: CombatEntityAPI) {
        val ballisticTarget = BallisticTarget(mousePosition(), target.velocity, target.collisionRadius)
        val selectedWeapons: Set<WeaponAPI> = ship.selectedWeapons
        val aimableWeapons: Set<WeaponAPI> = ship.nonAutofireWeapons + selectedWeapons
        val params = defaultBallisticParams

        aimableWeapons.forEach { weapon ->
            // Override aim for all non-autofire weapons except missile missile hardpoints.
            if (weapon.slot.isTurret || !weapon.isUnguidedMissile) {
                aimWeapon(weapon, ballisticTarget, params)
            }

            // Override fire command for manually operated turrets.
            // Vanilla hardpoints obey player fire command regardless of arc,
            // so there's no need to override their fire command.
            if (weapon.slot.isTurret && selectedWeapons.contains(weapon)) {
                fireWeapon(weapon, ballisticTarget, params)
            }
        }
    }

    private fun aimWeapon(weapon: WeaponAPI, ballisticTarget: BallisticTarget, params: BallisticParams) {
        val intercept: Vector2f = intercept(weapon, ballisticTarget, params)

        // Override vanilla-computed weapon facing.
        val aimTracker: Obfuscated.AimTracker = (weapon as Obfuscated.Weapon).aimTracker
        aimTracker.aimTracker_setTargetOverride(intercept + weapon.location)
    }

    private fun fireWeapon(weapon: WeaponAPI, ballisticTarget: BallisticTarget, params: BallisticParams) {
        val group: WeaponGroupAPI = weapon.group ?: return

        val shouldFire: Boolean = when {
            !VanillaKeymap.isKeyDown(VanillaKeymap.PlayerAction.SHIP_FIRE) -> false

            // Fire active alternating group weapon. Same behavior as vanilla.
            group.type == WeaponGroupType.ALTERNATING && weapon == group.activeWeapon -> true

            // Fire linked weapons if it's possible to hit the target.
            group.type == WeaponGroupType.LINKED && interceptArc(weapon, ballisticTarget, params).contains(weapon.currAngle) -> true

            else -> false
        }

        // Override the vanilla-computed should-fire decision.
        if (shouldFire) weapon.setForceFireOneFrame(true)
        else weapon.setForceNoFireOneFrame(true)
    }

    private fun selectTarget(): CombatEntityAPI? {
        val searchRadius = 500f

        // If mouse is over ship bounds, consider it the target.
        Grid.ships(mousePosition(), searchRadius).filter {
            when {
                !it.isValidTarget -> false
                it.owner == 0 -> false
                it.isFighter -> false

                else -> State.state.bounds.isPointWithin(mousePosition(), it)
            }
        }.firstOrNull()?.let { return it }

        val ships = Grid.ships(mousePosition(), searchRadius).filter {
            when {
                !it.isValidTarget -> false
                it.owner == 0 -> false

                else -> true
            }
        }

        closestTarget(ships)?.let { return it }

        val hulks = Grid.ships(mousePosition(), searchRadius).filter {
            when {
                it.isExpired -> false
                it.owner != 100 -> false
                it.isFighter -> false

                else -> true
            }
        }

        closestTarget(hulks)?.let { return it }

        val asteroids = Grid.asteroids(mousePosition(), searchRadius).filter {
            when {
                it.isExpired -> false

                else -> true
            }
        }

        return closestTarget(asteroids)
    }

    private fun closestTarget(entities: Sequence<CombatEntityAPI>): CombatEntityAPI? {
        val distances = entities.map { entity ->
            val closestPoint = if (!entity.isShip) entity.location
            else State.state.bounds.closestPoint(mousePosition(), entity as ShipAPI)

            // Apply target switching hysteresis.
            val currentTargetBonus = if (entity == currentTarget) 2 else 1
            val dist = (closestPoint - mousePosition()).length / currentTargetBonus

            Pair(entity, dist)
        }

        val closest = distances.minWithOrNull(compareBy { it.second }) ?: return null
        val targetEnvelope = 200f * Global.getCombatEngine().viewport.viewMult

        return if (closest.second > targetEnvelope) null
        else closest.first
    }

    private val WeaponAPI.shouldAim: Boolean
        get() = type != WeaponAPI.WeaponType.MISSILE || isUnguidedMissile

    private val ShipAPI.nonAutofireWeapons: Set<WeaponAPI>
        get() = weaponGroupsCopy.filter { !it.isAutofiring }.flatMap { it.weaponsCopy }.filter { it.shouldAim }.toSet()

    private val ShipAPI.selectedWeapons: Set<WeaponAPI>
        get() = selectedGroupAPI?.weaponsCopy?.filter { it.shouldAim }?.toSet() ?: setOf()
}