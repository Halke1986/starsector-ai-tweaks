package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.extensions.targetEntity
import com.genir.aitweaks.rotateAroundPivot
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs

fun applyTargetLeadHardpointAI(ship: ShipAPI) {
    ship.weaponGroupsCopy.forEach { group ->
        val plugins = group.aiPlugins
        for (i in plugins.indices) {
            if (plugins[i].weapon.slot.isHardpoint) plugins[i] = TargetLeadHardpointAI(plugins[i])
        }
    }
}

class TargetLeadHardpointAI(private val basePlugin: AutofireAIPlugin) : AutofireAIPlugin {
    override fun getTarget(): Vector2f? {
        val target = this.targetEntity ?: return null
        val intercept = basePlugin.target ?: return null

        val tgtLocation = target.location - weapon.ship.location
        val tgtFacing = VectorUtils.getFacing(tgtLocation)
        val angleToTarget = MathUtils.getShortestRotation(tgtFacing, weapon.ship.facing)

        // Ship is already facing the target. Return the
        // original target location to not overcompensate.
        if (abs(angleToTarget) < weapon.arc / 2f) {
            return intercept
        }

        return rotateAroundPivot(intercept, weapon.ship.location, angleToTarget)
    }

    override fun getTargetShip(): ShipAPI? = basePlugin.targetShip
    override fun advance(timeDelta: Float) = basePlugin.advance(timeDelta)
    override fun shouldFire(): Boolean = basePlugin.shouldFire()
    override fun forceOff() = basePlugin.forceOff()
    override fun getWeapon(): WeaponAPI = basePlugin.weapon
    override fun getTargetMissile(): MissileAPI? = basePlugin.targetMissile
}
