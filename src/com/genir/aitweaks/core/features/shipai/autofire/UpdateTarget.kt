package com.genir.aitweaks.core.features.shipai.autofire

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.*
import com.genir.aitweaks.core.state.State.Companion.state
import com.genir.aitweaks.core.utils.Arc
import com.genir.aitweaks.core.utils.DEGREES_TO_RADIANS
import com.genir.aitweaks.core.utils.Grid
import com.genir.aitweaks.core.utils.extensions.*
import com.genir.aitweaks.core.utils.shortestRotation
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs

private const val alsoTargetFighters = true

class UpdateTarget(
    private val weapon: WeaponAPI,
    private val current: CombatEntityAPI?,
    private val attackTarget: ShipAPI?,
    private val params: BallisticParams,
) {
    fun target(): CombatEntityAPI? = when {
        Global.getCurrentState() == GameState.TITLE && state.config.enableTitleScreenFire -> selectAsteroid()

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

        else -> selectShip() ?: selectFighter()?.let { if (!it.isSupportFighter) it else null }
    }

    /** Target asteroid selection. Selects asteroid only when the weapon and asteroid
     * are both in viewport. Otherwise, it looks weird on the title screen. */
    private fun selectAsteroid(): CombatEntityAPI? {
        val inViewport = { location: Vector2f -> Global.getCombatEngine().viewport.isNearViewport(location, 0f) }
        if (!inViewport(weapon.location)) return null
        return selectEntity(CombatAsteroidAPI::class.java) { inViewport(it.location) }
    }

    private fun selectMissile(): CombatEntityAPI? {
        return selectEntity(MissileAPI::class.java) { it is MissileAPI && (!it.isFlare || !weapon.ignoresFlares) }
    }

    private fun selectFighter(): CombatEntityAPI? {
        return selectEntity(ShipAPI::class.java) { it.isFighter }
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
        return selectEntity(ShipAPI::class.java) { !it.isFighter || alsoFighter }
    }

    private fun selectEntity(c: Class<*>, filter: (CombatEntityAPI) -> Boolean): CombatEntityAPI? {
        // Try tracking the current target.
        if (current != null && c.isInstance(current) && filter(current) && isTargetAcceptable(current)) return current

        val opportunities = Grid.entities(c, weapon.location, weapon.totalRange)
        val evaluated = opportunities.filter { filter(it) && isTargetAcceptable(it) }.map {
            // Evaluate the target based on angle and distance.
            val target = BallisticTarget.entity(it)
            val angle = shortestRotation((target.location - weapon.location).facing, weapon.currAngle) * DEGREES_TO_RADIANS
            val angleWeight = 0.75f
            val evalAngle = abs(angle) * angleWeight

            // Prioritize closer targets. Avoid attacking targets out of effective weapons range.
            val dist = intercept(weapon, target, params).length
            val evalDist = (dist / weapon.totalRange)

            Pair(it, evalAngle + evalDist)
        }

        return evaluated.minWithOrNull(compareBy { it.second })?.first
    }

    private fun isTargetAcceptable(target: CombatEntityAPI): Boolean {
        val ballisticTarget = BallisticTarget.entity(target)

        return when {
            !target.isValidTarget -> false
            target.owner == weapon.ship.owner -> false

            !canTrack(weapon, ballisticTarget, params) -> false

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
        val obstacles = Grid.ships(weapon.location, weapon.totalRange).filter {
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
            val target = BallisticTarget(obstacle.location, obstacle.velocity, state.bounds.radius(obstacle) * 0.8f)
            val dist = intercept(weapon, target, params).length
            val arc = interceptArc(weapon, target, params)

            Obstacle(arc, dist, obstacle)
        }.toList()
    }
}
