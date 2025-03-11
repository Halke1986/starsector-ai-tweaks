package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatAssignmentType.RETREAT
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags.MANEUVER_TARGET
import com.fs.starfarer.api.combat.ShipwideAIFlags.FLAG_DURATION
import com.fs.starfarer.api.util.IntervalUtil
import com.genir.aitweaks.core.debug.Debug
import com.genir.aitweaks.core.debug.expectedFacing
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.Preset.Companion.assaultShipApproachFactor
import com.genir.aitweaks.core.shipai.Preset.Companion.targetThickness
import com.genir.aitweaks.core.shipai.systems.BurnDriveToggle
import com.genir.aitweaks.core.shipai.systems.SystemAI
import com.genir.aitweaks.core.shipai.systems.SystemAIManager
import com.genir.aitweaks.core.state.Config
import com.genir.aitweaks.core.state.State.Companion.state
import com.genir.aitweaks.core.utils.*
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.math.max

@Suppress("MemberVisibilityCanBePrivate")
class CustomShipAI(val ship: ShipAPI) : BaseShipAIPlugin() {
    // Subsystems.
    val movement: Movement = Movement(this)
    val assignment: Assignment = Assignment(this)
    val ventModule: VentModule = VentModule(this)
    val systemAI: SystemAI? = SystemAIManager.overrideVanillaSystem(this)
    val vanilla: VanillaModule = VanillaModule(ship, systemAI?.overrideVanillaSystemAI() == true)

    // Helper classes.
    private val updateInterval: IntervalUtil = defaultAIInterval()

    // Standing orders.
    var maneuverTarget: ShipAPI? = null
    var attackTarget: CombatEntityAPI? = null

    // Keep attacking the previous target for the
    // duration or already started weapon bursts.
    var finishBurstTarget: CombatEntityAPI? = null
    var finishBurstWeaponGroup: WeaponGroup? = null

    // Attack details.
    var stats: ShipStats = ShipStats(ship)
    var attackingGroup: WeaponGroup = stats.weaponGroups[0]
    var attackRange: Float = 0f

    // AI State.
    val isAssaultShip: Boolean = systemAI is BurnDriveToggle
    var isExploring: Boolean = true
    var isAvoidingBorder: Boolean = false
    var is1v1: Boolean = false
    var threats: Set<ShipAPI> = setOf()
    var threatVector = Vector2f()

    val knownSegmentationTargets: MutableSet<ShipAPI> = mutableSetOf()

    override fun advance(dt: Float) {
        debug()

        // Cede the control to vanilla AI when the ship is retreating.
        if (ship.assignment?.type == RETREAT) {
            vanilla.advanceBasicShipAI(dt)
            return
        }

        // Update state.
        updateThreatVector()

        updateInterval.advance(dt)
        if (updateInterval.intervalElapsed()) {
            updateThreats()
            updateShipStats()
            updateAttackRange()
            update1v1Status()
            updateExplorationStatus()

            // Update targets.
            updateManeuverTarget()
            updateAttackTarget()
            updateFinishBurstTarget()
        }

        // Advance subsystems.
        vanilla.advance(dt, attackTarget as? ShipAPI, movement.expectedVelocity, movement.expectedFacing)
        assignment.advance()
        ventModule.advance(dt)
        systemAI?.advance(dt)
        movement.advance(dt)

        vanilla.flags.setFlag(MANEUVER_TARGET, FLAG_DURATION, maneuverTarget)
    }

    override fun getAIFlags(): ShipwideAIFlags = vanilla.flags

    private fun debug() {
        if (Config.config.highlightCustomAI) {
            Debug.drawCircle(ship.location, ship.collisionRadius / 2f, Color.BLUE)
        }

        if (ship.owner != 0) {
            return
        }

//        Debug.drawTurnLines(ship)
//        Debug.drawCircle(movement.headingPoint, ship.collisionRadius)

//        Debug.drawCircle(ship.location, stats.threatSearchRange)

//        Debug.drawLine(ship.location, attackTarget?.location ?: ship.location, Color.RED)
//        Debug.drawLine(ship.location, maneuverTarget?.location ?: ship.location, Color.BLUE)
//        Debug.drawLine(ship.location, finishBurstTarget?.location ?: ship.location, Color.YELLOW)

//        Debug.drawLine(ship.location, ship.location + (maneuverTarget?.velocity ?: ship.location), Color.GREEN)
//        Debug.drawLine(ship.location, movement.headingPoint, Color.YELLOW)

//        Debug.drawLine(ship.location, ship.location + unitVector(ship.facing) * 600f, Color.GREEN)
//        Debug.drawLine(ship.location, ship.location + movement.expectedFacing.unitVector * 600f, Color.YELLOW)

//        ship.facing = movement.expectedFacing.degrees

//        Debug.drawLine(ship.location, ship.location + unitVector(ship.facing + attackingGroup.facing) * 600f, Color.BLUE)
//        Debug.drawLine(ship.location, ship.location + movement.expectedVelocity, Color.GREEN)
//        Debug.drawLine(ship.location, ship.location + (ship.velocity).resized(300f), Color.BLUE)
//        Debug.drawLine(ship.location, ship.location - threatVector.resized(600f), Color.PINK)

//        if (maneuverTarget != null) {
//            Debug.drawCircle(ship.location, attackingGroup.minRange, Color.BLUE)
//            Debug.drawCircle(maneuverTarget!!.location, effectiveTargetRadius(maneuverTarget!!))
//        }
    }

