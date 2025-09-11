package com.genir.aitweaks.core.shipai.autofire

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatAsteroidAPI
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamageType.FRAGMENTATION
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.*
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.shipai.autofire.ballistics.*
import com.genir.aitweaks.core.shipai.movement.Kinematics.Companion.kinematics
import com.genir.aitweaks.core.state.Config.Companion.config
import com.genir.aitweaks.core.utils.Grid
import com.genir.aitweaks.core.utils.angularVelocity
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

    fun target(): CombatEntityAPI? {
        val selectShip = fun(): CombatEntityAPI? {
            return selectShip { !it.isFighter }
        }

        val selectShipOrFighter = fun(): CombatEntityAPI? {
            return selectShip { true }
        }

        val selectFighter = fun(): CombatEntityAPI? {
            return selectShip { it.isFighter }
        }

        val selectNonSupportFighter = fun(): CombatEntityAPI? {
            return selectShip { it.isFighter && !it.isSupportFighter }
        }

        return when {
            Global.getCurrentState() == GameState.TITLE && config.enableTitleScreenFire -> selectAsteroid()

            // PD
            weapon.hasAIHint(PD_ONLY) -> combineSelectors(::selectMissile, selectFighter)
            weapon.hasAIHint(PD_ALSO) -> combineSelectors(selectShipOrFighter, ::selectMissile)
            weapon.hasAIHint(PD) -> combineSelectors(::selectMissile, selectFighter, selectShip)

            // Main weapons
            weapon.isAntiFighter -> selectShipOrFighter()
            weapon.hasAIHint(STRIKE) || weapon.isFinisherBeam -> selectShip()

            // Default main weapon.
            else -> combineSelectors(selectShip, selectNonSupportFighter)
        }
    }

    private fun combineSelectors(vararg selectors: () -> CombatEntityAPI?): CombatEntityAPI? {
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

    private fun selectShip(shipTypeFilter: ((ShipAPI) -> Boolean)): ShipAPI? {
        val prioritizeAntiArmor = when {
            !weapon.isStrictlyAntiArmor -> false

            weapon.damageType == FRAGMENTATION -> false

            weapon.slot.isHardpoint -> false

            else -> true
        }

        // No need to prioritize no-shield targets.
        if (!prioritizeAntiArmor) {
            return selectShipInner(shipTypeFilter)
        }

        val willAttackBypassShields = fun(target: ShipAPI): Boolean {
            val idealHit by lazy {
                estimateIdealHit(weapon, target, params)
            }

            return when {
                !shipTypeFilter(target) -> false

                !target.isShip -> false

                idealHit.type == Hit.Type.SHIELD -> false

                else -> true
            }
        }

        // Prioritize targets that can be hit directly on the hull.
        // If none are available, fall back to targets that can be hit on shields.
        val selected = combineSelectors(
            { selectShipInner(willAttackBypassShields) },
            { selectShipInner(shipTypeFilter) },
        )

        return selected as? ShipAPI
    }

    private fun selectShipInner(shipTypeFilter: ((ShipAPI) -> Boolean)): ShipAPI? {
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

                // Allow tracking main attack target even if it's occluded.
                // This helps the ship to stay focused on finishing a single target.
                target != attackTarget && obstacleList.isOccluded(target) -> false

                else -> true
            }
        }

        val ships: Sequence<ShipAPI> = Grid.ships(weapon.location, targetSearchRange)

        return selectTarget(ships, isTargetAcceptable) as? ShipAPI
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

        return selectTarget(missiles, isTargetAcceptable) as? MissileAPI
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

        return selectTarget(asteroids, isTargetAcceptable) as? CombatAsteroidAPI
    }

    private fun selectTarget(
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
            evaluateTarget(opportunity)
        }

        return evaluated.minWithOrNull(compareBy { it.second })?.first
    }

    private fun evaluateTarget(target: CombatEntityAPI): Pair<CombatEntityAPI, Float> {
        var evaluation = 0f
        val ballisticTarget = BallisticTarget.collisionRadius(target)
        val dist = intercept(weapon, ballisticTarget, params).length
        val range = weapon.totalRange

        // Evaluate the target based on angle from current weapon facing.
        val angle = ((target.location - weapon.location).facing - weapon.currAngle.direction).radians
        val angleWeight = if (dist <= range) 0.75f else 0.25f // Lower angle weight for out of range targets.
        evaluation += abs(angle) * angleWeight

        // Evaluate the target based on range.
        val evalDist = dist / range
        evaluation += evalDist
        if (dist > range) {
            val outOfRangePenalty = 1e4f
            evaluation += outOfRangePenalty
        }

        // Beams exhibit rapid on/off behavior when attempting
        // to track targets with too high angular velocity.
        if (weapon.isPlainBeam && estimateAngularVelocity(target) > weapon.turnRateWhileFiring) {
            val unableToTrackPenalty = 1e3f
            evaluation += unableToTrackPenalty
        }

        return Pair(target, evaluation)
    }

    /** Estimate target angular velocity in weapon frame of reference.
     * Similar to InterceptTracker, but less precise. */
    private fun estimateAngularVelocity(target: CombatEntityAPI): Float {
        val kinematics = weapon.ship.kinematics

        val p = target.location - weapon.location
        val v = target.timeAdjustedVelocity - kinematics.velocity

        val targetW = angularVelocity(p, v)
        return targetW - kinematics.angularVelocity
    }
}
