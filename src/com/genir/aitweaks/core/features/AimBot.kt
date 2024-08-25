package com.genir.aitweaks.core.features

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.features.shipai.CustomShipAI
import com.genir.aitweaks.core.features.shipai.autofire.BallisticTarget
import com.genir.aitweaks.core.features.shipai.autofire.SimulateMissile
import com.genir.aitweaks.core.features.shipai.autofire.defaultBallisticParams
import com.genir.aitweaks.core.features.shipai.autofire.intercept
import com.genir.aitweaks.core.utils.asteroidGrid
import com.genir.aitweaks.core.utils.extensions.*
import com.genir.aitweaks.core.utils.mousePosition
import com.genir.aitweaks.core.utils.shipGrid
import org.lazywizard.lazylib.CollisionUtils
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method

class AimBot : BaseEveryFrameCombatPlugin() {
    var target: CombatEntityAPI? = null

    private var isFiring: Boolean = false
    private var mouse: Vector2f = Vector2f()
    private var shipAI: CustomShipAI? = null

    private var getAimTracker: MethodHandle? = null
    private var setTargetVectorOverride: MethodHandle? = null

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        if (Global.getCurrentState() != GameState.COMBAT) return

        val ship: ShipAPI = Global.getCombatEngine().playerShip ?: return

        // Handle input.
        mouse = mousePosition()
        events?.forEach {
            when {
                it.isConsumed -> Unit

                it.isLMBDownEvent -> isFiring = true

                it.isLMBUpEvent -> isFiring = false
            }
        }

        // TODO remove
        when (ship.isUnderManualControl) {
            true -> {
                val ai = shipAI ?: CustomShipAI(ship)
                ai.advance(dt)
                shipAI = ai
            }

            false -> {
                shipAI = null
            }
        }

        val target: CombatEntityAPI = selectTarget() ?: return
        this.target = target

        // Aim all non-autofire weapons.
        val manualWeapons: List<WeaponAPI> = ship.weaponGroupsCopy.filter { !it.isAutofiring }.flatMap { it.weaponsCopy }
        manualWeapons.forEach { weapon ->
            aimWeapon(weapon, target)
        }

        // Aim and fire weapons in selected group.
        val selectedWeapons: List<WeaponAPI> = ship.selectedGroupAPI?.weaponsCopy ?: listOf()
        selectedWeapons.forEach { weapon ->
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

        if (getAimTracker == null) initMethodHandlers(weapon)

        setTargetVectorOverride!!(getAimTracker!!(weapon), intercept)

        return intercept
    }

    private fun fireWeapon(weapon: WeaponAPI, intercept: Vector2f) {
        val interceptFacing = (intercept - weapon.location).facing - weapon.ship.facing
        val shouldFire: Boolean = when {
            !isFiring -> false

            // Fire if target is in arc, regardless if weapon is actually
            // pointed at the target. Same behavior as vanilla.
            else -> weapon.isAngleInArc(interceptFacing)
        }

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

    private fun initMethodHandlers(weapon: WeaponAPI) {
        val weaponMethods: Array<Method> = weapon::class.java.methods

        val getAimTracker = weaponMethods.first { it.name == "getAimTracker" }
        this.getAimTracker = MethodHandles.lookup().unreflect(getAimTracker)

        val aimTrackerClass: Class<*> = getAimTracker.returnType
        val aimTrackerMethods: Array<Method> = aimTrackerClass.methods

        val params = arrayOf(Vector2f::class.java)
        val setTargetVectorOverride = aimTrackerMethods.first { it.returnType == Void.TYPE && it.parameterTypes.contentEquals(params) }
        this.setTargetVectorOverride = MethodHandles.lookup().unreflect(setTargetVectorOverride)
    }
}