    private fun updateManeuverTarget() {
        val segmentationTarget: ShipAPI? by lazy { findClosestSegmentationTarget() }

        when {
            // Don't change target when movement system is on.
            maneuverTarget?.isValidTarget == true && systemAI?.holdTargets() == true -> {
                Unit
            }

            // Eliminate assignment target.
            assignment.eliminate != null -> {
                maneuverTarget = assignment.eliminate
            }

            // Try fleet segmentation targets first.
            segmentationTarget != null -> {
                knownSegmentationTargets.add(segmentationTarget!!)
                maneuverTarget = segmentationTarget
            }

            // No target found in the exploration phase of the battle. Don't force finding one.
            isExploring -> {
                Unit
            }

            // Fall back to the closest target.
            else -> {
                val targets = Global.getCombatEngine().ships.filter {
                    when {
                        it.owner == ship.owner -> false

                        !it.isValidTarget -> false

                        it.isFighter -> false

                        else -> true
                    }
                }

                maneuverTarget = closestEntity(targets, ship.location)
            }
        }
    }

    /** Find a new maneuver target using enemy fleet segmentation. */
    private fun findClosestSegmentationTarget(): ShipAPI? {
        val segmentation = state.fleetSegmentation[ship.owner]

        // Prioritize the nearest segmentation target over primary targets if the ship is already in proximity to it.
        val allTargets = segmentation.allTargets(ship.isFast).filter { isTargetVisible(it) }
        val closestTarget: ShipAPI = closestEntity(allTargets, ship.location) ?: return null
        if (isCloseToEnemy(ship, closestTarget)) {
            return closestTarget
        }

        // Follow the closest primary target.
        val primaryTargets = segmentation.primaryTargets(ship.isFast).filter { isTargetVisible(it) }
        return closestEntity(primaryTargets, ship.location)
    }

    /** Consider once seen ships as visible, even if they're in fog again. This allows
     * to avoid erratic target changes in the initial phase of the battle. */
    private fun isTargetVisible(target: ShipAPI): Boolean {
        return target in knownSegmentationTargets || Global.getCombatEngine().getFogOfWar(ship.owner).isVisible(target)
    }

    /** Select which enemy ship to attack. This may be different
     * from the maneuver target provided by the ShipAI. */
    private fun updateAttackTarget() {
        // Don't change target when movement system is on.
        if (attackTarget?.isValidTarget == true && systemAI?.holdTargets() == true) return

        val (newWeaponGroup, newTarget) = findNewAttackTarget()

        // Keep track of previous target until weapon bursts subside.
        if (newTarget != attackTarget && attackTarget?.isValidTarget == true) {
            finishBurstTarget = attackTarget
            finishBurstWeaponGroup = attackingGroup
        }

        ship.shipTarget = newTarget
        attackTarget = newTarget
        attackingGroup = newWeaponGroup
    }

    private fun updateShipStats() {
        stats = ShipStats(ship)

        // Find the most similar weapon group to the current one after ship stats have been updated.
        attackingGroup = stats.weaponGroups.minWithOrNull(compareBy { (attackingGroup.defaultFacing - it.defaultFacing).length })!!
    }

    /** Is ship engaged in 1v1 duel with the target. */
    private fun update1v1Status() {
        is1v1 = when {
            isAvoidingBorder -> false
            ventModule.isBackingOff -> false
            attackTarget == null -> false
            attackTarget != maneuverTarget -> false
            (attackTarget as? ShipAPI)?.root?.isFrigate != ship.root.isFrigate -> false
            threats.size > 1 -> false
            else -> true
        }
    }

    private fun updateThreats() {
        threats = Grid.ships(ship.location, stats.threatSearchRange).filter { isThreat(it) }.toSet()
    }

