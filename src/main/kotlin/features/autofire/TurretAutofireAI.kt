package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.utils.div
import com.genir.aitweaks.utils.extensions.hasBestTargetLeading
import com.genir.aitweaks.utils.extensions.isValidTarget
import com.genir.aitweaks.utils.extensions.maneuverTarget
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f

// TODO
// hardpoint
// dont switch targets mid burst
// fire on shields

// target selection
// paladin ff
// ir lance tracks fighters
// track ship target for player
// ff when attacking fighters

// profile

class TurretAutofireAI(private val weapon: WeaponAPI) : AutofireAIPlugin {
    private var solution: FiringSolution? = null
    private var maneuverTarget: ShipAPI? = null
    private var prevTarget: CombatEntityAPI? = null

    private var attackTime: Float = 0f
    private var idleTime: Float = 0f

    override fun advance(timeDelta: Float) {
        trackManeuverTarget(timeDelta)
        trackTimes(timeDelta)

        solution = selectTarget(weapon, solution?.target, maneuverTarget)
    }

    override fun shouldFire(): Boolean {
        if (solution == null) return false

        // only beams attack phased ships
        if ((solution!!.target as? ShipAPI)?.isPhased == true && !weapon.spec.isBeam) return false

        // Fire only when the selected target is in sights.
        val hitSolver = HitSolver(solution!!.weapon)
        val range = hitSolver.hitRange(solution!!.target) ?: return false

        // Avoid firing on friendlies or junk.
        val blocker = firstAlongLineOfFire(hitSolver, range)
        return (blocker == null || blocker.owner xor weapon.ship.owner == 1)
    }

    override fun forceOff() {
        solution = null
    }

    override fun getTarget(): Vector2f? {
        if (solution?.valid != true) return null

        val offset = solution!!.intercept - solution!!.target.location
        return solution!!.target.location + offset / getAccuracy()
    }

    override fun getTargetShip(): ShipAPI? = solution?.target as? ShipAPI
    override fun getWeapon(): WeaponAPI = weapon
    override fun getTargetMissile(): MissileAPI? = solution?.target as? MissileAPI

    private fun trackManeuverTarget(timeDelta: Float) {
        val newTarget = weapon.ship.maneuverTarget
        if (newTarget != null || maneuverTarget?.isValidTarget != true) maneuverTarget = newTarget
    }

    private fun trackTimes(timeDelta: Float) {
        val currentTarget = solution?.target
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
