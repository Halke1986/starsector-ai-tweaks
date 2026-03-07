package com.genir.aitweaks.core.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CollisionGridAPI
import com.fs.starfarer.api.combat.CombatAsteroidAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.handles.ShipHandle
import com.genir.aitweaks.core.handles.ShipHandle.Companion.handle
import org.lwjgl.util.vector.Vector2f

object Grid {
    fun ships(p: Vector2f, r: Float): Sequence<ShipHandle> {
        return Global.getCombatEngine().shipGrid.search(p, r).filterIsInstance<ShipAPI>().map { it.handle }
    }

    fun missiles(p: Vector2f, r: Float): Sequence<MissileAPI> {
        return Global.getCombatEngine().missileGrid.search(p, r).filterIsInstance<MissileAPI>()
    }

    fun asteroids(p: Vector2f, r: Float): Sequence<CombatAsteroidAPI> {
        return Global.getCombatEngine().asteroidGrid.search(p, r).filterIsInstance<CombatAsteroidAPI>()
    }

    private fun CollisionGridAPI.search(p: Vector2f, r: Float): Sequence<Any> {
        return getCheckIterator(p, r * 2, r * 2).asSequence()
    }
}
