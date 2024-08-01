package com.genir.aitweaks.core.features.shipai.systems

import org.lwjgl.util.vector.Vector2f

interface SystemAI {
    fun advance(dt: Float)

    fun holdManeuverTarget(): Boolean

    fun overrideHeading(): Vector2f?

    fun overrideFacing(): Float?
}