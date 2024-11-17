package com.genir.aitweaks.core.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.genir.aitweaks.core.Obfuscated
import com.genir.aitweaks.core.features.shipai.autofire.BallisticParams
import com.genir.aitweaks.core.features.shipai.autofire.Hit
import com.genir.aitweaks.core.features.shipai.autofire.analyzeAllyHit
import com.genir.aitweaks.core.features.shipai.autofire.analyzeHit
import com.genir.aitweaks.core.utils.extensions.*
import org.json.JSONObject

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

fun firstShipAlongLineOfFire(weapon: WeaponAPI, params: BallisticParams): Hit? {
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
        if (ship.owner == weapon.ship.owner) analyzeAllyHit(weapon, ship, params)
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
    }
}

enum class VanillaShipCommand {
    TURN_LEFT,
    TURN_RIGHT,
    STRAFE_LEFT,
    STRAFE_RIGHT,
    ACCELERATE,
    ACCELERATE_BACKWARDS,
    TOGGLE_SHIELD,
}

fun makeAIDrone(ai: ShipAIPlugin): ShipAPI {
    val spec = Global.getSettings().getHullSpec("dem_drone")
    val v = Global.getSettings().createEmptyVariant("dem_drone", spec)
    val aiDrone = Global.getCombatEngine().createFXDrone(v)

    aiDrone.owner = 0
    aiDrone.mutableStats.hullDamageTakenMult.modifyMult("aitweaks_ai_drone", 0f) // so it's non-targetable
    aiDrone.isDrone = true
    aiDrone.collisionClass = CollisionClass.NONE
    aiDrone.location.y = -1e7f

    aiDrone.shipAI = ai

    return aiDrone
}
