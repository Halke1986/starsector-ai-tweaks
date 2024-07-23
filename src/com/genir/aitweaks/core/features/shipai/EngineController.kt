package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand.*
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.extensions.resized
import com.genir.aitweaks.core.utils.extensions.rotated
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.*
import org.lwjgl.util.vector.Vector2f
import kotlin.math.*
import kotlin.random.Random

class EngineController(val ship: ShipAPI) {
    /** Limit allows to restrict velocity to not exceed
     * max speed in a direction along a given heading. */
    data class Limit(val heading: Float, val speed: Float)

    /** Set ship heading towards 'target' location. Appropriate target
     * leading is calculated based on 'targetVelocity'. If ship is already
     * at 'target' location, it will match the target velocity. Limits are
     * used to restrict the velocity, e.g. for collision avoidance purposes.
     * Returns the calculated expected velocity. */
    fun heading(target: Vector2f, targetVelocity: Vector2f, limits: List<Limit> = listOf()): Vector2f {
        // Change unit of time from second to
        // animation frame duration (* dt).
        val dt = Global.getCombatEngine().elapsedInLastFrame
        val af = ship.acceleration * dt * dt
        val ab = ship.deceleration * dt * dt
        val al = ship.strafeAcceleration * dt * dt
        val vMax = max(ship.maxSpeed, ship.velocity.length()) * dt + af

        // Transform input into ship frame of reference. Account for
        // ship angular velocity, as linear acceleration is applied
        // by the game engine after rotation.
        val w = ship.angularVelocity * dt
        val toShipFacing = 90f - ship.facing - w
        val r = Rotation(toShipFacing)
        val l = r.rotate(target - ship.location)
        val v = r.rotate(ship.velocity) * dt
        val vt = r.rotate(targetVelocity) * dt

        // Stop if reached stationary target. Distance threshold for
        // stopping is the distance the ship covers in one frame after
        // accelerating from a standstill.
        if ((target - ship.location).length() < af && vt.isZeroVector()) {
            if (!ship.velocity.isZeroVector()) ship.command(DECELERATE)
            return Vector2f()
        }

        // Distance to target location in next frame. Next frame
        // is used because game engine apparently updates location
        // before updating velocity, so it's only the next frame
        // the current changes will take effect.
        val d = l - v + vt

        // Maximum velocity towards target for both axis of movement.
        // Any higher velocity would lead to overshooting target location.
        val vmx = if (d.x > 0) vMax(d.x, al) else -vMax(-d.x, al)
        val vmy = if (d.y > 0) vMax(d.y, ab) else -vMax(-d.y, af)

        // Expected velocity directly towards target location.
        val vtt = if (vmx == 0f || vmy == 0f) Vector2f(vmx, vmy)
        else d * min(vmx / d.x, vmy / d.y)

        // Expected velocity change.
        val ve = (vtt + vt).clampLength(vMax)
        val vec = limitVelocity(dt, toShipFacing, ve, limits)
        val dv = vec - v

        // Stop if expected velocity is smaller than half of minimum delta v.
        if (vec.length() < af / 2f && !ship.velocity.isZeroVector()) {
            ship.command(DECELERATE)
            return Vector2f()
        }

        // Proportional thrust required to achieve
        // the expected velocity change.
        val ff = +dv.y / af
        val fb = -dv.y / ab
        val fl = -dv.x / al
        val fr = +dv.x / al
        val fMax = max(max(ff, fb), max(fl, fr))

        // Give commands to achieve the calculated thrust.
        if (shouldAccelerate(+d.y, ff, fMax)) ship.command(ACCELERATE)
        if (shouldAccelerate(-d.y, fb, fMax)) ship.command(ACCELERATE_BACKWARDS)
        if (shouldAccelerate(-d.x, fl, fMax)) ship.command(STRAFE_LEFT)
        if (shouldAccelerate(+d.x, fr, fMax)) ship.command(STRAFE_RIGHT)

        return r.reverse(vec)
    }

