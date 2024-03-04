package com.genir.aitweaks.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CollisionGridAPI
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.utils.extensions.rootModule
import com.genir.aitweaks.utils.extensions.totalRange
import org.lwjgl.util.vector.Vector2f

fun closestEntityFinder(
    location: Vector2f, radius: Float, grid: CollisionGridAPI, considerEntity: (CombatEntityAPI) -> Hit?
): Hit? {
    val entityIterator = grid.getCheckIterator(location, radius * 2.0f, radius * 2.0f)
    val hits = entityIterator.asSequence().mapNotNull { (it as? CombatEntityAPI)?.let(considerEntity) }
    return hits.minWithOrNull(compareBy { it.range })
}

fun firstShipAlongLineOfFire(weapon: WeaponAPI, params: BallisticParams): Hit? =
    closestEntityFinder(weapon.location, weapon.totalRange, shipGrid()) {
        when {
            it !is ShipAPI -> null
            it.isFighter -> null
            it == weapon.ship -> null
            it.isAlive && weapon.ship.rootModule == it.rootModule -> null

            it.owner == weapon.ship.owner -> analyzeAllyHit(weapon, it, params)
            it.isPhased -> null
            else -> analyzeHit(weapon, it, params)
        }
    }

fun shipGrid(): CollisionGridAPI = Global.getCombatEngine().shipGrid

fun missileGrid(): CollisionGridAPI = Global.getCombatEngine().missileGrid

fun asteroidGrid(): CollisionGridAPI = Global.getCombatEngine().asteroidGrid