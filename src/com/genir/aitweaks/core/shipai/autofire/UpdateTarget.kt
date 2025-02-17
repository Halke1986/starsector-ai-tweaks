package com.genir.aitweaks.core.shipai.autofire

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.*
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.state.Config.Companion.config
import com.genir.aitweaks.core.utils.Arc
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import com.genir.aitweaks.core.utils.Grid
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs

class UpdateTarget(
    private val weapon: WeaponAPI,
    private val current: CombatEntityAPI?,
    private val attackTarget: ShipAPI?,
    private val params: BallisticParams,
) {
    companion object {
        private const val ALSO_TARGET_FIGHTERS = true
        const val TARGET_SEARCH_MULT = 1.5f
    }

    // Search within twice weapon.totalRange to account for projectile flight time,
    // allowing attacks to start before the target enters maximum range.
    private val targetSearchRange = weapon.totalRange * TARGET_SEARCH_MULT

    fun target(): CombatEntityAPI? = when {
        Global.getCurrentState() == GameState.TITLE && config.enableTitleScreenFire -> selectAsteroid()

        // Obligatory PD
        weapon.hasAIHint(PD_ONLY) && weapon.isAntiFighter -> selectTarget(::selectFighter, ::selectMissile)
        weapon.hasAIHint(PD_ONLY) -> selectTarget(::selectMissile, ::selectFighter)

        // PD
        weapon.hasAIHint(PD) && weapon.isAntiFighter -> selectTarget(::selectFighter, ::selectMissile, ::selectShip)
        weapon.hasAIHint(PD) -> selectTarget(::selectMissile, ::selectFighter, ::selectShip)

        // Main weapons
        weapon.isAntiFighter -> selectTarget(::selectShipOrFighter)
        weapon.hasAIHint(STRIKE) || weapon.isFinisherBeam -> selectTarget(::selectShip)
        weapon.ship.hullSpec.hullId.startsWith("guardian") -> selectTarget(::selectShip)

        // Default main weapon.
        else -> selectTarget(::selectShip, ::selectNonSupportFighter)
    }

    private fun selectTarget(vararg selectors: () -> CombatEntityAPI?): CombatEntityAPI? {
        var outOfRangeTarget: CombatEntityAPI? = null

        selectors.forEach { selector ->
            val target = selector()

            if (target != null) {
                val ballisticTarget = BallisticTarget.collisionRadius(target)
                val dist = intercept(weapon, ballisticTarget, params).length
                val range = weapon.totalRange

                // New in-range target found.
                if (dist <= range) {
                    return target
                }

                // Special case for hardpoints, which should target ship target even when it's out of range.
                if (weapon.slot.isHardpoint && !weapon.ship.engineController.isFlamedOut && target == attackTarget) {
                    return target
                }

                // Stash potential out of range target.
                if (outOfRangeTarget == null) {
                    outOfRangeTarget = target
                }
            }
        }

        return outOfRangeTarget
    }

    /** Target asteroid selection. Selects asteroid only when the weapon and asteroid
     * are both in viewport. Otherwise, it looks weird on the title screen. */
    private fun selectAsteroid(): CombatEntityAPI? {
        val inViewport = { location: Vector2f -> Global.getCombatEngine().viewport.isNearViewport(location, 0f) }
        if (!inViewport(weapon.location)) {
            return null
        }

        return selectEntity(CombatAsteroidAPI::class.java) { inViewport(it.location) }
    }

    private fun selectMissile(): CombatEntityAPI? {
        return selectEntity(MissileAPI::class.java) { it is MissileAPI && (!it.isFlare || !weapon.ignoresFlares) }
    }

    private fun selectFighter(): CombatEntityAPI? {
        return selectEntity(ShipAPI::class.java) { it.isFighter }
    }

    private fun selectNonSupportFighter(): CombatEntityAPI? {
        return selectEntity(ShipAPI::class.java) { it.isFighter && !it.isSupportFighter }
    }

    private fun selectShipOrFighter(): CombatEntityAPI? {
        return selectShip(ALSO_TARGET_FIGHTERS)
    }

    private fun selectShip(alsoFighter: Boolean = false): CombatEntityAPI? {
        // Try to follow the ship attack target.
        val priorityTarget: ShipAPI? = attackTarget
        when {
            priorityTarget == null -> Unit

            !priorityTarget.isValidTarget -> Unit

            // Don't attack allies.
            priorityTarget.owner == weapon.ship.owner -> Unit

            // Hardpoint weapons select ship target even when it's outside their firing arc,
            // unless the ship is incapable of following the target.
            weapon.slot.isHardpoint && !weapon.ship.engineController.isFlamedOut -> return priorityTarget

            // Turreted weapons select ship target if it can be tracked.
            canTrack(weapon, BallisticTarget.collisionRadius(priorityTarget), params) -> return priorityTarget
        }

        // Select alternative target.
        return selectEntity(ShipAPI::class.java) { !it.isFighter || alsoFighter }
    }

    private fun selectEntity(c: Class<*>, entityFilter: (CombatEntityAPI) -> Boolean): CombatEntityAPI? {
        // Try tracking the current target.
        if (current != null && c.isInstance(current) && entityFilter(current) && isTargetAcceptable(current, weapon.totalRange)) {
            return current
        }

        val opportunities = Grid.entities(c, weapon.location, targetSearchRange)
        val evaluated = opportunities.filter { entityFilter(it) && isTargetAcceptable(it, targetSearchRange) }.map {
            val target = BallisticTarget.collisionRadius(it)
            val dist = intercept(weapon, target, params).length
            val range = weapon.totalRange

            return@map if (dist <= range) {
                // Evaluate the target based on angle and distance.
                val angle = ((target.location - weapon.location).facing - weapon.currAngle.direction).radians
                val angleWeight = 0.75f

                val evalAngle = abs(angle) * angleWeight
                val evalDist = dist / range

                Pair(it, evalAngle + evalDist)
            } else {
                // If the target is out of range, evaluate it based only on range, ignoring the angle.
                val outOfRangePenalty = 1e3f

                val evalDist = (dist - range) / range
                Pair(it, evalDist + outOfRangePenalty)
            }
        }

        return evaluated.minWithOrNull(compareBy { it.second })?.first
    }

    private fun isTargetAcceptable(target: CombatEntityAPI, searchRange: Float): Boolean {
        val ballisticTarget = BallisticTarget.collisionRadius(target)

        return when {
            !target.isValidTarget -> false
            target.owner == weapon.ship.owner -> false

            !canTrack(weapon, ballisticTarget, params, searchRange) -> false

            // Do not track targets occluded by obstacles.
            else -> {
                val intercept = intercept(weapon, ballisticTarget, params)

                obstacleList.none { obstacle ->
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

    private data class Obstacle(val arc: Arc, val dist: Float, val origin: CombatEntityAPI)

    private val obstacleList: List<Obstacle> by lazy {
        val obstacles = Grid.ships(weapon.location, targetSearchRange).filter {
            when {
                // Same ship.
                it.root == weapon.ship.root -> false

                it.isFighter -> false

                // Weapon fires over allies.
                weapon.noFF && it.owner == weapon.ship.owner -> false

                else -> true
            }
        }

        obstacles.map { obstacle ->
            val target = BallisticTarget(obstacle.location, obstacle.velocity, obstacle.boundsRadius * 0.8f)
            val dist = intercept(weapon, target, params).length
            val arc = interceptArc(weapon, target, params)

            Obstacle(arc, dist, obstacle)
        }.toList()
    }
}
