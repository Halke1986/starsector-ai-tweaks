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

fun applyNeedlerAI(ship: ShipAPI) {
    ship.weaponGroupsCopy.forEach { group ->
        val plugins = group.aiPlugins
        for (i in plugins.indices) {
            if (applyTo.contains(plugins[i].weapon.spec.weaponId)) {
                val ai = NeedlerAI(plugins[i])
                plugins[i] = ai
            }
        }
    }
}

class NeedlerAI(
    private val basePlugin: AutofireAIPlugin,
) : AutofireAIPlugin {
    private var shouldFire = false
    private var shouldFireSince = 0f

    override fun advance(p0: Float) {
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

        // Apply a delay to counteract shield flickering.
        // Enemy with fully raised shields can be attacked
        // with no delay.
        val shield = basePlugin.targetShip.shield
        val fullShield = shield.arc == shield.activeArc
        val delay = 0.8f
        shouldFireSince += p0
        shouldFire = fullShield || shouldFireSince > delay
    }

    override fun shouldFire(): Boolean = shouldFire
    override fun forceOff() = basePlugin.forceOff()
    override fun getTarget(): Vector2f? = basePlugin.target
    override fun getTargetShip(): ShipAPI? = basePlugin.targetShip
    override fun getWeapon(): WeaponAPI = basePlugin.weapon
    override fun getTargetMissile(): MissileAPI? = basePlugin.targetMissile
}

