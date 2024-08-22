package com.genir.aitweaks.core.debug

import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.loading.MissileSpecAPI
import com.genir.aitweaks.core.features.shipai.autofire.BallisticTarget
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.extensions.facing
import com.genir.aitweaks.core.utils.extensions.length
import com.genir.aitweaks.core.utils.extensions.resized
import com.genir.aitweaks.core.utils.extensions.rotated
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.max

//class Trail(dt: Float, weapon: WeaponAPI, private val missile: MissileAPI) {
//    val seq = simulateMissile(dt, weapon)!!.iterator()
//
//    fun advance(dt: Float) {
//        val frame = seq.next()
////        log(" ${missile.velocity} ${frame.velocity / dt} $dt")
////        log(" ${missile.location} ${frame.location}")
//        log("${missile.location} ${missile.velocity} ${frame.location} ${frame.velocity / dt}")
//
//
//        debugPrint["missile"] = "missile ${missile.location}"
//    }
//}

data class Frame(val velocity: Vector2f, val location: Vector2f)

fun missileIntercept(dt: Float, weapon: WeaponAPI, target: BallisticTarget): Vector2f {
    var facing: Float = (target.location - weapon.location).facing

    for (i in 0..3) {

        val path: List<Frame> = simulateMissile(dt, weapon, facing).toList()
        val error: Float = closestPoint(dt, weapon, target, path.iterator().asSequence())
//    val error: Float = MathUtils.getShortestRotation(facing, closest)
//        debugPrint[i] = "$error"
        facing += error

    }

//    var prev = path.firstOrNull()!!.location
//    path.forEach { frame ->
//        drawLine(prev, frame.location, BLUE)
//        prev = frame.location
//    }

//    debugPrint["facing"] = "facing ${facing}"

    return unitVector(facing) * (target.location - weapon.location).length + weapon.location
}

fun closestPoint(dt: Float, weapon: WeaponAPI, target: BallisticTarget, path: Sequence<Frame>): Float {
    val p0: Vector2f = target.location
    val v: Vector2f = target.velocity * dt

    var dMin: Float = Float.MAX_VALUE
//    var pMin = Vector2f()
    var fMin = 0f

    path.forEachIndexed { idx, frame ->
        val p = p0 + v * idx.toFloat()
        val d = (p - frame.location).length

        if (d < dMin) {
            dMin = d


//            pMin = p
            fMin = getShortestRotation(frame.location, weapon.location, p)
        }
    }

//    drawLine(fMin, pMin, YELLOW)

    return fMin
}

fun simulateMissile(dt: Float, weapon: WeaponAPI, facing: Float): Sequence<Frame> {
    val spec: MissileSpecAPI = weapon.spec.projectileSpec as MissileSpecAPI
    val p0: Vector2f = weapon.barrelLocation(facing)
    val vMax: Float = spec.hullSpec.engineSpec.maxSpeed * dt
    val facingVector: Vector2f = unitVector(facing)
    val v0: Vector2f = (weapon.ship.velocity + facingVector * spec.launchSpeed) * dt
    val a: Vector2f = facingVector * spec.hullSpec.engineSpec.acceleration * dt * dt
    val decel: Float = spec.hullSpec.engineSpec.acceleration * 2f * dt * dt

    return generateSequence(Frame(v0, p0)) {
        val v2: Vector2f = it.velocity + a
        val speed: Float = v2.length
        Frame(
            if (speed <= vMax) v2 else v2.resized(max(vMax, speed - decel)),
            it.location + it.velocity,
        )
    }.take((spec.maxFlightTime / dt).toInt())
}

fun WeaponAPI.barrelLocation(facing: Float): Vector2f {
    val offsets: List<Vector2f> = when {
        slot.isHardpoint -> spec.hardpointFireOffsets
        slot.isTurret -> spec.turretFireOffsets
        else -> listOf()
    }

    val sum: Vector2f = offsets.fold(Vector2f()) { sum, offset -> sum + offset }
    val average: Vector2f = sum / offsets.size.toFloat()

    return location + average.rotated(Rotation(facing))
}