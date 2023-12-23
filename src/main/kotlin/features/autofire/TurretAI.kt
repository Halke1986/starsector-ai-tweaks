package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.debugValue
import com.genir.aitweaks.utils.extensions.isValidTarget
import com.genir.aitweaks.utils.extensions.maneuverTarget
import com.genir.aitweaks.utils.extensions.targetEntity
import org.lwjgl.util.vector.Vector2f

fun applyTurretAI(ship: ShipAPI) {
    ship.weaponGroupsCopy.forEach { group ->
        val plugins = group.aiPlugins
        for (i in plugins.indices) {
            val weapon = plugins[i].weapon
            if (weapon.slot.isTurret && weapon.type != WeaponAPI.WeaponType.MISSILE && weapon.ship.owner == 0) {
                plugins[i] = TurretAI(plugins[i])
            }
        }
    }
}

// TODO
// accuracy
// fire on shields
// ff
// target selection
// paladin ff
// ir lance tracks fighters
// small pd weapons by hullmod

class TurretAI(private val basePlugin: AutofireAIPlugin) : AutofireAIPlugin {
    private val maneuverTargetTracker = ManeuverTargetTracker(basePlugin.weapon.ship)
    private var solution: FiringSolution? = null

    override fun advance(timeDelta: Float) {
        basePlugin.advance(timeDelta)
        maneuverTargetTracker.advance(timeDelta)

        solution =
            if (weapon.hasAIHint(WeaponAPI.AIHints.PD)) basePlugin.targetEntity?.let { FiringSolution(weapon, it) }
            else selectTarget(weapon, solution?.target, maneuverTargetTracker.target)
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

    override fun getTarget(): Vector2f? = solution?.intercept
    override fun forceOff() = basePlugin.forceOff()
    override fun getTargetShip(): ShipAPI? = solution?.target as? ShipAPI
    override fun getWeapon(): WeaponAPI = basePlugin.weapon
    override fun getTargetMissile(): MissileAPI? = solution?.target as? MissileAPI
}


class ManeuverTargetTracker(private val ship: ShipAPI) {
    var target: ShipAPI? = null

    fun advance(p0: Float) {
        when (val newTarget = ship.maneuverTarget) {
            target -> return
            null -> if (!target.isValidTarget) target = null
            else -> target = newTarget
        }
    }
}