package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.utils.extensions.isValidTarget
import com.genir.aitweaks.utils.extensions.maneuverTarget
import org.lwjgl.util.vector.Vector2f

// TODO
// accuracy
// fire on shields
// ff
// target selection
// paladin ff
// ir lance tracks fighters
// small pd weapons by hullmod
// track ship target for player
// ignore flares

class TurretAutofireAI(private val weapon: WeaponAPI) : AutofireAIPlugin {
    private val maneuverTargetTracker = ManeuverTargetTracker(weapon.ship)
    private var solution: FiringSolution? = null


    override fun advance(timeDelta: Float) {
        maneuverTargetTracker.advance(timeDelta)

        solution = selectTarget(weapon, solution?.target, maneuverTargetTracker.target)
    }

    override fun shouldFire(): Boolean {
        if (solution == null) return false

        // Fire only when the selected target is in sights.
        val hitSolver = HitSolver(solution!!.weapon)
        val range = hitSolver.hitRange(solution!!.target) ?: return false

        val first = firstAlongLineOfFire(hitSolver, range)

        if (first != null && (!first.isAlive || (first.isAlive && first.owner == weapon.ship.owner))) {
            if (weapon.spec.weaponId == "hephag" && first != solution!!.target) {
//                debugValue = if (first.isHulk) {
//                    "junk"
//                } else {
//                    first
//                }
            }

            return false
        }

        return true
    }

    override fun forceOff() {
        solution = null
    }

    override fun getTarget(): Vector2f? = solution?.intercept
    override fun getTargetShip(): ShipAPI? = solution?.target as? ShipAPI
    override fun getWeapon(): WeaponAPI = weapon
    override fun getTargetMissile(): MissileAPI? = solution?.target as? MissileAPI
}

class ManeuverTargetTracker(private val ship: ShipAPI) {
    var target: ShipAPI? = null

    fun advance(timeDelta: Float) {
        when (val newTarget = ship.maneuverTarget) {
            target -> return
            null -> if (target?.isValidTarget != true) target = null
            else -> target = newTarget
        }
    }
}