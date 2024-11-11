package com.genir.aitweaks.core.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.Obfuscated
import com.genir.aitweaks.core.utils.extensions.facing
import com.genir.aitweaks.core.utils.extensions.lengthSquared
import org.json.JSONObject
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f

fun shieldUptime(shield: ShieldAPI?): Float {
    if (shield == null) return 0f
    val r = shield.activeArc / shield.arc
    return if (r >= 1f) Float.MAX_VALUE
    else r * shield.unfoldTime
}

internal infix operator fun Vector2f.times(d: Float): Vector2f = Vector2f(x * d, y * d)
internal infix operator fun Vector2f.div(d: Float): Vector2f = Vector2f(x / d, y / d)

class Log

fun log(message: Any) = Global.getLogger(Log().javaClass).info(message)

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
    return shortestRotation(from.facing, to.facing)
}

fun getShortestRotation(from: Vector2f, pivot: Vector2f, to: Vector2f): Float {
    return shortestRotation((from - pivot).facing, (to - pivot).facing)
}

fun mousePosition(): Vector2f {
    val viewport = Global.getCombatEngine().viewport
    val settings = Global.getSettings()

    return Vector2f(
        viewport.convertScreenXToWorldX(settings.mouseX.toFloat()),
        viewport.convertScreenYToWorldY(settings.mouseY.toFloat()),
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

/** Remove ship commands issued by AI or the player. Needs to be executed before Ship.advance() to take effect.*/
fun clearVanillaCommands(ship: ShipAPI, vararg commands: VanillaShipCommand) {
    val commandWrappers: MutableIterator<Obfuscated.ShipCommandWrapper> = (ship as Obfuscated.Ship).commands.iterator()
    while (commandWrappers.hasNext()) {
        val command: Obfuscated.ShipCommand = commandWrappers.next().shipCommandWrapper_getCommand

        if (commands.any { command == Obfuscated.ShipCommand.valueOf(it.name) }) {
            commandWrappers.remove()
        }

//        if (commands.contains(command.name)) commandWrappers.remove()
    }
}

enum class VanillaShipCommand {
    TURN_LEFT,
    TURN_RIGHT,
    STRAFE_LEFT,
    STRAFE_RIGHT,
    ACCELERATE,
    ACCELERATE_BACKWARDS,
}