    /** The threat vector should be updated every frame, as it is used
     * in movement calculations. Values involved in these calculations
     * should change smoothly to avoid erratic velocity changes. */
    private fun updateThreatVector() {
        val maxThreatDistSqr = stats.threatSearchRange * stats.threatSearchRange
        threatVector = threats.fold(Vector2f()) { sum, threat ->
            // Count modular ships just once.
            if (threat.isModule) return@fold sum

            val dp = max(1f, threat.deploymentPoints)
            val toThreat = threat.location - ship.location

            // Threats are assigned decreasing weights as they approach the maximum
            // threat radius, eventually reaching a weight of 0 when they leave the radius.
            // This ensures smooth transitions in threat vectors, avoiding sudden changes
            // when a threat exits the radius. The maneuver target is always assigned
            // a weight of 1, preventing situations where the threat vector becomes undefined.
            val weight = max(maxThreatDistSqr - toThreat.lengthSquared, 0f) / maxThreatDistSqr

            val dir = toThreat.resized(weight)
            sum + dir * dp * dp
        }.resized(1f)
    }

    /** Keep track of previous target until weapon bursts subside. */
    private fun updateFinishBurstTarget() {
        if (finishBurstTarget?.isValidTarget != true) {
            finishBurstTarget = null
            finishBurstWeaponGroup = null
            return
        }

        val continueBurst = finishBurstWeaponGroup?.weapons?.any { it.isInFiringSequence && it.target == finishBurstTarget }
        if (continueBurst != true) finishBurstTarget = null
    }

    /** Exploration phase is implemented only for the opening phase of the battle,
     * when it would be obvious that ships can see through the fog of war. */
    private fun updateExplorationStatus() {
        isExploring = when {
            !isExploring -> false

            // End the exploration phase after first target is acquired.
            maneuverTarget != null -> false

            threats.isNotEmpty() -> false

            // Stop pretending to explore after the ship has reached the center of the map.
            assignment.type == Assignment.Type.EXPLORE && assignment.arrivedAt -> false

            // No exploring in simulator.
            Global.getCombatEngine().isSimulation -> false

            else -> true
        }
    }

    private fun findNewAttackTarget(): Pair<WeaponGroup, ShipAPI?> {
        val allies = Global.getCombatEngine().ships.filter { it != ship && it.owner == ship.owner && !it.isFighter && !it.isFrigate }
        val targetedEnemies = allies.mapNotNull { it.attackTarget }.filter { it.isBig }.toSet()

        // Find best attack opportunity for each weapon group.
        val opportunities = threats.filter { it.isValidTarget }
        val weaponGroupTargets: Map<WeaponGroup, Map.Entry<ShipAPI, Float>> = stats.weaponGroups.associateWith { weaponGroup ->
            val obstacles = getObstacles(weaponGroup)
            val groupOpportunities = opportunities.asSequence().filter { currentEffectiveRange(it) < weaponGroup.maxRange }
            val evaluatedOpportunities: Map<ShipAPI, Float> = groupOpportunities.associateWith {
                evaluateTarget(it, weaponGroup, obstacles, targetedEnemies)
            }

            evaluatedOpportunities.maxWithOrNull(compareBy { it.value })
        }.filterValues { it != null }.mapValues { it.value!! }

        val bestTarget = weaponGroupTargets.maxOfWithOrNull(compareBy { it.value.value }) { it }
        if (bestTarget != null) {
            return Pair(bestTarget.key, bestTarget.value.key)
        }

        // No good attack target found. Try alternatives.
        val altTarget: ShipAPI? = when {
            maneuverTarget != null -> maneuverTarget

            // Try to find a target near move location.
            assignment.navigateTo != null -> {
                Grid.ships(assignment.navigateTo!!, 200f).filter { isThreat(it) }.firstOrNull()
            }

            else -> null
        }

        return Pair(stats.weaponGroups[0], altTarget)
    }

    private fun isThreat(target: ShipAPI): Boolean {
        return target.owner != ship.owner && target.isAlive && target.isShip && !target.isFighter
    }

    private inner class Obstacle(val arc: Arc, val dist: Float) {
        fun occludes(target: ShipAPI): Boolean {
            val toTarget = target.location - ship.location
            return arc.contains(toTarget.facing) && dist < toTarget.length
        }
    }

    private fun getObstacles(weaponGroup: WeaponGroup): List<Obstacle> {
        val obstacles = Grid.ships(ship.location, weaponGroup.maxRange).filter { obstacle ->
            when {
                // Same ship.
                obstacle.root == ship.root -> false

                obstacle.isFighter -> false

                // Don't consider enemy ships as obstacles. Try to shoot
                // through them, as long as they're possible to damage.
                !obstacle.isHullDamageable -> true
                obstacle.isHostile(ship) -> false

                obstacle.isFast -> false

                else -> true
            }
        }

        // Use simple approximate calculations instead of ballistics for simplicity.
        return obstacles.map { obstacle ->
            val toObstacle = obstacle.location - ship.location
            val dist = toObstacle.length
            val arc = Arc(angularSize(dist * dist, obstacle.boundsRadius * 0.8f), toObstacle.facing)

            Obstacle(arc, dist)
        }.toList()
    }

