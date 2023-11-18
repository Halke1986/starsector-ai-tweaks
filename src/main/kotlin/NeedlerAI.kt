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

fun applyNeedlerAI(ship: ShipAPI) {
    val shipNeedlers = mutableListOf<NeedlerAI>()
    ship.weaponGroupsCopy.forEach { group ->
        val plugins = group.aiPlugins
        for (i in plugins.indices) {
            if (applyTo.contains(plugins[i].weapon.spec.weaponId)) {
                val ai = NeedlerAI(plugins[i], shipNeedlers)
                plugins[i] = ai
                shipNeedlers.add(ai)
            }
        }
    }
}

class NeedlerAI(
    private val basePlugin: AutofireAIPlugin,
    private val shipNeedlers: List<NeedlerAI>,
) : AutofireAIPlugin {
    private var shouldFire = false
    private var shouldFireSince = 0f

    private var overrideBase = false

    override fun advance(p0: Float) {
        debug()
        basePlugin.advance(p0)

        // Consider firing only when base AI wants
        // to fire and the attack will hit shields.
        val shouldFireNow = basePlugin.shouldFire() &&
                willHitShield(basePlugin.weapon, basePlugin.targetShip)
        if (!shouldFireNow) {
            shouldFire = false
            shouldFireSince = 0f
            return
        }

        shouldFireSince += p0

        // Enemy with shields fully raised can be attacked
        // with no delay.
        val shield = basePlugin.targetShip.shield
        val fullShield = shield.arc == shield.activeArc
        shouldFire = fullShield || shouldFireSince > 0.8f
    }

    override fun shouldFire(): Boolean = shouldFire
    override fun forceOff() = basePlugin.forceOff()
    override fun getTarget(): Vector2f? = basePlugin.target
    override fun getTargetShip(): ShipAPI? = basePlugin.targetShip
    override fun getWeapon(): WeaponAPI = basePlugin.weapon
    override fun getTargetMissile(): MissileAPI? = basePlugin.targetMissile

    private fun debug() {
        if (basePlugin.weapon.ship.owner != 0) return
        if (overrideBase != (shouldFire != basePlugin.shouldFire())) {
            overrideBase = !overrideBase
            count += if (overrideBase) 1 else -1
            debugStr = count.toString()
        }
    }
}

