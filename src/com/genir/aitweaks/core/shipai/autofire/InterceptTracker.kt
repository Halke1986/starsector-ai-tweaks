package com.genir.aitweaks.core.shipai.autofire

import com.genir.aitweaks.core.extensions.facing
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.utils.types.Direction
import org.lwjgl.util.vector.Vector2f

/** Tracks intercept angular velocity in weapon slot frame of reference. */
class InterceptTracker(private val weapon: WeaponHandle) {
    private var prevAngleToIntercept: Direction? = null
    var interceptVelocity = 0f

    fun advance(dt: Float, intercept: Vector2f?) {
        if (intercept == null) {
            return
        }

        val angleToIntercept: Direction = intercept.facing - weapon.absoluteArc.facing
        interceptVelocity = (angleToIntercept - (prevAngleToIntercept ?: angleToIntercept)).degrees / dt
        prevAngleToIntercept = angleToIntercept
    }

    /** clear should be called after target change,
     * to avoid false intercept velocity estimation. */
    fun clear() {
        prevAngleToIntercept = null
        interceptVelocity = 0f
    }
}
