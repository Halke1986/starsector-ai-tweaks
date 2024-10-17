package com.genir.aitweaks.core.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.utils.extensions.facing
import com.genir.aitweaks.core.utils.extensions.lengthSquared
import org.json.JSONObject
import org.lazywizard.lazylib.MathUtils.getShortestRotation
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.PI
import kotlin.math.abs

// TODO remove and use ballistics implementation
fun willHitShield(weapon: WeaponAPI, target: ShipAPI?) = when {
    target == null -> false
    target.shield == null -> false
    target.shield.isOff -> false
    else -> willHitActiveShieldArc(weapon, target.shield)
}

// TODO remove and use ballistics implementation
fun willHitActiveShieldArc(weapon: WeaponAPI, shield: ShieldAPI): Boolean {
    val tgtFacing = (weapon.location - shield.location).facing
    val attackAngle = getShortestRotation(tgtFacing, shield.facing)
    return abs(attackAngle) < (shield.activeArc / 2)
}

fun shieldUptime(shield: ShieldAPI?): Float {
    if (shield == null) return 0f
    val r = shield.activeArc / shield.arc
    return if (r >= 1f) Float.MAX_VALUE
    else r * shield.unfoldTime
}

internal infix operator fun Vector2f.times(d: Float): Vector2f = Vector2f(x * d, y * d)
internal infix operator fun Vector2f.div(d: Float): Vector2f = Vector2f(x / d, y / d)

fun rotateAroundPivot(toRotate: Vector2f, pivot: Vector2f, angle: Float): Vector2f = VectorUtils.rotateAroundPivot(toRotate, pivot, angle, Vector2f())

fun unitVector(angle: Float): Vector2f = VectorUtils.rotate(Vector2f(1f, 0f), angle)


class Log

fun log(message: Any) = Global.getLogger(Log().javaClass).info(message)

class Rotation(angle: Float) {
    private val radians = angle / 180.0f * PI.toFloat()
    private val sin = kotlin.math.sin(radians)
    private val cos = kotlin.math.cos(radians)

    fun rotate(v: Vector2f) = Vector2f(v.x * cos - v.y * sin, v.x * sin + v.y * cos)

    fun reverse(v: Vector2f) = Vector2f(v.x * cos + v.y * sin, -v.x * sin + v.y * cos)
}

fun defaultAIInterval() = Interval(0.25f, 0.33f)

class RollingAverageVector(private val historySize: Int) {
    private var history: MutableList<Vector2f> = mutableListOf()
    private var sum: Vector2f = Vector2f()

    fun update(v: Vector2f): Vector2f {
        history.add(Vector2f(v))
        sum = sum + v
        if (history.size > historySize) {
            sum -= history.first()
            history.removeFirst()
        }

        return sum / history.size.toFloat()
    }

    fun clear() {
        history.clear()
        sum = Vector2f()
    }
}

class RollingAverageFloat(private val historySize: Int) {
    private var history: MutableList<Float> = mutableListOf()
    private var sum = 0f

    fun update(v: Float): Float {
        history.add(v)
        sum += v
        if (history.size > historySize) {
            sum -= history.first()
            history.removeFirst()
        }

        return sum / history.size.toFloat()
    }

    fun clear() {
        history.clear()
        sum = 0f
    }
}

inline fun <reified T : Enum<T>> loadEnum(json: JSONObject, fieldName: String): T? {
    val value: String = json.optString(fieldName)
    if (value == "") return null

    return try {
        enumValueOf<T>(value)
    } catch (_: IllegalArgumentException) {
        null
    }
}

fun getShortestRotation(from: Vector2f, to: Vector2f): Float {
    return getShortestRotation(from.facing, to.facing)
}

fun getShortestRotation(from: Vector2f, pivot: Vector2f, to: Vector2f): Float {
    return getShortestRotation((from - pivot).facing, (to - pivot).facing)
}

fun mousePosition(): Vector2f {
    return Vector2f(
        Global.getCombatEngine().viewport.convertScreenXToWorldX(Global.getSettings().mouseX.toFloat()),
        Global.getCombatEngine().viewport.convertScreenYToWorldY(Global.getSettings().mouseY.toFloat()),
    )
}

inline fun <reified T> closestEntity(entities: Collection<CombatEntityAPI>, p: Vector2f): T? {
    return entities.minByOrNull { (it.location - p).lengthSquared } as? T
}

/**
 * Calculates the average facing direction by converting facings into unit vectors
 * and averaging the resulting vectors. Returns the facing of the average vector.
 * This approach avoids issues with angles behaving as modular values in Starsector.
 */
fun averageFacing(facings: Collection<Float>): Float {
    return facings.fold(Vector2f()) { sum, f -> sum + unitVector(f) }.facing
}