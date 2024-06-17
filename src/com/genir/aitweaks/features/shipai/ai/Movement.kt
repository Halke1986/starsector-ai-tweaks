package com.genir.aitweaks.features.shipai.ai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.utils.*
import com.genir.aitweaks.utils.extensions.*
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
        val (headingPoint, velocity) = when {
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

                val headingPoint = ai.maneuverTarget.location - attackPositionOffset.resized(ship.minRange)
                val velocity = (headingPoint - (ai.headingPoint ?: headingPoint)) / dt
                Pair(headingPoint, velocity)
            }

            // Nothing to do, stop the ship.
            else -> Pair(ship.location, Vector2f())
        }

        // Avoid border. When in border zone, do not attempt to lead
        // target, as it may lead to deeper intrusion into the border zone.
        val censoredHeadingPoint = avoidBorder(headingPoint)
        val censoredVelocity = if (censoredHeadingPoint == headingPoint) velocity else Vector2f()

        ai.headingPoint = censoredHeadingPoint
        ai.desiredHeading = engineController.heading(censoredHeadingPoint, censoredVelocity) { v -> avoidCollisions(dt, v) }
    }

    /** Make the ship avoid map border. The ship will attempt to move
     * inside a rectangle with rounded corners placed `borderNoGoZone`
     * units from map border.*/
    private fun avoidBorder(headingPoint: Vector2f): Vector2f {
        ai.isAvoidingBorder = false

        val mapH = Global.getCombatEngine().mapHeight / 2f
        val mapW = Global.getCombatEngine().mapWidth / 2f
        val borderZone = Preset.borderNoGoZone + Preset.borderCornerRadius

        // Translate ship coordinates so that the ship appears to be
        // always near a map corner. That way we can use a circle
        // calculations to approximate a rectangle with rounded corners.
        val l = ship.location
        val dirToBorder = Vector2f()
        dirToBorder.x = if (l.x > 0) (l.x - mapW + borderZone).coerceAtLeast(0f)
        else (l.x + mapW - borderZone).coerceAtMost(0f)
        dirToBorder.y = if (l.y > 0) (l.y - mapH + borderZone).coerceAtLeast(0f)
        else (l.y + mapH - borderZone).coerceAtMost(0f)

        // Distance into the border zone.
        val d = (dirToBorder.length() - Preset.borderCornerRadius).coerceAtLeast(0f)

        // Ship is far from border, no avoidance required.
        if (d == 0f) return headingPoint

        val borderFacing = dirToBorder.getFacing()
        val travelFacing = (headingPoint - ship.location).getFacing()
        val intrusionAngle = MathUtils.getShortestRotation(borderFacing, travelFacing)

        // Ship attempts to move away from the border on its own.
        if (abs(intrusionAngle) > 90f) return headingPoint

        // Allow chasing targets into the border zone.
        val attackAngle = ai.attackTarget?.let { (it.location - ship.location).getFacing() - borderFacing }
        val absAllowedAngle = if (attackAngle?.sign == intrusionAngle.sign) min(90f, abs(attackAngle))
        else 90f

        if (abs(intrusionAngle) + 1f >= absAllowedAngle) return headingPoint

        // The closer the ship is to map edge, the stronger
        // the heading transformation away from the border.
        val avoidForce = (d / (Preset.borderNoGoZone - Preset.borderHardNoGoZone))
        val correctionSign = if (intrusionAngle.sign > 0) 1f else -1f
        val correctionAngle = (absAllowedAngle - abs(intrusionAngle)) * correctionSign * avoidForce

        ai.isAvoidingBorder = true
        return headingPoint.rotatedAroundPivot(Rotation(correctionAngle), ship.location)
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

    // TODO Avoid modular ships

    private fun avoidCollisions(dt: Float, expectedVelocity: Vector2f): Vector2f {
        val obstacles = Global.getCombatEngine().ships.filter {
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

        // Course is clear, move ahead.
        if (obstacles.isEmpty()) return expectedVelocity

        // Gather all speed limits.
        val expectedSpeed = expectedVelocity.length()
        val limits = obstacles.mapNotNull { obstacle ->
            val lim = vMaxToObstacle(dt, obstacle)
            if (lim.speed < expectedSpeed) lim
            else null
        }

        // No relevant speed limits found, move ahead.
        if (limits.isEmpty()) return expectedVelocity

        // Find the most severe speed limit.
        val expectedFacing = expectedVelocity.getFacing()
        val lowestLimit = limits.minByOrNull { it.clampSpeed(expectedFacing, expectedSpeed) } ?: return expectedVelocity

        // Most severe speed limit does not influence speed ahead.
        if (lowestLimit.clampSpeed(expectedFacing, expectedSpeed) == expectedSpeed) return expectedVelocity

        // Find new heading that circumvents the lowest speed limit.
        val facingOffset = lowestLimit.facingOffset(expectedSpeed)
        val angleToLimit = MathUtils.getShortestRotation(expectedFacing, lowestLimit.facing)
        val angleToNewFacing = angleToLimit - facingOffset * angleToLimit.sign
        val newFacing = expectedFacing + angleToNewFacing

        if (angleToNewFacing >= 89f) return Vector2f()

        // Clamp new heading to not violate any of the speed limits.
        val newSpeed = limits.fold(expectedSpeed) { clampedSpeed, lim ->
            lim.clampSpeed(newFacing, clampedSpeed)
        }

        return expectedVelocity.rotated(Rotation(angleToNewFacing)).resized(newSpeed)
    }

    /** Limit allows to clamp velocity to not exceed max speed in a given direction.
     * From right triangle, the equation for max speed is:
     *
     * maxSpeed = speedLimit / cos( abs(limitFacing - velocityFacing) )
     *
     * To avoid using trigonometric functions, f(x) = 1/cos(x) is approximated as
     * g(t) = 1/t + t/5 where t = PI/2 - x. */
    private data class Limit(val facing: Float, val speed: Float) {
        fun clampSpeed(expectedFacing: Float, expectedSpeed: Float): Float {
            val angleFromLimit = abs(MathUtils.getShortestRotation(expectedFacing, facing))
            if (angleFromLimit >= 90f) return expectedSpeed

            val t = (PI / 2f - angleFromLimit * DEGREES_TO_RADIANS).toFloat()
            val e = speed * (1f / t + t / 5f)

            return min(e, expectedSpeed)
        }

        fun facingOffset(expectedSpeed: Float): Float {
            val a = 1f
            val b = -5f * (expectedSpeed / speed)
            val c = 5f

            val t = quad(a, b, c)!!.second
            return abs(t - PI / 2f).toFloat() * RADIANS_TO_DEGREES
        }
    }

    private fun vMaxToObstacle(dt: Float, obstacle: ShipAPI): Limit {
        val fullStop = 0.00001f

        val direction = obstacle.location - ship.location
        val dirAbs = direction.length()
        val dirFacing = direction.getFacing()
        val distance = dirAbs - (totalCollisionRadius + obstacle.totalCollisionRadius + Preset.collisionBuffer)

        // Already colliding.
        if (distance <= 0f) return Limit(dirFacing, fullStop)

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
        return Limit(dirFacing, vMax.coerceAtLeast(fullStop))
    }
}
