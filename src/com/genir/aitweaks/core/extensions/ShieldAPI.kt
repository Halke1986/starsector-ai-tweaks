package com.genir.aitweaks.core.extensions

import com.fs.starfarer.api.combat.ShieldAPI
import com.genir.aitweaks.core.utils.types.Arc
import com.genir.aitweaks.core.utils.types.Direction.Companion.direction
import org.lwjgl.util.vector.Vector2f

fun ShieldAPI.isHit(hitPoint: Vector2f): Boolean {
    return Arc(activeArc, facing.direction).contains(hitPoint)
}
