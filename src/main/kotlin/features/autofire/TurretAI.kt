package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.*
import com.genir.aitweaks.debugValue
import com.genir.aitweaks.utils.extensions.maneuverTarget
import com.genir.aitweaks.utils.extensions.targetEntity
import com.genir.aitweaks.utils.*
import com.genir.aitweaks.utils.extensions.absoluteArcFacing
import com.genir.aitweaks.utils.extensions.isValidTarget
import org.lwjgl.util.vector.Vector2f

var count = 0

fun applyTurretAI(ship: ShipAPI) {
    ship.weaponGroupsCopy.forEach { group ->
        val plugins = group.aiPlugins
        for (i in plugins.indices) {
            val weapon = plugins[i].weapon
            if (weapon.slot.isTurret && weapon.type != WeaponAPI.WeaponType.MISSILE && weapon.ship.owner == 0) {
                plugins[i] = TurretAI(plugins[i])
                count++
                debugValue = count
            }
        }
    }
}

class TurretAI(private val basePlugin: AutofireAIPlugin) : AutofireAIPlugin {
    private val maneuverTargetTracker = ManeuverTargetTracker(basePlugin.weapon.ship)
    private var solution: FiringSolution? = null

    override fun advance(timeDelta: Float) {
        basePlugin.advance(timeDelta)
        maneuverTargetTracker.advance(timeDelta)

        // Prioritize maneuver target if it's within weapon arc.
        solution = calculateFiringSolution(weapon, maneuverTargetTracker.target)
        if (solution != null && arcsOverlap(weapon.absoluteArcFacing, weapon.arc, solution!!.facing, solution!!.arc)) {
//            debugValue = Pair(Vector2f(weapon.absoluteArcFacing, weapon.arc), Vector2f(solution!!.facing, solution!!.arc))
            return
        }


        if (solution != null) {
            debugValue = Pair(Vector2f(weapon.absoluteArcFacing, weapon.arc), Vector2f(solution!!.facing, solution!!.arc))
//            count++
//            debugValue = count
        }

        solution = calculateFiringSolution(weapon, basePlugin.targetEntity)
    }

    override fun getTarget(): Vector2f? = solution?.intercept

    override fun shouldFire(): Boolean = basePlugin.shouldFire() && solution != null && arcsOverlap(
        weapon.currAngle, 0f, solution!!.facing, solution!!.arc
    )

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