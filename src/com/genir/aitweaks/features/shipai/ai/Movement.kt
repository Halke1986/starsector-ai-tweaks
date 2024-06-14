package com.genir.aitweaks.features.shipai.ai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.debug.debugVertex
import com.genir.aitweaks.utils.*
import com.genir.aitweaks.utils.extensions.autofireAI
import com.genir.aitweaks.utils.extensions.resized
import com.genir.aitweaks.utils.extensions.rotated
import com.genir.aitweaks.utils.extensions.rotatedAroundPivot
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.getFacing
import org.lazywizard.lazylib.ext.isZeroVector
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
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

//    private fun avoidCollisions(expectedVelocity: Vector2f): Vector2f {
//        val obstacles = Global.getCombatEngine().ships.filter {
//            when {
//                it == ship -> false
//                it.owner != ship.owner -> false
//                it.isFighter -> false
//                else -> true
//            }
//        }
//
//        val r = Rotation(90f - expectedVelocity.getFacing())
//        val expectedRotated = expectedVelocity.rotated(r)
//
//        var avoidBack = 0f
//        var avoidBackOnly = 0f
//        var avoidLeft = 0f
//        var avoidRight = 0f
//
//        val dt = Global.getCombatEngine().elapsedInLastFrame
//
//        obstacles.forEach { obstacle ->
//
//            vMaxToObstacle(dt, obstacle)
//
//
//            val avoidVector = avoidCollision(expectedVelocity, obstacle)?.rotated(r) ?: return@forEach
//
//            // Avoid vector in case only braking is possible, not moving to the side.
//            val currentAvoidBackOnly = expectedRotated.y * (avoidVector.y / vectorProjection(expectedRotated, avoidVector).y)
//
//            avoidBackOnly = min(currentAvoidBackOnly, avoidBackOnly)
//            avoidBack = min(avoidVector.y, avoidBack)
//            avoidLeft = min(avoidVector.x, avoidLeft)
//            avoidRight = max(avoidVector.x, avoidRight)
//        }
//
//        // Left and right avoidance conflict. Avoid obstacle only by braking.
//        if (avoidLeft != 0f && avoidRight != 0f) {
//            avoidBack = min(avoidBackOnly, avoidBack)
//            avoidLeft = 0f
//            avoidRight = 0f
//        }
//
//        return expectedVelocity + Vector2f(avoidRight + avoidLeft, avoidBack).rotatedReverse(r)
//    }
//
//    private fun avoidCollision(expectedVelocity: Vector2f, obstacle: ShipAPI): Vector2f? {
//        val p = obstacle.location - ship.location
//        val distance = p.length() - (ship.collisionRadius + obstacle.collisionRadius + Preset.collisionBuffer)
//
//        // Too far to consider collision risk.
//        if (distance > 2000f) return null
//
//        // Transform time unit from second to frame of animation.
//        val dt = Global.getCombatEngine().elapsedInLastFrame
//
//        // Rotate to align with direction to obstacle.
//        val r = Rotation(90f - p.getFacing())
//
//        // Calculate velocity components towards obstacle.
//        val vShip = expectedVelocity.rotated(r).y
//        val vObstacle = obstacle.velocity.rotated(r).y * dt
//        val vCollision = vShip - vObstacle
//
//        // Distance between ship and obstacle is increasing.
//        if (vCollision < 0f) return null
//
//        // Ship is flying away from the obstacle.
//        if (vShip < 0f) return null
//
//        // Already colliding. Full stop.
//        if (distance <= 0f) {
////            debugVertex(ship.location, ship.location - (-Vector2f(0f, vShip).rotatedReverse(r)) / dt, Color.RED)
//
//            return -Vector2f(0f, vShip).rotatedReverse(r)
//        }
//
//        val aObstacle = accelerationTracker[obstacle].rotated(r).y * dt * dt
//        val vMax = engineController.vMax(distance, ship.deceleration * dt * dt + aObstacle) + vObstacle
//        val overSpeed = vShip - vMax
//
//        // No need to brake yet.
//        if (overSpeed <= 0f) return null
//
//        val speedExpected = max(0f, vShip - overSpeed)
//
////        debugVertex(ship.location, ship.location - (-Vector2f(0f, vShip).rotatedReverse(r) * (1f - (speedExpected / vShip))) / dt, Color.YELLOW)
//
//        return -Vector2f(0f, vShip).rotatedReverse(r) * (1f - (speedExpected / vShip))
//    }

