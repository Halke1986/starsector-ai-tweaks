package com.genir.aitweaks.core.features.shipai.ai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.extensions.*
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.isZeroVector
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign

class Movement(private val ai: Maneuver) {
    private val ship = ai.ship
    private val engineController = EngineController(ship)

    // Make strafe rotation direction random, but consistent for a given ship.
    private val strafeRotation = Rotation(if (ship.id.hashCode() % 2 == 0) 10f else -10f)
    private var averageAimOffset = RollingAverageFloat(Preset.aimOffsetSamples)
    private var totalCollisionRadius = ship.totalCollisionRadius

    fun advance(dt: Float) {
        setFacing()
        setHeading(dt)
    }

    private fun setFacing() {
        val (aimPoint, velocity) = when {
            // Face the attack target.
            ai.attackTarget != null -> {
                val target = ai.attackTarget!!

                // Average aim offset to avoid ship wobbling.
                val aimPointThisFrame = calculateOffsetAimPoint(target)
                val aimOffsetThisFrame = getShortestRotation(target.location, ship.location, aimPointThisFrame)
                val aimOffset = averageAimOffset.update(aimOffsetThisFrame)

                Pair(target.location.rotatedAroundPivot(Rotation(aimOffset), ship.location), target.velocity)
            }

            // Face threat direction when backing off and no target.
            ai.isBackingOff && !ai.threatVector.isZeroVector() -> {
                Pair(ship.location + ai.threatVector, Vector2f())
            }

            // Move to location, if no attack target.
            ai.targetLocation != null -> {
                Pair(ai.targetLocation, Vector2f())
            }

            // Nothing to do. Stop rotation.
            else -> Pair(ship.location, Vector2f())
        }

        ai.aimPoint = aimPoint
        ai.desiredFacing = engineController.facing(aimPoint, velocity)
    }

    private fun setHeading(dt: Float) {
        val (headingPoint: Vector2f, velocity: Vector2f) = when {
            // Move opposite to threat direction when backing off.
            // If there's no threat, the ship will continue to coast.
            ai.isBackingOff -> {
                if (ai.threatVector.isZeroVector()) Pair(ship.location, ship.velocity.resized(ship.maxSpeed))
                else Pair(ship.location - ai.threatVector.resized(1000f), Vector2f())
            }

            // Move directly to ordered location.
            ai.targetLocation != null -> {
                Pair(ai.targetLocation, Vector2f())
            }

            // Orbit target at effective weapon range.
            // Rotate away from threat if there are multiple enemy ships around.
            // Chase the target if there are no other enemy ships around.
            // Strafe target randomly if in range and no other threat.
            ai.maneuverTarget != null -> {
                // TODO will need syncing when interval tracking is introduced.
                val vectorToTarget = ai.maneuverTarget.location - ship.location
                val vectorToThreat = if (!ai.threatVector.isZeroVector()) ai.threatVector else vectorToTarget

                // Strafe the target randomly, when it's the only threat.
                val shouldStrafe = ai.is1v1 && ai.engagementRange(ai.maneuverTarget) <= ai.effectiveRange
                val attackPositionOffset = if (shouldStrafe) vectorToThreat.rotated(strafeRotation)
                else vectorToThreat

                // Let the attack coordinator review the calculated heading point.
                ai.proposedHeadingPoint = ai.maneuverTarget.location - attackPositionOffset.resized(ship.minRange)
                val headingPoint = (ai.reviewedHeadingPoint ?: ai.proposedHeadingPoint)!!
                ai.reviewedHeadingPoint = null

                val velocity = (headingPoint - (ai.headingPoint ?: headingPoint)) / dt
                Pair(headingPoint, velocity)
            }

            // Nothing to do, stop the ship.
            else -> Pair(ship.location, Vector2f())
        }

        ai.headingPoint = headingPoint
        ai.desiredHeading = engineController.heading(headingPoint, velocity) { v -> avoidObstacles(dt, v) }
    }

    /** Aim hardpoint weapons with entire ship, if possible. */
    private fun calculateOffsetAimPoint(attackTarget: ShipAPI): Vector2f {
        // Find intercept points of all hardpoints attacking the current target.
        val hardpoints = ship.allWeapons.filter { it.slot.isHardpoint }.mapNotNull { it.autofireAI }
        val aimedHardpoints = hardpoints.filter { it.targetShip != null && it.targetShip == attackTarget }
        val interceptPoints = aimedHardpoints.mapNotNull { it.intercept }

        if (interceptPoints.isEmpty()) return attackTarget.location

        // Average the intercept points. This may cause poor aim if different hardpoints
        // have weapons with significantly different projectile velocities.
        val interceptSum = interceptPoints.fold(Vector2f()) { sum, intercept -> sum + intercept }
        val aimPoint = interceptSum / interceptPoints.size.toFloat()

        return aimPoint
    }

