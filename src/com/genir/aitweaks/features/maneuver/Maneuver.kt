package com.genir.aitweaks.features.maneuver

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType.MISSILE
import com.genir.aitweaks.debug.Line
import com.genir.aitweaks.debug.debugPlugin
import com.genir.aitweaks.debug.debugVertices
import com.genir.aitweaks.debug.drawEngineLines
import com.genir.aitweaks.features.autofire.AutofireAI
import com.genir.aitweaks.utils.Controller
import com.genir.aitweaks.utils.div
import com.genir.aitweaks.utils.extensions.frontFacing
import com.genir.aitweaks.utils.extensions.isPD
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

var c1 = 0
var c2 = 1

class Maneuver(val ship: ShipAPI, val target: ShipAPI) {
    val isDirectControl: Boolean = true
    private val controller = Controller()

    var desiredHeading: Float = ship.facing
    var desiredFacing: Float = ship.facing

    private var dt: Float = 0f

    fun advance(dt: Float) {
        this.dt = dt

        val p = aimPoint()
        controller.facing(ship, p, dt)
        desiredFacing = (aimPoint() - ship.location).getFacing()

        drawEngineLines(ship)
//        drawWeaponLines(ship)
    }

    fun doManeuver() {
        val p = target.location - Vector2f(0f, weaponsRange())

        debugPlugin[1] = weaponsRange()

        debugVertices.add(Line(ship.location, p, Color.YELLOW))

        controller.heading(ship, p, target.velocity, dt)
        desiredHeading = (p - ship.location).getFacing()
    }

    private fun weaponsRange(): Float {
        val weapons = ship.allWeapons.filter {
            when {
                it.type == MISSILE -> false
                !it.frontFacing -> false
                it.isPD -> false
                it.isPermanentlyDisabled -> false
                else -> true
            }
        }

        return weapons.maxOfOrNull { -it.range - it.slot.location.x }?.let { -it } ?: 0f
    }

    private fun aimPoint(): Vector2f {
        val allAIs = ship.weaponGroupsCopy.flatMap { it.aiPlugins }.filterIsInstance<AutofireAI>()
        val harpoints = allAIs.filter {
            when {
                it.weapon.slot.isHardpoint -> false
                it.weapon.isBeam -> false
                !it.shouldFire() -> false
                it.intercept == null -> false
                else -> true
            }
        }

        return if (harpoints.isEmpty()) target.location
        else harpoints.fold(Vector2f()) { sum, ai -> sum + ai.intercept!! } / harpoints.size.toFloat()
    }
}
