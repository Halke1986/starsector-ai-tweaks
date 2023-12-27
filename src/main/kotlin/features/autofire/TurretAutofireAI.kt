package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.*
import com.genir.aitweaks.debugValue
import com.genir.aitweaks.utils.div
import com.genir.aitweaks.utils.extensions.hasBestTargetLeading
import com.genir.aitweaks.utils.extensions.isPD
import com.genir.aitweaks.utils.extensions.isValidTarget
import com.genir.aitweaks.utils.extensions.maneuverTarget
import org.lazywizard.lazylib.MathUtils.getDistanceSquared
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f

// TODO
// hardpoint
// profile

// dont switch targets mid burst
// fire on shields

// target selection
// paladin ff
// ir lance tracks fighters
// track ship target for player
// avoid station bulk
// STRIKE never targets fighters

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
        if (target == null) return false

        // only beams and PD weapons attack phased ships
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

        val offset = interceptOffset(weapon, target!!) / getAccuracy()
        if (offset.x.isNaN() || offset.y.isNaN()) {
            debugValue = offset
        }

        return target!!.location + interceptOffset(weapon, target!!) / getAccuracy()
    }

    override fun getTargetShip(): ShipAPI? = target as? ShipAPI
    override fun getWeapon(): WeaponAPI = weapon
    override fun getTargetMissile(): MissileAPI? = target as? MissileAPI

    private fun trackManeuverTarget(timeDelta: Float) {
        val newTarget = weapon.ship.maneuverTarget
        if (newTarget != null || maneuverTarget?.isValidTarget != true) maneuverTarget = newTarget
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
        val safePDWeapon =
            weapon.isBeam || weapon.isBurstBeam || (weapon.spec.damageType == DamageType.FRAGMENTATION && weapon.isPD)
        val pdFire =
            target is MissileAPI || (target is ShipAPI && (target.isFighter || target.isDrone || target.isPhased))
        val unsafePDFire = pdFire && !safePDWeapon

        // Search for blockers behind target only for unsafe PD fire.
        // Otherwise, assume fire will hit target or will be harmless to friendlies.
        val searchRange = if (unsafePDFire) weapon.range else hitRange
        val blocker = firstAlongLineOfFire(weapon, searchRange) ?: return true

        val blockerBehindTarget = getDistanceSquared(weapon.location, blocker.location) >= hitRange * hitRange
        val friendly = blocker.owner == weapon.ship.owner
        val enemy = !friendly && !blocker.isHulk

        return enemy || blockerBehindTarget && !friendly
    }
}
