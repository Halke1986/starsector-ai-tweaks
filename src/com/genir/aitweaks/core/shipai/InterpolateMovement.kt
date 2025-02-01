package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.div
import com.genir.aitweaks.core.extensions.minus
import com.genir.aitweaks.core.extensions.plus
import com.genir.aitweaks.core.extensions.times
import com.genir.aitweaks.core.state.State
import org.lwjgl.util.vector.Vector2f

/**
 * The AI advance() method for fast-time ships is invoked multiple times per frame,
 * whereas normal-time ships have their coordinates updated only once per frame.
 *
 * This discrepancy can cause fast-time ships to calculate their movement
 * based on target coordinates that may appear to change erratically,
 * resulting in imprecise movement commands.
 *
 * To address this issue, the movement of fast-time ships is also calculated
 * once per frame. The resulting movement is then interpolated for any additional
 * advance() calls within the same frame, ensuring smoother and more precise movement.
 */
class InterpolateMovement<T>(private val ship: ShipAPI) {
    private var prevValue: T? = null
    private var value: T? = null

    private var timestamp: Int = 0
    private var dtSum: Float = 0f

    fun advance(dt: Float, nextValue: () -> T?): T? {
        val timeMult: Float = ship.mutableStats.timeMult.modifiedValue

        if (State.state.frameCount > timestamp) {
            timestamp = State.state.frameCount
            dtSum = 0f

            prevValue = value
            value = nextValue()
        }

        // No need to interpolate for ships in normal time flow.
        if (timeMult == 1f) {
            return value
        }

        dtSum += dt
        val value = value
        val prevValue = prevValue

        if (value == null || prevValue == null) {
            return value
        }

        val delta = (value - prevValue) / timeMult
        return prevValue + delta * (dtSum / Global.getCombatEngine().elapsedInLastFrame)
    }

    private infix operator fun T.minus(other: T): T = when (this!!::class.java) {
        Vector2f::class.java -> ((this as Vector2f) - (other as Vector2f)) as T
        Float::class.java -> ((this as Float) - (other as Float)) as T
        else -> this
    }

    private infix operator fun T.plus(other: T): T = when (this!!::class.java) {
        Vector2f::class.java -> ((this as Vector2f) + (other as Vector2f)) as T
        Float::class.java -> ((this as Float) + (other as Float)) as T
        else -> this
    }

    private infix operator fun T.div(other: Float): T = when (this!!::class.java) {
        Vector2f::class.java -> ((this as Vector2f) / other) as T
        Float::class.java -> ((this as Float) / other) as T
        else -> this
    }

    private infix operator fun T.times(other: Float): T = when (this!!::class.java) {
        Vector2f::class.java -> ((this as Vector2f) * other) as T
        Float::class.java -> ((this as Float) * other) as T
        else -> this
    }
}
