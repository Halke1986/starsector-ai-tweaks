package com.genir.aitweaks.core.features

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.loading.MissileSpecAPI
import com.fs.starfarer.api.loading.WeaponGroupType.ALTERNATING
import com.fs.starfarer.api.loading.WeaponGroupType.LINKED
import com.fs.starfarer.campaign.CampaignEngine
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.features.shipai.CustomShipAI
import com.genir.aitweaks.core.features.shipai.autofire.BallisticTarget
import com.genir.aitweaks.core.features.shipai.autofire.SimulateMissile
import com.genir.aitweaks.core.features.shipai.autofire.defaultBallisticParams
import com.genir.aitweaks.core.features.shipai.autofire.intercept
import com.genir.aitweaks.core.utils.asteroidGrid
import com.genir.aitweaks.core.utils.extensions.*
import com.genir.aitweaks.core.utils.mousePosition
import com.genir.aitweaks.core.utils.shipGrid
import lunalib.lunaSettings.LunaSettings
import org.lazywizard.lazylib.CollisionUtils
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method

class AimAssist : BaseEveryFrameCombatPlugin() {
    var target: CombatEntityAPI? = null

    private var debugShipAI: CustomShipAI? = null
    private var isFiring: Boolean = false
    private var mouse: Vector2f = Vector2f()
    private var keybind: Int? = null
    private var enableAimAssist = false

    private val getAimTracker: MethodHandle
    private val setTargetOverride: MethodHandle

    private companion object {
        val statusKey = Object()
    }

    init {
        // Get Weapon interface class from return type of Ship.getSelectedWeapon method.
        val getSelectedWeapon: Method = Ship::class.java.methods.first { it.name == "getSelectedWeapon" }
        val weaponClass: Class<*> = getSelectedWeapon.returnType

        // Get AimTracker class from return type of Weapon.getAimTracker method.
        val getAimTracker: Method = weaponClass.methods.first { it.name == "getAimTracker" }
        val aimTrackerClass: Class<*> = getAimTracker.returnType

        val setTargetOverride: Method = aimTrackerClass.methods.first { it.returnType == Void.TYPE && it.hasParameters(Vector2f::class.java) }

        this.getAimTracker = MethodHandles.lookup().unreflect(getAimTracker)
        this.setTargetOverride = MethodHandles.lookup().unreflect(setTargetOverride)
    }

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        if (Global.getCurrentState() != GameState.COMBAT) return

        // Finish initialization when SS classes are ready.
        if (keybind == null) {
            keybind = LunaSettings.getInt("aitweaks", "aitweaks_aim_bot_keybind") ?: return

            val memory: MemoryAPI = CampaignEngine.getInstance().memoryWithoutUpdate
            enableAimAssist = memory.getBoolean("\$aitweaks_enableAimBot")
        }

        val ship: ShipAPI = Global.getCombatEngine().playerShip ?: return

        if (!ship.isAlive) return

        // Handle input.
        mouse = mousePosition()
        events?.forEach {
            when {
                it.isConsumed -> Unit

                it.isLMBDownEvent -> isFiring = true

                it.isLMBUpEvent -> isFiring = false

                // Toggle the aim bot and persist the setting to memory.
                it.isKeyDownEvent && it.eventValue == keybind -> {
                    enableAimAssist = !enableAimAssist
                    val memory: MemoryAPI = CampaignEngine.getInstance().memoryWithoutUpdate
                    memory.set("\$aitweaks_enableAimBot", enableAimAssist)
                }
            }
        }

        if (!enableAimAssist) return

        // debug
//        when (ship.isUnderManualControl) {
//            true -> {
//                val ai = debugShipAI ?: CustomShipAI(ship)
//                ai.advance(dt)
//                debugShipAI = ai
//            }
//
//            false -> debugShipAI = null
//        }

        // Display status icon.
        val icon = "graphics/icons/hullsys/interdictor_array.png"
        Global.getCombatEngine().maintainStatusForPlayerShip(statusKey, icon, "aim assist", "automatic target leading", false)

        // Select target.
        val target: CombatEntityAPI = selectTarget() ?: return
        this.target = target

