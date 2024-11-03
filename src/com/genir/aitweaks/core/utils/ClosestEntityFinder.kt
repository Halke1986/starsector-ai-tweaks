package com.genir.aitweaks.core.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CollisionGridAPI
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.features.shipai.autofire.BallisticParams
import com.genir.aitweaks.core.features.shipai.autofire.Hit
import com.genir.aitweaks.core.features.shipai.autofire.analyzeAllyHit
import com.genir.aitweaks.core.features.shipai.autofire.analyzeHit
import com.genir.aitweaks.core.utils.extensions.get
import com.genir.aitweaks.core.utils.extensions.rootModule
import com.genir.aitweaks.core.utils.extensions.totalRange
import org.lwjgl.util.vector.Vector2f

fun closestEntityFinder(
    location: Vector2f, radius: Float, grid: CollisionGridAPI, evaluateEntity: (CombatEntityAPI) -> Float?
): CombatEntityAPI? {
    val entityIterator = grid.getCheckIterator(location, radius * 2.0f, radius * 2.0f)
    val score = entityIterator.asSequence().filterIsInstance<CombatEntityAPI>().map { Pair(it, evaluateEntity(it)) }
    return score.filter { it.second != null }.minWithOrNull(compareBy { it.second })?.first
}

fun firstShipAlongLineOfFire(weapon: WeaponAPI, params: BallisticParams): Hit? {
    val entity = closestEntityFinder(weapon.location, weapon.totalRange, shipGrid()) {
        when {
            it !is ShipAPI -> null
            it.isFighter -> null
            it.isExpired -> null
            it == weapon.ship -> null
            weapon.ship.rootModule == it.rootModule -> null

            it.owner == weapon.ship.owner -> analyzeAllyHit(weapon, it, params)
            it.isPhased -> null
            else -> analyzeHit(weapon, it, params)
        }?.range
    }

    return entity?.let { analyzeHit(weapon, it, params) }
}

fun shipGrid(): CollisionGridAPI = Global.getCombatEngine().shipGrid

fun missileGrid(): CollisionGridAPI = Global.getCombatEngine().missileGrid

fun asteroidGrid(): CollisionGridAPI = Global.getCombatEngine().asteroidGrid

fun shipSequence(p: Vector2f, r: Float): Sequence<ShipAPI> = shipGrid().get<ShipAPI>(p, r)
