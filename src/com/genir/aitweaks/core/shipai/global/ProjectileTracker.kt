package com.genir.aitweaks.core.shipai.global

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.extensions.boundsRadius
import com.genir.aitweaks.core.extensions.isMissile
import com.genir.aitweaks.core.extensions.root
import com.genir.aitweaks.core.extensions.timeAdjustedVelocity
import com.genir.aitweaks.core.handles.WeaponHandle.Companion.handle
import com.genir.aitweaks.core.shipai.autofire.ballistics.BallisticTarget
import com.genir.aitweaks.core.shipai.autofire.ballistics.willHitCircumference

class ProjectileTracker : BaseEveryFrameCombatPlugin() {
    private val threats: MutableMap<ShipAPI, MutableSet<DamagingProjectileAPI>> = mutableMapOf()

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        val newProjectiles = Global.getCombatEngine().projectiles.asSequence().filter {
            !it.isMissile && it.elapsed == 0f
        }

        val allTargets: List<BallisticTarget> = findTargets()

        newProjectiles.forEach { projectile ->
            if (projectile.weapon == null) {
                return@forEach
            }

            val target: ShipAPI = firstShipAlongFlightPath(projectile, allTargets)
                ?: return@forEach

            threats.getOrPut(target) { mutableSetOf() }.add(projectile)
        }

        garbageCollection()
    }

    fun getThreats(ship: ShipAPI): Sequence<DamagingProjectileAPI> {
        val allThreats = threats[ship]
            ?: return sequenceOf()

        return allThreats.asSequence().filter { projectile ->
            !projectile.isExpired && !projectile.wasRemoved()
        }
    }

    private fun garbageCollection() {
        val keyToClean = threats.keys.randomOrNull() ?: return

        val valuesToClean = threats[keyToClean]
        if (valuesToClean == null) {
            threats.remove(keyToClean)
            return
        }

        val iter = valuesToClean.iterator()
        while (iter.hasNext()) {
            val projectile = iter.next()
            if (projectile.isExpired || projectile.wasRemoved()) {
                iter.remove()
            }
        }

        if (valuesToClean.isEmpty()) {
            threats.remove(keyToClean)
        }
    }

    private fun findTargets(): List<BallisticTarget> {
        val ships = Global.getCombatEngine().ships.asSequence().filter { ship ->
            when {
                ship.isFighter -> false
                ship.isExpired -> false

                else -> true
            }
        }

        val targets = ships.map { ship ->
            BallisticTarget(
                ship.location,
                ship.timeAdjustedVelocity,
                ship.boundsRadius,
                ship,
            )
        }

        return targets.toList()
    }

    private fun firstShipAlongFlightPath(projectile: DamagingProjectileAPI, allTargets: List<BallisticTarget>): ShipAPI? {
        val targets: Sequence<BallisticTarget> = allTargets.asSequence().filter { target ->
            target.entity.root != projectile.weapon.ship.root
        }

        var closestHit: Float = Float.MAX_VALUE
        var hit: BallisticTarget? = null

        targets.forEach { target ->
            val hitRange: Float? = willHitCircumference(projectile, target)

            if (hitRange != null && hitRange < closestHit) {
                closestHit = hitRange
                hit = target
            }
        }

        if (closestHit > projectile.weapon.handle.totalRange) {
            return null
        }

        return hit?.entity as? ShipAPI
    }
}
