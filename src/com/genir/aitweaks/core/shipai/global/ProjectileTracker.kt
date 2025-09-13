package com.genir.aitweaks.core.shipai.global

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.extensions.isMissile
import com.genir.aitweaks.core.extensions.root
import com.genir.aitweaks.core.handles.WeaponHandle.Companion.handle
import com.genir.aitweaks.core.shipai.autofire.ballistics.Hit
import com.genir.aitweaks.core.shipai.autofire.ballistics.analyzeHit

class ProjectileTracker : BaseEveryFrameCombatPlugin() {
    private val threats: MutableMap<ShipAPI, MutableSet<DamagingProjectileAPI>> = mutableMapOf()

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        val newProjectiles = Global.getCombatEngine().projectiles.asSequence().filter {
            !it.isMissile && it.elapsed == 0f
        }

        val allTargets: List<ShipAPI> = findTargets()

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

    fun threats(ship: ShipAPI): Sequence<DamagingProjectileAPI> {
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

    private fun findTargets(): List<ShipAPI> {
        val ships = Global.getCombatEngine().ships.asSequence().filter { ship ->
            when {
                ship.isFighter -> false
                ship.isExpired -> false

                else -> true
            }
        }

        return ships.toList()
    }

    private fun firstShipAlongFlightPath(projectile: DamagingProjectileAPI, allTargets: List<ShipAPI>): ShipAPI? {
        val targets: Sequence<ShipAPI> = allTargets.asSequence().filter { target ->
            target.root != projectile.weapon?.ship?.root
        }

        var closestHit: Hit? = null
        for (target in targets) {
            val hit: Hit? = analyzeHit(projectile, target)
            if (hit != null && (closestHit == null || hit.range < closestHit.range)) {
                closestHit = hit
            }
        }

        if (closestHit == null || closestHit.range > projectile.weapon.handle.engagementRange) {
            return null
        }

        return closestHit.target as? ShipAPI
    }
}
