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
import com.genir.aitweaks.core.utils.extensions.lengthSquared
import com.genir.aitweaks.core.utils.extensions.root
import com.genir.aitweaks.core.utils.extensions.totalRange
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f

fun closestEntityFinder(
    location: Vector2f, radius: Float, grid: CollisionGridAPI, evaluateEntity: (CombatEntityAPI) -> Pair<Float, Any>?
): Any? {
    val entities = grid.getCheckIterator(location, radius * 2.0f, radius * 2.0f).asSequence()
    val score = entities.filterIsInstance<CombatEntityAPI>().map { evaluateEntity(it) }
    return score.filterNotNull().minWithOrNull(compareBy { it.first })?.second
}

fun firstShipAlongLineOfFire(weapon: WeaponAPI, params: BallisticParams): Hit? {
    return closestEntityFinder(weapon.location, weapon.totalRange, shipGrid()) { entity ->
        when {
            entity !is ShipAPI -> null
            entity.isFighter -> null
            entity.isExpired -> null
            entity == weapon.ship -> null
            weapon.ship.root == entity.root -> null

            entity.owner == weapon.ship.owner -> analyzeAllyHit(weapon, entity, params)
            entity.isPhased -> null
            else -> analyzeHit(weapon, entity, params)
        }?.let { Pair(it.range, it) }
    } as? Hit
}

fun shipGrid(): CollisionGridAPI = Global.getCombatEngine().shipGrid

fun missileGrid(): CollisionGridAPI = Global.getCombatEngine().missileGrid

fun asteroidGrid(): CollisionGridAPI = Global.getCombatEngine().asteroidGrid

/** Get filtered entities of a given type within radius r of the point p. */
inline fun <reified T : CombatEntityAPI> CollisionGridAPI.get(p: Vector2f, r: Float, crossinline filter: (T) -> Boolean): Sequence<T> {
    val entities = getCheckIterator(p, r * 2, r * 2).asSequence().filterIsInstance<T>()
    return entities.filter { filter(it) && (it.location - p).lengthSquared <= r * r }
}
