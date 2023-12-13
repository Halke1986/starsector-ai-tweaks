package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.*
import com.genir.aitweaks.extensions.hasBestTargetLeading
import com.genir.aitweaks.extensions.isAnyBeam
import com.genir.aitweaks.extensions.targetEntity
import com.genir.aitweaks.intersectionTime
import com.genir.aitweaks.times
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f

fun applyTargetLeadAI(ship: ShipAPI) {
    ship.weaponGroupsCopy.forEach { group ->
        val plugins = group.aiPlugins
        for (i in plugins.indices) {
            plugins[i] = TargetLeadAI(plugins[i])
        }
    }
}

class TargetLeadAI(private val basePlugin: AutofireAIPlugin) : AutofireAIPlugin {
    private var prevTarget: CombatEntityAPI? = null
    private var attackTime: Float = 0f
    private var idleTime: Float = 0f

    override fun advance(timeDelta: Float) {
        basePlugin.advance(timeDelta)

        val target = basePlugin.targetEntity
        if (target != null && prevTarget != target) {
            prevTarget = target
            attackTime = 0f
        }

        if (basePlugin.weapon.isFiring) {
            attackTime += timeDelta
            idleTime = 0f
        } else idleTime += timeDelta

        if (idleTime >= 3f) attackTime = 0f
    }

    override fun getTarget(): Vector2f? {
        val target = basePlugin.targetEntity ?: return null

        // no need to calculate lead for beams
        if (weapon.isAnyBeam) return target.location

        val tgtLocation = target.location - weapon.location
        val tgtVelocity = target.velocity - weapon.ship.velocity
        val travelT = intersectionTime(tgtLocation, tgtVelocity, 0f, weapon.projectileSpeed) ?: return null
        return target.location + tgtVelocity.times(travelT / getAccuracy())
    }

    override fun shouldFire(): Boolean = basePlugin.shouldFire()
    override fun forceOff() = basePlugin.forceOff()
    override fun getTargetShip(): ShipAPI? = basePlugin.targetShip
    override fun getWeapon(): WeaponAPI = basePlugin.weapon
    override fun getTargetMissile(): MissileAPI? = basePlugin.targetMissile

    private fun getAccuracy(): Float {
        if (basePlugin.weapon.hasBestTargetLeading) return 1f

        val accBase = basePlugin.weapon.ship.aimAccuracy
        val accBonus = basePlugin.weapon.spec.autofireAccBonus
        return (accBase - (accBonus + attackTime / 15f)).coerceAtLeast(1f)
    }
}
