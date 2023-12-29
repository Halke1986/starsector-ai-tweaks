package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.DamageType.FRAGMENTATION
import com.genir.aitweaks.debugValue
import com.genir.aitweaks.utils.div
import com.genir.aitweaks.utils.extensions.aimLocation
import com.genir.aitweaks.utils.extensions.hasBestTargetLeading
import com.genir.aitweaks.utils.extensions.isPD
import com.genir.aitweaks.utils.extensions.maneuverTarget
import com.genir.aitweaks.utils.rotateAroundPivot
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.MathUtils.getDistance
import org.lazywizard.lazylib.MathUtils.getDistanceSquared
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs

// TODO
// fire on shields
// don't attack fighters over friendlies

// don't switch targets mid burst
// target selection
// paladin ff
// ir lance tracks fighters
// track ship target for player
// avoid station bulk
// take high-tech station into account
// STRIKE never targets fighters

// profile again
// custom bounds check

class TurretAutofireAI(private val weapon: WeaponAPI) : AutofireAIPlugin {
    private var target: CombatEntityAPI? = null
    private var maneuverTarget: ShipAPI? = null
    private var prevTarget: CombatEntityAPI? = null

    private var attackTime: Float = 0f
    private var idleTime: Float = 0f

    override fun advance(timeDelta: Float) {
        trackManeuverTarget(timeDelta)
        trackTimes(timeDelta)

        target = selectTarget(weapon, target, maneuverTarget)
    }

    override fun shouldFire(): Boolean {
        if (target == null || Global.getCurrentState() != GameState.COMBAT) return false

        // Only beams and PD weapons attack phased ships.
        if ((target as? ShipAPI)?.isPhased == true && !weapon.spec.isBeam && !weapon.isPD) return false

        // Fire only when the selected target is in range.
        val range = hitRange(weapon, target!!)
        if (range.isNaN() || range > weapon.range) return false

        // Avoid firing on friendlies or junk.
        return avoidsFriendlyFire(target!!, range)
    }

    override fun forceOff() {
        target = null
    }

    override fun getTarget(): Vector2f? {
        if (target == null) return null

        val intercept = target!!.aimLocation + interceptOffset(weapon, target!!) / getAccuracy()
        return if (weapon.slot.isTurret) intercept
        else aimHardpoint(intercept)
    }

    override fun getTargetShip(): ShipAPI? = target as? ShipAPI
    override fun getWeapon(): WeaponAPI = weapon
    override fun getTargetMissile(): MissileAPI? = target as? MissileAPI

    private fun trackManeuverTarget(timeDelta: Float) {
        val newTarget = weapon.ship.maneuverTarget
        if (newTarget != null || maneuverTarget?.isAlive != true) maneuverTarget = newTarget
    }

    private fun trackTimes(timeDelta: Float) {
        val currentTarget = target
        if (currentTarget != null && prevTarget != currentTarget) {
            prevTarget = currentTarget
            attackTime = 0f
        }

        if (weapon.isFiring) {
            attackTime += timeDelta
            idleTime = 0f
        } else idleTime += timeDelta

        if (idleTime >= 3f) attackTime = 0f
    }

    /**
     * getAccuracy returns current weapon accuracy.
     * The value is a number in range [1.0;2.0]. 1.0 is perfect accuracy, 2.0f is the worst accuracy.
     * For worst accuracy, the weapon should aim exactly in the middle point between the target actual
     * position and calculated intercept position.
     */
    private fun getAccuracy(): Float {
        if (weapon.hasBestTargetLeading) return 1f

        val accBase = weapon.ship.aimAccuracy
        val accBonus = weapon.spec.autofireAccBonus
        return (accBase - (accBonus + attackTime / 15f)).coerceAtLeast(1f)
    }

    private fun avoidsFriendlyFire(target: CombatEntityAPI, hitRange: Float): Boolean {
        val missile = target is MissileAPI
        val fighter = (target as? ShipAPI)?.isFighter == true
        val phased = (target as? ShipAPI)?.isPhased == true
        val beam = weapon.isBeam || weapon.isBurstBeam
        val fragPD = weapon.spec.damageType == FRAGMENTATION && weapon.isPD
        val firePassesTarget = (((missile || fighter) && !beam) || phased) && !fragPD

        // Search for blockers behind target only for attacks that are
        // predicted to pass through the target and be dangerous to friendlies.
        val searchRange = if (firePassesTarget) weapon.range else hitRange
        val blocker = firstAlongLineOfFire(weapon, searchRange) ?: return true

        val r2 = hitRange * hitRange
        val blockerBehindTarget = getDistanceSquared(weapon.location, blocker.location) >= r2
        val friendly = blocker.owner == weapon.ship.owner
        val enemy = !friendly && !blocker.isHulk

        return enemy || blockerBehindTarget && !friendly
    }

    /** predictive aiming for hardpoints */
    private fun aimHardpoint(intercept: Vector2f): Vector2f {
        val tgtLocation = target!!.aimLocation - weapon.ship.location
        val tgtFacing = VectorUtils.getFacing(tgtLocation)
        val angleToTarget = MathUtils.getShortestRotation(tgtFacing, weapon.ship.facing)

        // Ship is already facing the target. Return the
        // original target location to not overcompensate.
        if (abs(angleToTarget) < weapon.arc / 2f) {
            return intercept
        }

        return rotateAroundPivot(intercept, weapon.ship.location, angleToTarget)
    }
}
