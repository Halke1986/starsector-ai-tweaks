package com.genir.aitweaks.core.shipai.autofire

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatAsteroidAPI
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.*
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.state.Config.Companion.config
import com.genir.aitweaks.core.utils.Grid
import com.genir.aitweaks.core.utils.types.Direction.Companion.direction
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs

class UpdateTarget(
    private val weapon: WeaponHandle,
    private val current: CombatEntityAPI?,
    private val attackTarget: ShipAPI?,
    private val params: BallisticParams,
) {
    companion object {
        const val TARGET_SEARCH_MULT = 1.5f
    }

    // Search within twice weapon.totalRange to account for projectile flight time,
    // allowing attacks to start before the target enters maximum range.
    private val targetSearchRange = weapon.totalRange * TARGET_SEARCH_MULT

    private val obstacleList by lazy {
        ObstacleList(weapon, targetSearchRange, params)
    }

    fun target(): CombatEntityAPI? = when {
        Global.getCurrentState() == GameState.TITLE && config.enableTitleScreenFire -> selectAsteroid()

        // Obligatory PD
        weapon.hasAIHint(PD_ONLY) -> selectTarget(::selectMissile, ::selectFighter)

        // PD
        weapon.hasAIHint(PD) -> selectTarget(::selectMissile, ::selectFighter, this::selectShip)

        // Main weapons
        weapon.isAntiFighter -> selectTarget(::selectShipOrFighter)
        weapon.hasAIHint(STRIKE) || weapon.isFinisherBeam -> selectTarget(this::selectShip)
        weapon.ship.hullSpec.hullId.startsWith("guardian") -> selectTarget(this::selectShip)

        // Default main weapon.
        else -> selectTarget(this::selectShip, ::selectNonSupportFighter)
    }

    private fun selectTarget(vararg selectors: () -> CombatEntityAPI?): CombatEntityAPI? {
        var outOfRangeTarget: CombatEntityAPI? = null

        selectors.forEach { selector ->
            val target = selector()

            if (target != null) {
                val ballisticTarget = BallisticTarget.collisionRadius(target)
                val dist = closestHitRange(weapon, ballisticTarget, params)
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

    private fun selectShip(): CombatEntityAPI? {
        return selectShipWithPriority { !it.isFighter }
    }

    private fun selectShipOrFighter(): CombatEntityAPI? {
        return selectShipWithPriority { true }
    }

    private fun selectFighter(): CombatEntityAPI? {
        return selectShip { it.isFighter }
    }

    private fun selectNonSupportFighter(): CombatEntityAPI? {
        return selectShip { it.isFighter && !it.isSupportFighter }
    }

    private fun selectShipWithPriority(shipTypeFilter: ((ShipAPI) -> Boolean)): ShipAPI? {
        if (!weapon.isStrictlyAntiArmor || weapon.slot.isHardpoint) {
            return selectShip(shipTypeFilter)
        }

        val selected = selectTarget(
            { selectShip { ship -> shipTypeFilter(ship) && !disqualifyLowPriorityTargets(ship) } },
            { selectShip(shipTypeFilter) },
        )

        return selected as? ShipAPI
    }

    private fun selectShip(shipTypeFilter: ((ShipAPI) -> Boolean)): ShipAPI? {
        val isTargetAcceptable = fun(target: CombatEntityAPI?, range: Float): Boolean {
            return when {
                target !is ShipAPI -> false

                !target.isValidTarget -> false

                target.owner == weapon.ship.owner -> false

                !shipTypeFilter(target) -> false

                // Hardpoint weapons select ship target even when it's outside their firing arc,
                // unless the ship is incapable of following the target.
                weapon.slot.isHardpoint && !weapon.ship.isFlamedOut && attackTarget != null -> {
                    target == attackTarget
                }

                !canTrack(weapon, BallisticTarget.collisionRadius(target), params, range) -> false

                target != attackTarget && obstacleList.isOccluded(target) -> false

                else -> true
            }
        }

        val ships: Sequence<ShipAPI> = Grid.ships(weapon.location, targetSearchRange)

        return selectEntity(ships, isTargetAcceptable) as? ShipAPI
    }

    private fun disqualifyLowPriorityTargets(target: ShipAPI): Boolean {
        val idealHit by lazy {
            estimateIdealHit(weapon, target, params)
        }

        return when {
            !weapon.isStrictlyAntiArmor -> false

            !target.isShip -> false

            idealHit.range > weapon.totalRange -> true

            idealHit.type == Hit.Type.SHIELD -> true

            else -> false
        }
    }

    private fun selectMissile(): MissileAPI? {
        val isTargetAcceptable = fun(target: CombatEntityAPI, range: Float): Boolean {
            return when {
                target !is MissileAPI -> false

                !target.isValidTarget -> false

                target.owner == weapon.ship.owner -> false

                target.isFlare && weapon.ignoresFlares -> false

                !canTrack(weapon, BallisticTarget.collisionRadius(target), params, range) -> false

                obstacleList.isOccluded(target) -> false

                else -> true
            }
        }

        val missiles: Sequence<MissileAPI> = Grid.missiles(weapon.location, targetSearchRange)

        return selectEntity(missiles, isTargetAcceptable) as? MissileAPI
    }

    /** Target asteroid selection. Selects asteroid only when the weapon and asteroid
     * are both in viewport. Otherwise, it looks weird on the title screen. */
    private fun selectAsteroid(): CombatAsteroidAPI? {
        fun inViewport(location: Vector2f): Boolean {
            return Global.getCombatEngine().viewport.isNearViewport(location, 0f)
        }

        if (!inViewport(weapon.location)) {
            return null
        }

        val isTargetAcceptable = fun(target: CombatEntityAPI, range: Float): Boolean {
            return when {
                target !is CombatAsteroidAPI -> false

                !target.isValidTarget -> false

                !canTrack(weapon, BallisticTarget.collisionRadius(target), params, range) -> false

                !inViewport(target.location) -> false

                obstacleList.isOccluded(target) -> false

                else -> true
            }
        }

        val asteroids: Sequence<CombatAsteroidAPI> = Grid.asteroids(weapon.location, targetSearchRange)

        return selectEntity(asteroids, isTargetAcceptable) as? CombatAsteroidAPI
    }

    private fun selectEntity(
        entities: Sequence<CombatEntityAPI>,
        isTargetAcceptable: ((CombatEntityAPI, Float) -> Boolean),
    ): CombatEntityAPI? {
        // Primary target takes priority.
        if (attackTarget != null && isTargetAcceptable(attackTarget, weapon.totalRange)) {
            return attackTarget
        }

        // Try tracking the current target.
        if (current != null && isTargetAcceptable(current, weapon.totalRange)) {
            return current
        }

        val opportunities: Sequence<CombatEntityAPI> = entities.filter { target ->
            isTargetAcceptable(target, targetSearchRange)
        }

        val evaluated: Sequence<Pair<CombatEntityAPI, Float>> = opportunities.map { opportunity ->
            evaluateEntity(opportunity)
        }

        return evaluated.minWithOrNull(compareBy { it.second })?.first
    }

    private fun evaluateEntity(target: CombatEntityAPI): Pair<CombatEntityAPI, Float> {
        val ballisticTarget = BallisticTarget.collisionRadius(target)
        val dist = intercept(weapon, ballisticTarget, params).length
        val range = weapon.totalRange

        return if (dist <= range) {
            // Evaluate the target based on angle and distance.
            val angle = ((target.location - weapon.location).facing - weapon.currAngle.direction).radians
            val angleWeight = 0.75f

            val evalAngle = abs(angle) * angleWeight
            val evalDist = dist / range

            Pair(target, evalAngle + evalDist)
        } else {
            // If the target is out of range, evaluate it based only on range, ignoring the angle.
            val outOfRangePenalty = 1e3f

            val evalDist = (dist - range) / range
            Pair(target, evalDist + outOfRangePenalty)
        }
    }
}
