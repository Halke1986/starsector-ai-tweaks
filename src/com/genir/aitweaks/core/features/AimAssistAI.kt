package com.genir.aitweaks.core.features

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.loading.WeaponGroupType
import com.fs.starfarer.campaign.CampaignEngine
import com.genir.aitweaks.core.Obfuscated
import com.genir.aitweaks.core.debug.Debug
import com.genir.aitweaks.core.features.shipai.BaseShipAIPlugin
import com.genir.aitweaks.core.features.shipai.BasicEngineController
import com.genir.aitweaks.core.features.shipai.WeaponGroup
import com.genir.aitweaks.core.features.shipai.autofire.BallisticTarget
import com.genir.aitweaks.core.features.shipai.autofire.defaultBallisticParams
import com.genir.aitweaks.core.features.shipai.autofire.intercept
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.Rotation.Companion.rotated
import com.genir.aitweaks.core.utils.VanillaKeymap.Action.*
import com.genir.aitweaks.core.utils.VanillaShipCommand.*
import com.genir.aitweaks.core.utils.extensions.*
import org.lazywizard.lazylib.CollisionUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

// TODO setting to disable hardpoint aiming (maybe)

class AimAssistAI : BaseShipAIPlugin() {
    private val keymap = VanillaKeymap()

    private var prevPlayerShip: ShipAPI? = null
    private var engineController: BasicEngineController? = null

    private var isFiring: Boolean = false
    private var isStrafeMode: Boolean = false

    override fun advance(dt: Float) {
        // Decide if aim assist should run.
        val engine = Global.getCombatEngine()
        val ship = engine.playerShip
        when {
            engine.isPaused -> return

            ship == null -> return
            !ship.isAlive -> return
            !ship.isUnderManualControl -> return

            // Is aim assist enabled.
            !CampaignEngine.getInstance().memoryWithoutUpdate.getBoolean("\$aitweaks_enableAimBot") -> return
        }

        // Read player input.
        isFiring = keymap.isKeyDown(SHIP_FIRE)
        isStrafeMode = keymap.isKeyDown(SHIP_STRAFE_KEY)

        // Estimate player target selection.
        val target: CombatEntityAPI? = selectTarget()
        target?.let { Debug.drawCircle(it.location, it.collisionRadius / 2, Color.YELLOW) }

        if (isStrafeMode) aimShip(dt, ship, target)

        // Override weapon behavior if there is a target.
        // Otherwise, let vanilla control the weapons.
        if (target != null) aimWeapons(ship, target)
    }

    private fun aimShip(dt: Float, ship: ShipAPI, target: CombatEntityAPI?) {
        // Update engine controller if player ship changed.
        if (ship != prevPlayerShip) {
            prevPlayerShip = ship
            engineController = BasicEngineController(ship)
        }

        // Remove vanilla move commands.
        clearVanillaCommands(ship, TURN_LEFT, TURN_RIGHT, STRAFE_LEFT, STRAFE_RIGHT, ACCELERATE, ACCELERATE_BACKWARDS)

        // Compensate ship movement for the fact the ship
        // is not necessary facing the target directly.
        val r = Rotation((mousePosition() - ship.location).facing)
        val front = Vector2f(1e4f, 0f).rotated(r)
        val back = Vector2f(-1e4f, 0f).rotated(r)
        val left = Vector2f(0f, 1e4f).rotated(r)
        val right = Vector2f(0f, -1e4f).rotated(r)

        var expectedHeading = ship.location.copy
        if (keymap.isKeyDown(SHIP_ACCELERATE)) expectedHeading += front
        if (keymap.isKeyDown(SHIP_ACCELERATE_BACKWARDS)) expectedHeading += back
        if (keymap.isKeyDown(SHIP_TURN_LEFT) || keymap.isKeyDown(SHIP_STRAFE_LEFT_NOTURN)) expectedHeading += left
        if (keymap.isKeyDown(SHIP_TURN_RIGHT) || keymap.isKeyDown(SHIP_STRAFE_RIGHT_NOTURN)) expectedHeading += right

        engineController!!.heading(dt, expectedHeading, Vector2f())

        // Control the ship rotation.
        val weaponGroup = WeaponGroup(ship, ship.selectedWeapons.toList())
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
        val ballisticTarget = BallisticTarget(mousePosition(), target.velocity, 0f)
        val selectedWeapons: Set<WeaponAPI> = ship.selectedWeapons
        val aimableWeapons: Set<WeaponAPI> = ship.nonAutofireWeapons + selectedWeapons

        aimableWeapons.forEach { weapon ->
            when {
                // Vanilla hardpoints obey player fire command regardless of arc,
                // so there's no need to override their fire command.
                weapon.slot.isHardpoint -> {
                    // It's not possible to aim missile hardpoints.
                    if (!weapon.isUnguidedMissile) {
                        aimWeapon(weapon, ballisticTarget)
                    }
                }

                // Override aim for all non-autofire turrets.
                weapon.slot.isTurret -> {
                    val intercept: Vector2f = aimWeapon(weapon, ballisticTarget)
                    // Override fire command for manually operated turrets.
                    if (selectedWeapons.contains(weapon)) {
                        fireWeapon(weapon, intercept)
                    }
                }
            }
        }
    }