        // Aim all non-autofire weapons.
        val manualWeapons: List<WeaponAPI> = ship.weaponGroupsCopy.filter { !it.isAutofiring }.flatMap { it.weaponsCopy }
        manualWeapons.filter { it.shouldOverride }.forEach { weapon ->
            aimWeapon(weapon, target)
        }

        // Aim and fire weapons in selected group.
        val selectedWeapons: List<WeaponAPI> = ship.selectedGroupAPI?.weaponsCopy ?: listOf()
        selectedWeapons.filter { it.shouldOverride }.forEach { weapon ->
            val intercept: Vector2f = aimWeapon(weapon, target)
            fireWeapon(weapon, intercept)
        }
    }

    private fun aimWeapon(weapon: WeaponAPI, target: CombatEntityAPI): Vector2f {
        val ballisticTarget = BallisticTarget(target.velocity, mouse, 0f)

        val intercept: Vector2f = when {
            weapon.type == WeaponType.MISSILE -> {
                SimulateMissile.missileIntercept(weapon, ballisticTarget)
            }

            else -> {
                intercept(weapon, ballisticTarget, defaultBallisticParams()) ?: target.location
            }
        }

        // Override the vanilla-computed weapon facing.
        setTargetOverride(getAimTracker(weapon), intercept)

        return intercept
    }

    private fun fireWeapon(weapon: WeaponAPI, intercept: Vector2f) {
        val interceptFacing = (intercept - weapon.location).facing - weapon.ship.facing
        val group: WeaponGroupAPI = weapon.group ?: return
        val shouldFire: Boolean = when {
            !isFiring -> false

            // Fire active alternating group weapon. Same behavior as vanilla.
            group.type == ALTERNATING && weapon == group.activeWeapon -> true

            // Fire linked weapons if target is in arc, regardless if weapon
            // is actually pointed at the target. Same behavior as vanilla.
            group.type == LINKED && weapon.isAngleInArc(interceptFacing) -> true

            else -> false
        }

        // Override the vanilla-computed should-fire decision.
        if (shouldFire) weapon.setForceFireOneFrame(true)
        else weapon.setForceNoFireOneFrame(true)
    }

    private fun selectTarget(): CombatEntityAPI? {
        val searchRadius = 500f

        val ships: Sequence<ShipAPI> = shipGrid().get<ShipAPI>(mouse, searchRadius).filter {
            when {
                !it.isValidTarget -> false
                it.owner == 0 -> false
                it.isFighter -> false

                else -> true
            }
        }

        closestTarget(ships)?.let { return it }

        val hulks = shipGrid().get<ShipAPI>(mouse, searchRadius).filter {
            when {
                it.isExpired -> false
                it.owner != 100 -> false
                it.isFighter -> false

                else -> true
            }
        }

        closestTarget(hulks)?.let { return it }

        val asteroids: Sequence<CombatAsteroidAPI> = asteroidGrid().get<CombatAsteroidAPI>(mouse, searchRadius).filter {
            when {
                it.isExpired -> false

                else -> true
            }
        }

        return closestTarget(asteroids)
    }

    private fun closestTarget(entities: Sequence<CombatEntityAPI>): CombatEntityAPI? {
        val targetEnvelope = 150f
        val closeEntities: Sequence<CombatEntityAPI> = entities.filter {
            (it.location - mouse).length <= (it.collisionRadius + targetEnvelope)
        }

        var closestEntity: CombatEntityAPI? = null
        var closestDist = Float.MAX_VALUE

        closeEntities.forEach {
            // If mouse is over ship bounds, consider it the target.
            if (CollisionUtils.isPointWithinBounds(mouse, it)) return it

            val dist = (it.location - mouse).lengthSquared
            if (dist < closestDist) {
                closestDist = dist
                closestEntity = it
            }
        }

        return closestEntity
    }

    private val WeaponAPI.shouldOverride: Boolean
        get() = when {
            // Beams do not need target leading.
            isBeam -> false

            type == WeaponType.MISSILE -> when {
                // Missiles in hardpoints are not aimable.
                slot.isHardpoint -> false

                // Unguided missiles are aimable.
                (spec.projectileSpec as? MissileSpecAPI)?.maneuverabilityDisplayName == "None" -> true

                // Guided missiles do not need target leading.
                else -> false
            }

            else -> true
        }
}
