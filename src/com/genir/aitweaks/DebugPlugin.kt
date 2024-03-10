package com.genir.aitweaks

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipCommand.TURN_LEFT
import com.fs.starfarer.api.combat.ShipCommand.TURN_RIGHT
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.utils.times
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.isZeroVector
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ui.LazyFont
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

var debugPlugin: DebugPlugin = DebugPlugin()

// DebugPlugin is used to render debug information during combat.
class DebugPlugin : BaseEveryFrameCombatPlugin() {
    private var font: LazyFont? = null
    private var logs: MutableMap<String, LazyFont.DrawableString> = TreeMap()

    operator fun set(index: Any, value: Any?) {
        if (font == null) return

        if (value == null) logs.remove("$index")
        else logs["$index"] = font!!.createText("${if (index is String) index else ""} $value", baseColor = Color.ORANGE)
    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        if (font == null) {
            font = LazyFont.loadFont("graphics/fonts/insignia15LTaa.fnt")
            debugPlugin = this
        }

        debug(amount)
//        speedupAsteroids()
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        super.renderInUICoords(viewport)

        for ((i, v) in logs.entries.withIndex()) {
            v.value.draw(500f, 500f + (logs.count() / 2 - i) * 16f)
        }
    }

    private fun debug(dt: Float) {
        val ship = Global.getCombatEngine().ships.firstOrNull { it.variant.hasHullMod(HullMods.AUTOMATED) } ?: return

        (ship as Ship).ai = null

        if (!ship.velocity.isZeroVector()) {
            ship.giveCommand(ShipCommand.DECELERATE, null, 0)
            return
        }

        val target = Vector2f(Global.getCombatEngine().viewport.convertScreenXToWorldX(Global.getSettings().mouseX.toFloat()), Global.getCombatEngine().viewport.convertScreenYToWorldY(Global.getSettings().mouseY.toFloat())

        )

        setFacing(ship, target)
    }

    private fun speedupAsteroids() {
        val asteroids = Global.getCombatEngine().asteroids
        for (i in asteroids.indices) {
            val a = asteroids[i]
            a.mass = 0f
            a.velocity.set(VectorUtils.getDirectionalVector(Vector2f(), a.velocity) * 1200f)
        }
    }
}

fun setFacing(ship: ShipAPI, target: Vector2f) {
    val tgtFacing = VectorUtils.getFacing(target - ship.location)
    val d = MathUtils.getShortestRotation(ship.facing, tgtFacing)
    val v = ship.angularVelocity
    val a = ship.turnAcceleration

    val cmd = if (d > 0) setFacing2(d, v, a, TURN_LEFT, TURN_RIGHT)
    else setFacing2(-d, -v, a, TURN_RIGHT, TURN_LEFT)

    cmd?.let { ship.giveCommand(it, null, 0) }
}

fun setFacing2(d: Float, v: Float, a: Float, accel: ShipCommand, decel: ShipCommand) = when {
    v < 0 -> accel
    (v * v) / (a * 2f) > d -> decel
    d < 0.75f -> null
    else -> accel
}

fun angleToStop(v: Float, a: Float) = (v * v) / (a * 2f)