    private fun aimWeapon(weapon: WeaponAPI, ballisticTarget: BallisticTarget): Vector2f {
        val intercept: Vector2f = intercept(weapon, ballisticTarget, defaultBallisticParams)

        // Override vanilla-computed weapon facing.
        val aimTracker: Obfuscated.AimTracker = (weapon as Obfuscated.Weapon).aimTracker
        aimTracker.aimTracker_setTargetOverride(intercept + weapon.location)

        return intercept
    }

    private fun fireWeapon(weapon: WeaponAPI, intercept: Vector2f) {
        val interceptFacing = intercept.facing - weapon.ship.facing
        val group: WeaponGroupAPI = weapon.group ?: return
        val isFiring = keymap.isKeyDown(VanillaKeymap.Action.SHIP_FIRE)

        val shouldFire: Boolean = when {
            !isFiring -> false

            // Fire active alternating group weapon. Same behavior as vanilla.
            group.type == WeaponGroupType.ALTERNATING && weapon == group.activeWeapon -> true

            // Fire linked weapons if target is in arc, regardless if weapon
            // is actually pointed at the target. Same behavior as vanilla.
            group.type == WeaponGroupType.LINKED && weapon.isAngleInArc(interceptFacing) -> true

            else -> false
        }

        // Override the vanilla-computed should-fire decision.
        if (shouldFire) weapon.setForceFireOneFrame(true)
        else weapon.setForceNoFireOneFrame(true)
    }

    private fun selectTarget(): CombatEntityAPI? {
        val searchRadius = 500f

        // If mouse is over ship bounds, consider it the target.
        shipGrid().get<ShipAPI>(mousePosition(), searchRadius) {
            when {
                !it.isValidTarget -> false
                it.owner == 0 -> false
                it.isFighter -> false

                else -> CollisionUtils.isPointWithinBounds(mousePosition(), it)
            }
        }.firstOrNull()?.let { return it }

        val ships: Sequence<ShipAPI> = shipGrid().get(mousePosition(), searchRadius) {
            when {
                !it.isValidTarget -> false
                it.owner == 0 -> false

                else -> true
            }
        }

        closestTarget(ships)?.let { return it }

        val hulks: Sequence<ShipAPI> = shipGrid().get(mousePosition(), searchRadius) {
            when {
                it.isExpired -> false
                it.owner != 100 -> false
                it.isFighter -> false

                else -> true
            }
        }

        closestTarget(hulks)?.let { return it }

        val asteroids: Sequence<CombatAsteroidAPI> = asteroidGrid().get(mousePosition(), searchRadius) {
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
            else Bounds.closestPoint(mousePosition(), entity as ShipAPI)
            Pair(entity, (closestPoint - mousePosition()).length)
        }

        val closest = distances.minWithOrNull(compareBy { it.second }) ?: return null
        val targetEnvelope = 170f * Global.getCombatEngine().viewport.viewMult

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
