package com.genir.aitweaks.core.features.shipai.systems

import org.lwjgl.util.vector.Vector2f

interface SystemAI {
    fun advance(dt: Float)

    fun holdManeuverTarget(): Boolean

    fun overrideHeading(): Pair<Vector2f, Vector2f>?

    fun overrideFacing(): Pair<Vector2f, Vector2f>?
}