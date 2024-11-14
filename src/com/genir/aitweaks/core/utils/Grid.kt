package com.genir.aitweaks.core.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import org.lwjgl.util.vector.Vector2f

object Grid {
    fun ships(p: Vector2f, r: Float): Sequence<ShipAPI> {
        return Global.getCombatEngine().shipGrid.search(p, r).filterIsInstance<ShipAPI>()
    }

    fun missiles(p: Vector2f, r: Float): Sequence<MissileAPI> {
        return Global.getCombatEngine().missileGrid.search(p, r).filterIsInstance<MissileAPI>()
    }

    fun asteroids(p: Vector2f, r: Float): Sequence<CombatAsteroidAPI> {
        return Global.getCombatEngine().asteroidGrid.search(p, r).filterIsInstance<CombatAsteroidAPI>()
    }

    fun entities(c: Class<*>, p: Vector2f, r: Float): Sequence<CombatEntityAPI> {
        return when (c) {
            ShipAPI::class.java -> ships(p, r)
            MissileAPI::class.java -> missiles(p, r)
            CombatAsteroidAPI::class.java -> asteroids(p, r)
            else -> sequenceOf()
        }
    }

    private fun CollisionGridAPI.search(p: Vector2f, r: Float): Sequence<Any> {
        return getCheckIterator(p, r * 2, r * 2).asSequence()
    }
}