    /** Evaluate if target is worth attacking. The higher the score, the better the target. */
    private fun evaluateTarget(target: ShipAPI, weaponGroup: WeaponGroup, obstacles: List<Obstacle>, targetedEnemies: Set<ShipAPI>): Float {
        var evaluation = 0f

        // Prioritize targets closer to ship facing.
        val angle = ship.shortestRotationToTarget(target.location, weaponGroup.defaultFacing).length / ship.maxTurnRate
        val angleWeight = 0.2f
        evaluation -= angle * angleWeight

        // Prioritize closer targets. Avoid attacking targets out of effective weapons range.
        val dist = currentEffectiveRange(target)
        val distWeight = 1f / weaponGroup.dpsFractionAtRange(dist)
        evaluation -= (dist / weaponGroup.maxRange) * distWeight

        // Prioritize targets high on flux. Avoid hunting low flux phase ships.
        val fluxLeft = (1f - target.FluxLevel)
        val fluxFactor = if (target.phaseCloak?.specAPI?.isPhaseCloak == true) 2f else 0.5f
        evaluation -= fluxLeft * fluxFactor

        // Avoid attacking bricks, especially Monitors.
        if (target.system?.id == "damper" && !target.root.isFrigate) {
            evaluation += -1f
        }
        if (target.variant.hasHullMod("fluxshunt") && target.root.isFrigate) {
            evaluation += -16f
        }

        // Assign higher priority to large targets for slow ships.
        if ((ship.root.isCruiser || ship.root.isCapital) && !ship.isFast) when {
            target.root.isFrigate -> evaluation += -1f
            target.root.isCruiser -> evaluation += 0.5f
            target.root.isCapital -> evaluation += 1f
        }

        // Sync attack with allies.
        if (targetedEnemies.contains(target)) {
            evaluation += 1f
        }

        // Avoid attacking ships with no weapons (mostly station armor modules).
        if (target.allGroupedWeapons.isEmpty()) {
            evaluation += -3f
        }

        // Finish helpless target.
        if (target.fluxTracker.isOverloadedOrVenting) {
            evaluation += 2f
        }

        // Try to stay on target.
        if (target == attackTarget) {
            val withinRange = currentEffectiveRange(target) <= weaponGroup.effectiveRange
            val withinArc = (expectedFacing - ship.facing).length < 60f
            if (withinRange && withinArc) {
                evaluation += 1f
            }
        }

        // Strongly prioritize eliminate assignment.
        if (target == assignment.eliminate) {
            evaluation += 16f
        }

        // Do not attempt to attack occluded targets.
        if (obstacles.any { it.occludes(target) }) {
            evaluation += -16f
        }

        return evaluation
    }

    /** Range from which ship should attack its target. */
    fun updateAttackRange() {
        // Range overridden by ai flag.
        vanilla.flags.get<Float>(ShipwideAIFlags.AIFlags.MANEUVER_RANGE_FROM_TARGET)?.let {
            attackRange = it
            return
        }

        val range = when {
            // Default all-weapons attack range.
            attackingGroup.dps > 0f -> attackingGroup.minRange

            // Range for ships with no weapons.
            else -> Preset.noWeaponsAttackRange
        }

        val approachFactor = if (isAssaultShip) assaultShipApproachFactor else 1f

        attackRange = (effectiveTargetRadius(maneuverTarget) + range) * approachFactor
    }

    /** Distance between ship location and target effective radius. */
    fun currentEffectiveRange(target: CombatEntityAPI): Float {
        if (target !is ShipAPI) {
            return (target.location - ship.location).length
        }

        return max(0f, targetBoundsDistance(target) + targetThickness)
    }

    /** Target radius that should be used when calculating attack range. */
    private fun effectiveTargetRadius(target: CombatEntityAPI?): Float {
        if (target !is ShipAPI) {
            return 0f
        }

        val position = ship.location - target.location
        val dist = targetBoundsDistance(target)
        val radius = position.length - dist

        return radius - targetThickness
    }

    /** Distance between ship location and target bounds. */
    private fun targetBoundsDistance(target: ShipAPI): Float {
        val position = ship.location - target.location
        val velocity = position.resized(-1f)
        return Bounds.collision(position, velocity, target) ?: 0f
    }
}
