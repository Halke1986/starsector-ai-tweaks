package com.genir.aitweaks.core.features.shipai.autofire

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.*
import com.genir.aitweaks.core.state.combatState
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.extensions.*
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs

private const val alsoTargetFighters = true

class UpdateTarget(
    private val weapon: WeaponAPI,
    private val current: CombatEntityAPI?,
    private val attackTarget: ShipAPI?,
    private val params: BallisticParams,
) {
    val target: CombatEntityAPI? = when {
        Global.getCurrentState() == GameState.TITLE && combatState.titleScreenFireIsOn -> selectAsteroid()

        // Obligatory PD
        weapon.hasAIHint(PD_ONLY) && weapon.hasAIHint(ANTI_FTR) -> selectFighter() ?: selectMissile()
        weapon.hasAIHint(PD_ONLY) -> selectMissile() ?: selectFighter()

        // PD
        weapon.hasAIHint(PD) && weapon.hasAIHint(ANTI_FTR) -> selectFighter() ?: selectMissile() ?: selectShip()
        weapon.hasAIHint(PD) -> selectMissile() ?: selectFighter() ?: selectShip()

        // Main weapons
        weapon.hasAIHint(ANTI_FTR) -> selectShip(alsoTargetFighters)
        weapon.hasAIHint(STRIKE) -> selectShip()
        weapon.ship.hullSpec.hullId.startsWith("guardian") -> selectShip()
        weapon.ship.customShipAI != null -> selectShip() // Custom AI doesn't attack fighters with main weapons, for now.

        else -> selectShip() ?: selectFighter()?.let { if (!it.isSupportFighter) it else null }
    }

    /** Target asteroid selection. Selects asteroid only when the weapon and asteroid
     * are both in viewport. Otherwise, it looks weird on the title screen. */
    private fun selectAsteroid(): CombatEntityAPI? {
        val inViewport = { location: Vector2f -> Global.getCombatEngine().viewport.isNearViewport(location, 0f) }
        if (!inViewport(weapon.location)) return null
        return selectEntity(asteroidGrid()) { it is CombatAsteroidAPI && inViewport(it.location) }
    }

    private fun selectMissile(): CombatEntityAPI? {
        return selectEntity(missileGrid()) { it is MissileAPI && (!it.isFlare || !weapon.ignoresFlares) }
    }

    private fun selectFighter(): CombatEntityAPI? {
        return selectEntity(shipGrid()) { it is ShipAPI && it.isFighter }
    }

    private fun selectShip(alsoFighter: Boolean = false): CombatEntityAPI? {
        val priorityTarget: ShipAPI? = attackTarget

        // Try to follow the ship attack target.
        val selectPriorityTarget = when {
            priorityTarget == null -> false

            !priorityTarget.isValidTarget -> false

            // Don't attack allies.
            priorityTarget.owner == weapon.ship.owner -> false

            // Hardpoint weapons select ship target even when it's outside their firing arc.
            weapon.slot.isHardpoint -> true

            // Turreted weapons select ship target if it can be tracked.
            canTrack(weapon, BallisticTarget.entity(priorityTarget), params) -> true

            else -> false
        }

        if (selectPriorityTarget) return priorityTarget

        // Select alternative target.
        return selectEntity(shipGrid()) { it is ShipAPI && (!it.isFighter || alsoFighter) }
    }

    private fun selectEntity(grid: CollisionGridAPI, isTargetAcceptable: (CombatEntityAPI) -> Boolean): CombatEntityAPI? {
        val approve = { it: CombatEntityAPI? -> it != null && isTargetAcceptable(it) && isTargetAcceptableGeneric(it) }

        // Try tracking the current target.
        if (approve(current)) return current

        // Find the closest enemy entity that can be tracked by the weapon.
        return closestEntityFinder(weapon.location, weapon.totalRange, grid) {
            if (!approve(it)) return@closestEntityFinder null

            // Evaluate the target based on angle and distance.
            val target = BallisticTarget.entity(it)
            val angle = shortestRotation((target.location - weapon.location).facing, weapon.currAngle) * DEGREES_TO_RADIANS
            val angleWeight = 0.75f
            val evalAngle = abs(angle) * angleWeight

            // Prioritize closer targets. Avoid attacking targets out of effective weapons range.
            val dist = interceptRelative(weapon, target, params).length
            val evalDist = (dist / weapon.totalRange)

            Pair(evalAngle + evalDist, it)
        } as? CombatEntityAPI
    }

    private fun isTargetAcceptableGeneric(target: CombatEntityAPI): Boolean {
        val ballisticTarget = BallisticTarget.entity(target)

        return when {
            !target.isValidTarget -> false
            target.owner == weapon.ship.owner -> false

            !canTrack(weapon, ballisticTarget, params) -> false

            // Do not track targets occluded by obstacles.
            else -> {
                val intercept = interceptRelative(weapon, ballisticTarget, params)

                getObstacleList().none { obstacle ->
                    when {
                        obstacle.origin == target -> false
                        !obstacle.arc.contains(intercept.facing) -> false
                        obstacle.dist > intercept.length -> false

                        else -> true
                    }
                }
            }
        }
    }

    private fun getObstacleList(): List<Obstacle> {
        if (obstacleList == null)
            obstacleList = makeObstacleList()

        return obstacleList!!
    }

    private var obstacleList: List<Obstacle>? = null

    private data class Obstacle(val arc: Arc, val dist: Float, val origin: CombatEntityAPI)

    private fun makeObstacleList(): List<Obstacle> {
        val radius = weapon.totalRange
        val ships = shipGrid().getCheckIterator(weapon.location, radius * 2.0f, radius * 2.0f).asSequence()
        val obstacles = ships.filterIsInstance<ShipAPI>().filter { it.root != weapon.ship.root && !it.isFighter }

        return obstacles.map { obstacle ->
            val target = BallisticTarget(obstacle.velocity, obstacle.location, obstacle.boundsRadius * 0.8f)
            val dist = interceptRelative(weapon, target, params).length
            val arc = interceptArc(weapon, target, params)

            Obstacle(arc, dist, obstacle)
        }.toList()
    }
}
