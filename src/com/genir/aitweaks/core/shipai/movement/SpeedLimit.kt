package com.genir.aitweaks.core.shipai.movement

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.genir.aitweaks.core.utils.DEGREES_TO_RADIANS
import com.genir.aitweaks.core.utils.PI
import com.genir.aitweaks.core.utils.types.Direction

/** Limit allows to restrict velocity to not exceed
 * max speed in a direction along a given heading. */
data class SpeedLimit(
    val direction: Direction,
    val speedLimit: Float,
    val obstacle: CombatEntityAPI?,
) {
    /**
     * Clamp expectedSpeed to maximum speed in which ship can travel
     * along the expectedHeading and not break the Limit.
     *
     * From right triangle, the equation for max speed is:
     * maxSpeed = speedLimit / cos( abs(limitFacing - velocityFacing) )
     *
     * To avoid using trigonometric functions, f(x) = 1/cos(x) is approximated as
     * g(t) = 1/t(x) + t(x)/5 where t(x) = PI/2 - x
     */
    fun clampSpeed(expectedHeading: Direction, expectedSpeed: Float): Float {
        val angleFromLimit = (expectedHeading - direction).length
        if (angleFromLimit >= 90f) {
            return expectedSpeed
        }

        val t = PI / 2f - angleFromLimit * DEGREES_TO_RADIANS
        val e = speedLimit * (1f / t + t / 5f)
        return minOf(maxOf(0f, e), expectedSpeed)
    }
}