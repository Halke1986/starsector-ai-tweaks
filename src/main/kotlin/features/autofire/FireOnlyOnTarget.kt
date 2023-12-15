package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.extensions.isAnyBeam
import com.genir.aitweaks.extensions.targetEntity
import com.genir.aitweaks.utils.intersectionTime
import com.genir.aitweaks.utils.times
import com.genir.aitweaks.utils.unitVector
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f

fun applyFireOnlyOnTargetAI(ship: ShipAPI) {
    ship.weaponGroupsCopy.forEach { group ->
        val plugins = group.aiPlugins
        for (i in plugins.indices) {
            if (plugins[i].weapon.type != WeaponAPI.WeaponType.MISSILE) {
                plugins[i] = FireOnlyOnTargetAI(plugins[i])
            }
        }
    }
}

class FireOnlyOnTargetAI(private val basePlugin: AutofireAIPlugin) : AutofireAIPlugin {
    override fun shouldFire(): Boolean {
        if (!basePlugin.shouldFire()) return false
        val target = this.targetEntity ?: return false

        val location = weapon.location - target.location
        val projectileDir = unitVector(weapon.currAngle)
        val velocity = if (weapon.isAnyBeam) projectileDir
        else {
            val projectileVelocity = projectileDir * weapon.projectileSpeed
            weapon.ship.velocity + projectileVelocity - target.velocity
        }

        return intersectionTime(location, velocity, target.collisionRadius, 0f) != null
    }

    override fun advance(p0: Float) = basePlugin.advance(p0)
    override fun forceOff() = basePlugin.forceOff()
    override fun getTarget(): Vector2f? = basePlugin.target
    override fun getTargetShip(): ShipAPI? = basePlugin.targetShip
    override fun getWeapon(): WeaponAPI = basePlugin.weapon
    override fun getTargetMissile(): MissileAPI? = basePlugin.targetMissile
}