    private fun avoidObstacles(dt: Float, expectedVelocity: Vector2f): Vector2f {
        val expectedSpeed = expectedVelocity.length()

        // Gather all speed limits.
        val limits: MutableList<Limit> = mutableListOf()
        limits.addAll(avoidCollisions(dt, expectedSpeed))
        avoidBorder(dt)?.let { limits.add(it) }

        // No relevant speed limits found, move ahead.
        if (limits.isEmpty()) return expectedVelocity

        // Find the most severe speed limit.
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

    private fun avoidCollisions(dt: Float, expectedSpeed: Float): List<Limit> {
        val potentialCollisions = Global.getCombatEngine().ships.filter {
            when {
                it == ship -> false
                it.owner != ship.owner -> false
                it.isFighter -> false

                // Modules and drones count towards
                // their parent collision radius.
                it.isModule -> false
                it.isDrone -> false
                else -> true
            }
        }

        // Gather all speed limits.
        return potentialCollisions.mapNotNull { obstacle ->
            val lim = vMaxToObstacle(dt, obstacle)
            if (lim.speed < expectedSpeed) lim
            else null
        }
    }

    private fun avoidBorder(dt: Float): Limit? {
        ai.isAvoidingBorder = false

        val mapH = Global.getCombatEngine().mapHeight / 2f
        val mapW = Global.getCombatEngine().mapWidth / 2f
        val borderZone = Preset.borderNoGoZone + Preset.borderCornerRadius

        // Translate ship coordinates so that the ship appears to be
        // always near a map corner. That way we can use a circle
        // calculations to approximate a rectangle with rounded corners.
        val l = ship.location
        val borderIntrusion = Vector2f()
        borderIntrusion.x = if (l.x > 0) (l.x - mapW + borderZone).coerceAtLeast(0f)
        else (l.x + mapW - borderZone).coerceAtMost(0f)
        borderIntrusion.y = if (l.y > 0) (l.y - mapH + borderZone).coerceAtLeast(0f)
        else (l.y + mapH - borderZone).coerceAtMost(0f)

        // Distance into the border zone.
        val d = (borderIntrusion.length() - Preset.borderCornerRadius).coerceAtLeast(0f)

        // Ship is far from border, no avoidance required.
        if (d == 0f) return null

        // Allow chasing targets into the border zone.
        val tgtLoc = ai.maneuverTarget?.location
        if (tgtLoc != null && !ai.isBackingOff && getShortestRotation(tgtLoc - ship.location, borderIntrusion) < 90f) {
            return null
        }

        ai.isAvoidingBorder = true

        // The closer the ship is to map edge, the stronger
        // the heading transformation away from the border.
        val avoidForce = (d / (Preset.borderNoGoZone - Preset.borderHardNoGoZone)).coerceAtMost(1f)
        return Limit(borderIntrusion.getFacing(), ship.maxSpeed * (1f - avoidForce) * dt)
    }

    private fun vMaxToObstacle(dt: Float, obstacle: ShipAPI): Limit {
        val direction = obstacle.location - ship.location
        val dirAbs = direction.length()
        val dirFacing = direction.getFacing()
        val distance = dirAbs - (totalCollisionRadius + obstacle.totalCollisionRadius + Preset.collisionBuffer)

        // Already colliding.
        if (distance <= 0f) return Limit(dirFacing, 0f)

        val vObstacle = vectorProjection(obstacle.velocity, direction)
        val aObstacle = vectorProjection(accelerationTracker[obstacle], direction)

        var vAbs = (vObstacle + direction).length() - dirAbs
        val aAbs = (aObstacle + direction).length() - dirAbs

        // Take obstacle acceleration into account when the obstacle is doing a brake check.
        // The acceleration is approximated as total velocity loss. Including actual
        // acceleration (shipAcc + aAbs) in calculations leads to erratic behavior.
        if (vAbs > 0f && aAbs < 0f) vAbs = 0f

        val angleFromBow = abs(MathUtils.getShortestRotation(ship.facing, dirFacing))
        val shipAcc = when {
            angleFromBow < 30f -> ship.deceleration
            angleFromBow < 150f -> ship.strafeAcceleration
            else -> ship.acceleration
        }

        val vMax = engineController.vMax(distance, shipAcc * dt * dt) + vAbs * dt
        return Limit(dirFacing, vMax)
    }

    /** Limit allows to clamp velocity to not exceed max speed in a direction along a given heading.
     * From right triangle, the equation for max speed is:
     *
     * maxSpeed = speedLimit / cos( abs(limitFacing - velocityFacing) )
     *
     * To avoid using trigonometric functions, f(x) = 1/cos(x) is approximated as
     * g(t) = 1/t + t/5 where t = PI/2 - x. */
    private class Limit(val heading: Float, speed: Float) {
        // Clamp the speed limit so that different limits remain
        // distinguishable by producing a slightly different value
        // of f(x) = 1/cos(x) for a given hading, instead of all
        // being equal to 0f.
        val speed = speed.coerceAtLeast(0.00001f)

        // Clamp expectedSpeed to maximum speed in which ship can
        // travel along the expectedHeading and not break the limit.
        fun clampSpeed(expectedHeading: Float, expectedSpeed: Float): Float {
            val angleFromLimit = abs(MathUtils.getShortestRotation(expectedHeading, heading))
            if (angleFromLimit >= 90f) return expectedSpeed

            val t = (PI / 2f - angleFromLimit * DEGREES_TO_RADIANS).toFloat()
            val e = speed * (1f / t + t / 5f)
            return min(e, expectedSpeed)
        }

        // Calculate angle from the limit heading, at which traveling
        // with expectedSpeed will not break the limit.
        fun headingOffset(expectedSpeed: Float): Float {
            val a = 1f
            val b = -5f * (expectedSpeed / speed)
            val c = 5f

            val t = quad(a, b, c)!!.second
            return abs(t - PI / 2f).toFloat() * RADIANS_TO_DEGREES
        }
    }
}
