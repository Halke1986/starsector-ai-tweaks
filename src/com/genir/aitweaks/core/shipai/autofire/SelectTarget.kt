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
import com.genir.aitweaks.core.shipai.autofire.ballistics.BallisticParams
import com.genir.aitweaks.core.shipai.autofire.ballistics.BallisticTarget
import com.genir.aitweaks.core.shipai.autofire.ballistics.Hit
import com.genir.aitweaks.core.shipai.autofire.ballistics.estimateIdealHit
import com.genir.aitweaks.core.shipai.global.TargetTracker
import com.genir.aitweaks.core.shipai.movement.Movement.Companion.movement
import com.genir.aitweaks.core.state.Config.Companion.config
import com.genir.aitweaks.core.utils.Grid
import com.genir.aitweaks.core.utils.angularVelocity
import com.genir.aitweaks.core.utils.types.Direction.Companion.toDirection
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs

class SelectTarget(
    private val weapon: WeaponHandle,
    private val current: CombatEntityAPI?,
    private val attackTarget: ShipAPI?,
    private val params: BallisticParams,
    private val targetTracker: TargetTracker,
) {
    companion object {
        const val TARGET_SEARCH_MULT = 1.5f
    }

    // Search within TARGET_SEARCH_MULT weapon.totalRange to account for projectile
    // flight time, allowing attacks to start before the target enters maximum range.
    private val targetSearchRange = weapon.engagementRange * TARGET_SEARCH_MULT

    private val obstacleList by lazy {
        ObstacleList(weapon, targetSearchRange, targetTracker, params)
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
            return selectShip { it.isFighter && (!it.isSupportFighter || it == attackTarget) }
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

            // Default main weapon. Do not fire at support fighters, as this risks wasting weapon
            // burst immediately before the fighters’ mothership enters weapon range.
            else -> combineSelectors(selectShip, selectNonSupportFighter)
        }
    }

    private fun combineSelectors(vararg selectors: () -> CombatEntityAPI?): CombatEntityAPI? {
        var outOfRangeTarget: CombatEntityAPI? = null

        selectors.forEach { selector ->
            val target = selector()

            if (target != null) {
                val ballisticTarget = BallisticTarget.collisionRadius(target)
                val dist = weapon.ballistics.closestHitRange(ballisticTarget, params)
                val range = weapon.engagementRange

                // New in-range target found.
                if (dist <= range) {
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

                target.owner == weapon.ship.owner -> false

                !shipTypeFilter(target) -> false

                (target.location - weapon.location).length > range -> false

                weapon.slot.isHardpoint -> {
                    when {
                        // Hardpoint weapons select ship target even when it's outside their firing arc,
                        // unless the ship is incapable of following the target.
                        !weapon.ship.isFlamedOut && target == attackTarget -> true

                        !weapon.ballistics.canEngage(BallisticTarget.collisionRadius(target), params, range) -> false

                        else -> true
                    }
                }

                weapon.slot.isTurret -> {
                    when {
                        !weapon.ballistics.canEngage(BallisticTarget.collisionRadius(target), params, range) -> false

                        // Can the weapon rotate fast enough to track the target.
                        !canTrack(target) -> false

                        // Allow tracking main attack target even if it's occluded.
                        // This helps the ship to stay focused on finishing a single target.
                        target != attackTarget && obstacleList.isOccluded(target) -> false

                        else -> true
                    }
                }

                else -> true
            }
        }

        val ships: List<ShipAPI> = targetTracker.getShipTargets()

        return selectTarget(ships, isTargetAcceptable) as? ShipAPI
    }

    private fun selectMissile(): MissileAPI? {
        val isTargetAcceptable = fun(target: CombatEntityAPI, range: Float): Boolean {
            return when {
                target !is MissileAPI -> false

                target.owner == weapon.ship.owner -> false

                (target.location - weapon.location).length > range -> false

                target.isFlare && weapon.ignoresFlares -> false

                !weapon.ballistics.canEngage(BallisticTarget.collisionRadius(target), params, range) -> false

                // Beams exhibit rapid on/off behavior when attempting
                // to track targets with too high angular velocity.
                !canTrack(target) -> false

                obstacleList.isOccluded(target) -> false

                else -> true
            }
        }

        val missiles: List<MissileAPI> = targetTracker.getMissileTargets()

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

                !weapon.ballistics.canEngage(BallisticTarget.collisionRadius(target), params, range) -> false

                !canTrack(target) -> false

                !inViewport(target.location) -> false

                obstacleList.isOccluded(target) -> false

                else -> true
            }
        }

        val asteroids: Sequence<CombatAsteroidAPI> = Grid.asteroids(weapon.location, targetSearchRange)

        return selectTarget(asteroids.toList(), isTargetAcceptable) as? CombatAsteroidAPI
    }

    private fun selectTarget(
        entities: List<CombatEntityAPI>,
        isTargetAcceptable: ((CombatEntityAPI, Float) -> Boolean),
    ): CombatEntityAPI? {
        // Primary target takes priority.
        if (attackTarget != null && isTargetAcceptable(attackTarget, weapon.engagementRange)) {
            return attackTarget
        }

        // Try tracking the current target.
        if (current != null && isTargetAcceptable(current, weapon.engagementRange)) {
            return current
        }

        var selectedTarget: CombatEntityAPI? = null
        var targetEvaluation = Float.MAX_VALUE
        for (target in entities) {
            if (!isTargetAcceptable(target, targetSearchRange)) {
                isTargetAcceptable(target, targetSearchRange)

                continue
            }

            val eval = evaluateTarget(target)
            if (eval < targetEvaluation) {
                selectedTarget = target
                targetEvaluation = eval
            }
        }

        return selectedTarget
    }

    private fun evaluateTarget(target: CombatEntityAPI): Float {
        var evaluation = 0f
        val ballisticTarget = BallisticTarget.collisionRadius(target)
        val dist = weapon.ballistics.intercept(ballisticTarget, params).length
        val range = weapon.engagementRange

        // Evaluate the target based on angle from current weapon facing.
        val angle = ((target.location - weapon.location).facing - weapon.currAngle.toDirection).radians
        val angleWeight = if (dist <= range) 0.75f else 0.25f // Lower angle weight for out of range targets.
        evaluation += abs(angle) * angleWeight

        // Evaluate the target based on range.
        val evalDist = dist / range
        evaluation += evalDist
        if (dist > range) {
            val outOfRangePenalty = 1e4f
            evaluation += outOfRangePenalty
        }

        return evaluation
    }

    private fun canTrack(target: CombatEntityAPI): Boolean {
        if (target == weapon.ship.attackTarget) {
            // Assume the ship is turning towards the attack target.
            return true
        }

        return estimateAngularVelocity(target) <= weapon.turnRateWhileFiring
    }

    /** Estimate target angular velocity in weapon frame of reference.
     * Similar to InterceptTracker, but less precise. */
    private fun estimateAngularVelocity(target: CombatEntityAPI): Float {
        val movement = weapon.ship.movement

        val p = target.location - weapon.location
        val v = target.timeAdjustedVelocity - movement.velocity

        val targetW = angularVelocity(p, v)
        return targetW - movement.angularVelocity
    }
}