    /** Set ship facing towards 'target' location. Returns the expected facing angle. */
    fun facing(target: Vector2f, targetVelocity: Vector2f): Float {
        // Change unit of time from second to
        // animation frame duration (* dt).
        val dt = Global.getCombatEngine().elapsedInLastFrame
        val a = ship.turnAcceleration * dt * dt
        val w = ship.angularVelocity * dt
        val vr = (targetVelocity - ship.velocity) * dt

        // Calculate facing and facing change.
        val reachedTarget = (target - ship.location).length() < ship.collisionRadius / 2f
        val f = if (reachedTarget) ship.facing else (target - ship.location).getFacing()
        val df = if (reachedTarget) 0f else (target + vr - ship.location).getFacing() - f

        // Angular distance between expected
        // facing and ship facing the next frame.
        val r = MathUtils.getShortestRotation(ship.facing + w, f + df)

        // Expected velocity change. Since rotation is
        // 1-dimensional, as opposed to the 2-dimensional
        // heading, the calculations can be simplified.
        val we = sign(r) * vMax(abs(r), a) + df
        val dw = we - w

        if (shouldAccelerate(+r, +dw / a, 0f)) ship.command(TURN_LEFT)
        if (shouldAccelerate(-r, -dw / a, 0f)) ship.command(TURN_RIGHT)

        return f
    }

    /** Maximum velocity in given direction to not overshoot target. */
    private fun vMax(d: Float, a: Float): Float {
        val (q, _) = quad(0.5f, 0.5f, -d / a) ?: return 0f
        return floor(q) * a
    }

    /** Decide if the ship should accelerate in the given
     * direction to reach its target without overshooting. */
    private fun shouldAccelerate(d: Float, f: Float, m: Float) = when {
        f < 0.5f -> false
        d < 0f && f >= 0.5f -> true // braking to not overshoot target
        f >= m -> true
        else -> f / m > Random.nextFloat() // apply proportional thrust
    }

    private fun limitVelocity(dt: Float, toShipFacing: Float, expectedVelocity: Vector2f, absLimits: List<Limit>): Vector2f {
        // No relevant speed limits found, move ahead.
        if (absLimits.isEmpty()) return expectedVelocity

        // Translate speed limit to ship frame of reference.
        // Clamp the speed limit so that different limits remain
        // distinguishable by producing a slightly different value
        // of f(x) = 1/cos(x) for a given hading, instead of all
        // being equal to 0f.
        val limits = absLimits.map { Limit(it.heading + toShipFacing, (it.speed * dt).coerceAtLeast(0.00001f)) }

        // Find the most severe speed limit.
        val expectedSpeed = expectedVelocity.length()
        val expectedHeading = expectedVelocity.getFacing()
        val lowestLimit = limits.minByOrNull { it.clampSpeed(expectedHeading, expectedSpeed) }
            ?: return expectedVelocity

        // Most severe speed limit does not influence speed ahead.
        if (lowestLimit.clampSpeed(expectedHeading, expectedSpeed) == expectedSpeed) return expectedVelocity

        // Find new heading that circumvents the lowest speed limit.
        val headingOffset = lowestLimit.headingOffset(expectedSpeed)
        val angleToLimit = MathUtils.getShortestRotation(expectedHeading, lowestLimit.heading)
        val angleToNewFacing = angleToLimit - headingOffset * angleToLimit.sign
        val newFacing = expectedHeading + angleToNewFacing

        // Stop if angle to new heading is right, to avoid erratic behavior
        // when avoiding collision and being stopped close to destination.
        if (angleToNewFacing >= 89f) return Vector2f()

        // Clamp new heading to not violate any of the speed limits.
        val newSpeed = limits.fold(expectedSpeed) { clampedSpeed, lim ->
            lim.clampSpeed(newFacing, clampedSpeed)
        }

        return expectedVelocity.rotated(Rotation(angleToNewFacing)).resized(newSpeed)
    }

    /** Clamp expectedSpeed to maximum speed in which ship can travel
     * along the expectedHeading and not break the Limit.
     *
     * From right triangle, the equation for max speed is:
     * maxSpeed = speedLimit / cos( abs(limitFacing - velocityFacing) )
     *
     * To avoid using trigonometric functions, f(x) = 1/cos(x) is approximated as
     * g(t) = 1/t + t/5 where t = PI/2 - x. */
    private fun Limit.clampSpeed(expectedHeading: Float, expectedSpeed: Float): Float {
        val angleFromLimit = abs(MathUtils.getShortestRotation(expectedHeading, heading))
        if (angleFromLimit >= 90f) return expectedSpeed

        val t = (PI / 2f - angleFromLimit * DEGREES_TO_RADIANS).toFloat()
        val e = speed * (1f / t + t / 5f)
        return min(e, expectedSpeed)
    }

    /** Calculate angle from the limit heading, at which traveling
     * with expectedSpeed will not break the limit. */
    private fun Limit.headingOffset(expectedSpeed: Float): Float {
        val a = 1f
        val b = -5f * (expectedSpeed / speed)
        val c = 5f

        val t = quad(a, b, c)!!.second
        return abs(t - PI / 2f).toFloat() * RADIANS_TO_DEGREES
    }
}