package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.debugValue
import com.genir.aitweaks.utils.div
import com.genir.aitweaks.utils.extensions.hasBestTargetLeading
import com.genir.aitweaks.utils.extensions.isValidTarget
import com.genir.aitweaks.utils.extensions.maneuverTarget
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f

// TODO
// ff when attacking fighters
// ignore small hulks

// hardpoint
// dont switch targets mid burst
// fire on shields

// target selection
// paladin ff
// ir lance tracks fighters
// track ship target for player
// avoid station bulk

// profile

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

        // only beams attack phased ships
        if ((target as? ShipAPI)?.isPhased == true && !weapon.spec.isBeam) return false

        // Fire only when the selected target is in range.
        val range = hitRange(weapon, target!!)
        if (range.isNaN() || range > weapon.range) return false

        // Avoid firing on friendlies or junk.
        val blocker = firstAlongLineOfFire(weapon, range)
        return (blocker == null || blocker.owner xor weapon.ship.owner == 1)
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
}
