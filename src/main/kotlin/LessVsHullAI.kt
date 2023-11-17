package com.genir.aitweaks

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

val applyTo = listOf(
    "lightneedler",
    "heavyneedler",
)

var count = 0

fun applyLessVsHullAI(ship: ShipAPI) {
    ship.weaponGroupsCopy.forEach { group ->
        val plugins = group.aiPlugins
        for (i in plugins.indices) {
            if (applyTo.contains(plugins[i].weapon.spec.weaponId)) {
                count++
                debugStr = count.toString()
                plugins[i] = LessVsHullAI(plugins[i])
            }
        }
    }
}

class LessVsHullAI(private val basePlugin: AutofireAIPlugin) : AutofireAIPlugin {
    override fun shouldFire(): Boolean =
        basePlugin.shouldFire() && willHitShield(basePlugin.weapon, basePlugin.targetShip)

    override fun advance(p0: Float) = basePlugin.advance(p0)
    override fun forceOff() = basePlugin.forceOff()
    override fun getTarget(): Vector2f? = basePlugin.target
    override fun getTargetShip(): ShipAPI? = basePlugin.targetShip
    override fun getWeapon(): WeaponAPI = basePlugin.weapon
    override fun getTargetMissile(): MissileAPI? = basePlugin.targetMissile
}

