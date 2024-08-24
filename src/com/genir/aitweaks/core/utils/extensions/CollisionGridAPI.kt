package com.genir.aitweaks.core.utils.extensions

import com.fs.starfarer.api.combat.CollisionGridAPI
import com.fs.starfarer.api.combat.CombatEntityAPI
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f

/** Get all entities within radius r of the point p. */
inline fun <reified T> CollisionGridAPI.get(p: Vector2f, r: Float): Sequence<T> {
    val entities = getCheckIterator(p, r * 2f, r * 2f).asSequence().filterIsInstance<T>()
    return entities.filter { ((it as CombatEntityAPI).location - p).lengthSquared <= r * r }
}