//    data class Limit(val facing: Float, val speed: Float)

//    private fun avoidCollisions(dt: Float, expectedVelocity: Vector2f): Vector2f {
//        val obstacles = Global.getCombatEngine().ships.filter {
//            when {
//                it == ship -> false
//                it.owner != ship.owner -> false
//                it.isFighter -> false
//                else -> true
//            }
//        }
//
//        if (obstacles.isEmpty()) return expectedVelocity
//
//        val expectedSpeed = expectedVelocity.length()
//        val expectedFacing = expectedVelocity.getFacing()
//
//        val limits: MutableList<Limit> = mutableListOf()
//
//        var lowestLimit: Limit? = null
//        var lowestLimitSpeedAhead = Float.MAX_VALUE
//
//        obstacles.forEach { obstacle ->
//            val lim = vMaxToObstacle(dt, obstacle)
//
//            if (lim.speed > expectedSpeed) return@forEach
//
//            limits.add(lim)
//
//            val rDistance = abs(MathUtils.getShortestRotation(expectedFacing, lim.facing)) * DEGREES_TO_RADIANS
//            if (rDistance >= PI / 2f) return@forEach
//
//            val speedAhead = rDistance * rDistance + 1
//
//            if (speedAhead < lowestLimitSpeedAhead && speedAhead < expectedSpeed) {
//                lowestLimit = lim
//                lowestLimitSpeedAhead = speedAhead
//            }
//
//
//        }
//
//        val lim = lowestLimit ?: return expectedVelocity
//
//        val facingOffset = facingOffset(expectedSpeed, lim.speed)
//
//
//        val angleToLimit = MathUtils.getShortestRotation(expectedFacing, lim.facing)
//        val angleToNewFacing = angleToLimit - facingOffset * angleToLimit.sign
//        val newFacing = expectedFacing + angleToNewFacing
//
//        var newSpeed = expectedSpeed
//
//        limits.forEach { l ->
//            val x = abs(newFacing - l.facing) * DEGREES_TO_RADIANS
//            val t = (PI / 2f - x).toFloat()
//
//            val e1 = l.speed * (1f / t + t / 5f)
//
//            if (l.speed < 0.0001f) {
//                log("${ship.name}  $newFacing ${l.facing} $e1 ${l.speed} $x $t")
//            }
//
//            // Angle between speed limit and heading is more than 90 degrees.
//            // Speed limit has no effect on velocity.
//            if (t <= 0f) return@forEach
//
//            val e = l.speed * (1f / t + t / 5f)
//
//
//            newSpeed = min(e, newSpeed)
//        }
//
////        return expectedVelocity.rotated(Rotation(angleToNewFacing)).resized(cos(angleToNewFacing * DEGREES_TO_RADIANS))
//        return expectedVelocity.rotated(Rotation(angleToNewFacing)).resized(newSpeed)
//    }

    class Limit(val facing: Float, val speed: Float) {
        fun clampSpeed(expectedFacing: Float, expectedSpeed: Float): Float {
            val angleFromLimit = abs(MathUtils.getShortestRotation(expectedFacing, facing))
            if (angleFromLimit > 90f) return expectedSpeed

            val t = (PI / 2f - angleFromLimit * DEGREES_TO_RADIANS).toFloat()
            val e = speed * (1f / t + t / 5f)

            return min(e, expectedSpeed)
        }
    }

    private fun avoidCollisions(dt: Float, expectedVelocity: Vector2f): Vector2f {
        val obstacles = Global.getCombatEngine().ships.filter {
            when {
                it == ship -> false
                it.owner != ship.owner -> false
                it.isFighter -> false
                else -> true
            }
        }

        if (obstacles.isEmpty()) return expectedVelocity

        val expectedSpeed = expectedVelocity.length()
        val expectedFacing = expectedVelocity.getFacing()

        val limits: MutableList<Limit> = mutableListOf()

        var lowestLimit: Limit? = null
        var lowestLimitSpeedAhead = Float.MAX_VALUE

        obstacles.forEach { obstacle ->
            val lim = vMaxToObstacle(dt, obstacle)

            if (lim.speed > expectedSpeed) return@forEach

            limits.add(lim)

            val speedAhead = lim.clampSpeed(expectedFacing, expectedSpeed)

//            val rDistance = abs(MathUtils.getShortestRotation(expectedFacing, lim.facing)) * DEGREES_TO_RADIANS
//            if (rDistance >= PI / 2f) return@forEach
//
//             rDistance * rDistance + 1

            if (speedAhead < lowestLimitSpeedAhead && speedAhead < expectedSpeed) {
                lowestLimit = lim
                lowestLimitSpeedAhead = speedAhead
            }
        }


        val lim = lowestLimit ?: return expectedVelocity

        val facingOffset = facingOffset(expectedSpeed, lim.speed)


        val angleToLimit = MathUtils.getShortestRotation(expectedFacing, lim.facing)
        val angleToNewFacing = angleToLimit - facingOffset * angleToLimit.sign
        val newFacing = expectedFacing + angleToNewFacing

        var newSpeed = expectedSpeed

        limits.forEach { l ->
            val x = abs(MathUtils.getShortestRotation(newFacing, l.facing)) * DEGREES_TO_RADIANS
            val t = (PI / 2f - x).toFloat()

            val e1 = l.speed * (1f / t + t / 5f)

            if (l.speed < 0.0001f) {
                log("${ship.name}  $newFacing ${l.facing} $e1 ${l.speed} $x $t")
            }

            // Angle between speed limit and heading is more than 90 degrees.
            // Speed limit has no effect on velocity.
            if (t <= 0f) return@forEach

            val e = l.speed * (1f / t + t / 5f)


            newSpeed = min(e, newSpeed)
        }

//        return expectedVelocity.rotated(Rotation(angleToNewFacing)).resized(cos(angleToNewFacing * DEGREES_TO_RADIANS))
        return expectedVelocity.rotated(Rotation(angleToNewFacing)).resized(newSpeed)
    }

    private fun facingOffset(expected: Float, max: Float): Float {
        val a = 1f
        val b = -5f * (expected / max)
        val c = 5f

        val t = quad(a, b, c)!!.second
        return abs(t - PI / 2f).toFloat() * RADIANS_TO_DEGREES
    }

    private fun vMaxToObstacle(dt: Float, obstacle: ShipAPI): Limit {
        val fullStop = 0.00001f

        val direction = obstacle.location - ship.location
        val dAbs = direction.length()
        val distance = dAbs - (ship.collisionRadius + obstacle.collisionRadius + Preset.collisionBuffer)

        // Already colliding.
        if (distance <= 0f) {
            debugVertex(ship.location, ship.location + direction.resized(100f), Color.RED)
            return Limit(direction.getFacing(), fullStop)
        }

        val vObstacle = vectorProjection(obstacle.velocity, direction)
        val aObstacle = vectorProjection(accelerationTracker[obstacle], direction)

        val vAbs = (vObstacle + direction).length() - dAbs
        val aAbs = (aObstacle + direction).length() - dAbs
//        val aAbs = 0f

        val vMax = engineController.vMax(distance, (ship.deceleration + aAbs) * dt * dt) + vAbs * dt

//        debugVertex(ship.location, ship.location + direction.resized(vMax.coerceAtLeast(0f) / dt), Color.YELLOW)
        return Limit(direction.getFacing(), vMax.coerceAtLeast(fullStop))
    }
}


