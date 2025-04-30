package com.genir.aitweaks.core.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.debug.Debug
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.Preset
import com.genir.aitweaks.core.shipai.autofire.BallisticParams
import com.genir.aitweaks.core.shipai.autofire.Hit
import com.genir.aitweaks.core.shipai.autofire.analyzeAllyHit
import com.genir.aitweaks.core.shipai.autofire.analyzeHit
import com.genir.aitweaks.core.utils.types.Direction
import org.json.JSONObject
import org.lwjgl.util.vector.Vector2f
import kotlin.math.max

fun shieldUptime(shield: ShieldAPI?): Float {
    if (shield == null) return 0f
    val r = shield.activeArc / shield.arc
    return if (r >= 1f) Float.MAX_VALUE
    else r * shield.unfoldTime
}

class Log

fun log(message: Any) = Global.getLogger(Log::class.java).info(message)

fun defaultAIInterval() = IntervalUtil(0.25f, 0.33f)

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

fun getShortestRotation(from: Vector2f, to: Vector2f): Direction {
    return to.facing - from.facing
}

fun getShortestRotation(from: Vector2f, pivot: Vector2f, to: Vector2f): Direction {
    return (to - pivot).facing - (from - pivot).facing
}

fun firstShipAlongLineOfFire(weapon: WeaponAPI, target: CombatEntityAPI, params: BallisticParams): Hit? {
    val obstacles = Grid.ships(weapon.location, weapon.totalRange).filter { ship ->
        when {
            ship.isFighter -> false
            ship.isExpired -> false
            ship == weapon.ship -> false
            weapon.ship.root == ship.root -> false

            ship.owner == weapon.ship.owner -> true
            ship.isPhased -> false
            else -> true
        }
    }

    val evaluated = obstacles.mapNotNull { ship ->
        if (ship.owner == weapon.ship.owner) analyzeAllyHit(weapon, target, ship, params)
        else analyzeHit(weapon, ship, params)
    }

    return evaluated.minWithOrNull(compareBy { it.range })
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
    return entities.minByOrNull { (it.location - p).lengthSquared - it.boundsRadius } as? T
}

inline fun <reified T> closestEntity(entities: Sequence<CombatEntityAPI>, p: Vector2f): T? {
    return entities.minByOrNull { (it.location - p).lengthSquared - it.boundsRadius } as? T
}

/**
 * Calculates the average facing direction by converting facings into unit vectors
 * and averaging the resulting vectors. Returns the facing of the average vector.
 * This approach avoids issues with angles behaving as modular values in Starsector.
 */
fun averageFacing(facings: Collection<Direction>): Direction {
    return facings.fold(Vector2f()) { sum, f -> sum + f.unitVector }.facing
}

/** Remove ship commands issued by AI or the player. Needs to be executed before Ship.advance() to take effect.*/
fun clearVanillaCommands(ship: ShipAPI, vararg commands: VanillaShipCommand) {
    val commandWrappers: MutableIterator<Ship.CommandWrapper> = (ship as Ship).commands.iterator()
    while (commandWrappers.hasNext()) {
        val command: Ship.Command = commandWrappers.next().shipCommandWrapper_command

        if (commands.any { command == it.obfuscated }) {
            commandWrappers.remove()
        }
    }
}

enum class VanillaShipCommand {
    TURN_LEFT,
    TURN_RIGHT,
    STRAFE_LEFT,
    STRAFE_RIGHT,
    ACCELERATE,
    ACCELERATE_BACKWARDS,
    DECELERATE,
    TOGGLE_SHIELD;

    val obfuscated = Ship.Command.valueOf(name)
}

fun isCloseToEnemy(ship: ShipAPI, enemy: ShipAPI): Boolean {
    val maxRange = max(max(Preset.threatSearchRange, ship.maxRange * 2f), enemy.maxRange)
    return (ship.location - enemy.location).lengthSquared <= maxRange * maxRange
}

fun sqrt(x: Float): Float {
    val result = kotlin.math.sqrt(x)

    if (result.isNaN()) {
        Debug.print["NaN"] = "NaN"
        val e = Exception("NaN")
        Global.getLogger(Log::class.java).error(e, e)
    }

    return result
}
