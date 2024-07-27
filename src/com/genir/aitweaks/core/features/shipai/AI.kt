package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatAssignmentType.*
import com.fs.starfarer.api.combat.CombatFleetManagerAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.FLAG_DURATION
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize.SMALL
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.combat.combatState
import com.genir.aitweaks.core.debug.debugPrint
import com.genir.aitweaks.core.features.shipai.systems.SystemAI
import com.genir.aitweaks.core.features.shipai.systems.SystemAIManager
import com.genir.aitweaks.core.utils.extensions.*
import com.genir.aitweaks.core.utils.shieldUptime
import com.genir.aitweaks.core.utils.shipSequence
import com.genir.aitweaks.core.utils.times
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.PI
import kotlin.math.abs

@Suppress("MemberVisibilityCanBePrivate")
class AI(val ship: ShipAPI) {
    // Subclasses.
    val movement: Movement = Movement(this)
    val systemAI: SystemAI? = SystemAIManager.overrideVanillaSystem(this)
    private val vanilla: Vanilla = Vanilla(ship, systemAI != null)

    // Helper classes.
    private val damageTracker: DamageTracker = DamageTracker(ship)
    private var updateInterval: IntervalUtil = IntervalUtil(0.75f, 1.25f)

    // Standing orders.
    var assignment: CombatFleetManagerAPI.AssignmentInfo? = null
    var assignmentLocation: Vector2f? = null
    var maneuverTarget: ShipAPI? = null
    var attackTarget: ShipAPI? = null

    // AI State.
    var stats: ShipStats = ShipStats(ship)
    var broadside: Broadside = stats.broadsides[0]
    var vanillaFlags: ShipwideAIFlags = vanilla.flags()
    var isBackingOff: Boolean = false
    var isHoldingFire: Boolean = false
    var isAvoidingBorder: Boolean = false
    var is1v1: Boolean = false
    var idleTime = 0f
    var threats: List<ShipAPI> = listOf()
    var threatVector = Vector2f()

    fun advance(dt: Float) {
        debug()

        updateInterval.advance(dt)
        val interval: Boolean = updateInterval.intervalElapsed()

        if (interval) {
            stats = ShipStats(ship)
            ensureAutofire()
        }

        // Update state.
        damageTracker.advance()
        updateThreats()

        updateIdleTime(dt)
        updateBackoffStatus()
        updateAssignment(interval)
        updateManeuverTarget(interval)
        updateAttackTarget(interval)
        update1v1Status()

        vanilla.advance(dt, attackTarget, movement.expectedVelocity, movement.expectedFacing)
        ventIfNeeded()
        holdFireIfOverfluxed()

        systemAI?.advance(dt)
        movement.advance(dt)

        vanillaFlags.setFlag(MANEUVER_RANGE_FROM_TARGET, broadside.minRange)
        vanillaFlags.setFlag(MANEUVER_TARGET, FLAG_DURATION, maneuverTarget)
    }

    private fun debug() {
//        drawLine(ship.location, movement.headingPoint ?: ship.location, Color.YELLOW)
//        ship.significantWeapons.forEachIndexed { idx, w ->
//            debugPrint[idx] = "${w.id} ${w.slotRange}"
//        }
//        stats.broadsides.forEachIndexed { idx, b ->
//            debugPrint[idx] = "${b.facing} ${b.maxRange}"
//        }
    }

    private fun updateManeuverTarget(interval: Boolean) {
        val needsUpdate = when {
            // Current target is no longer valid.
            maneuverTarget?.isValidTarget == false -> true

            // Don't change target when movement system is on.
            systemAI?.holdManeuverTarget() == true -> false

            else -> interval
        }

        if (!needsUpdate) return

        // Try cohesion AI first.
        val cohesionAI = combatState().fleetCohesion?.get(ship.owner)
        cohesionAI?.findClosestTarget(ship)?.let {
            maneuverTarget = it
            return
        }

        // Fall back to the closest target.
        val targets = Global.getCombatEngine().ships.filter {
            when {
                it.owner == ship.owner -> false

                !it.isValidTarget -> false

                it.isFighter -> false

                else -> true
            }
        }

        maneuverTarget = targets.minByOrNull { (it.location - ship.location).lengthSquared() }
    }

    private fun updateAssignment(interval: Boolean) {
        // Update assignment location only when assignment
        // was changed or when interval has elapsed.
        if (ship.assignment == assignment && !interval) return

        assignment = ship.assignment
        assignmentLocation = null

        if (assignment == null) return

        val assignment = assignment!!

        when (assignment.type) {
            RECON, AVOID, RETREAT, REPAIR_AND_REFIT, SEARCH_AND_DESTROY -> return

            DEFEND, RALLY_TASK_FORCE, RALLY_CARRIER, RALLY_CIVILIAN, RALLY_STRIKE_FORCE -> Unit
            RALLY_FIGHTERS, STRIKE, INTERCEPT, HARASS, LIGHT_ESCORT, MEDIUM_ESCORT -> Unit
            HEAVY_ESCORT, CAPTURE, CONTROL, ASSAULT, ENGAGE -> Unit

            else -> return
        }

        val location = assignment.target?.location ?: return

        if ((ship.location - location).length() > Preset.arrivedAtLocationRadius) assignmentLocation = location
    }

    /** Select which enemy ship to attack. This may be different
     * from the maneuver target provided by the ShipAI. */
    private fun updateAttackTarget(interval: Boolean) {
        val currentTarget = attackTarget

        val updateTarget = when {
            currentTarget == null -> true
            !currentTarget.isValidTarget -> true

            // Do not interrupt weapon bursts.
            stats.significantWeapons.any { it.size != SMALL && it.isInFiringSequence && it.customAI?.targetShip == attackTarget } -> false

            // Target is out of range.
            range(currentTarget) > broadside.maxRange -> true

            // Finish helpless target.
            currentTarget.fluxTracker.isOverloadedOrVenting -> false

            else -> interval
        }

        if (updateTarget) {
            val (broadside, target) = findNewAttackTarget()
//            if (currentTarget == target)
//                return

            ship.shipTarget = target
            attackTarget = target
            this.broadside = broadside
        }
    }

    /** Decide if ships needs to back off due to high flux level */
    private fun updateBackoffStatus() {
        val backingOffFlag = vanillaFlags.hasFlag(BACKING_OFF)
        val fluxLevel = ship.fluxTracker.fluxLevel
        val underFire = damageTracker.damage / ship.maxFlux > 0.2f

        isBackingOff = when {
            // Enemy is routing, keep the pressure.
            Global.getCombatEngine().isEnemyInFullRetreat -> false

            // High flux.
            fluxLevel > Preset.backoffUpperThreshold -> true

            // Shields down and received damage.
            ship.shield != null && ship.shield.isOff && underFire -> true

            // Started venting under fire.
            ship.fluxTracker.isVenting && underFire -> true

            // Stop backing off.
            backingOffFlag && fluxLevel <= 0.01f -> false

            // Continue backing off.
            else -> backingOffFlag
        }

        if (isBackingOff) vanillaFlags.setFlag(BACKING_OFF)
        else if (backingOffFlag) vanillaFlags.unsetFlag(BACKING_OFF)
    }

    private fun updateIdleTime(dt: Float) {
        val shieldIsUp = ship.shield?.isOn == true && shieldUptime(ship.shield) > Preset.shieldFlickerThreshold
        val pdIsFiring = ship.allWeapons.firstOrNull { it.isPD && it.isFiring } != null

        idleTime = if (shieldIsUp || pdIsFiring) 0f
        else idleTime + dt
    }

    /** Is ship engaged in 1v1 duel with the target. */
    private fun update1v1Status() {
        is1v1 = when {
            isAvoidingBorder -> false
            isBackingOff -> false
            attackTarget == null -> false
            attackTarget != maneuverTarget -> false
            attackTarget!!.isFrigate != ship.isFrigate -> false
            threats.size > 1 -> false
            else -> true
        }
    }

    private fun updateThreats() {
        threats = shipSequence(ship.location, stats.threatSearchRange).filter { isThreat(it) }.toList()

        threatVector = threats.fold(Vector2f()) { sum, it ->
            val dp = it.deploymentPoints
            val dir = (it.location - ship.location).resized(1f)
            sum + dir * dp * dp
        }
    }

    private fun ensureAutofire() {
        (ship as Ship).setNoWeaponSelected()
        ship.weaponGroupsCopy.forEach { it.toggleOn() }
    }

    private fun holdFireIfOverfluxed() {
        isHoldingFire = when {
            // Ships with no shields don't need to preserve flux.
            ship.shield == null -> false

            // Ship is overfluxed.
            ship.fluxTracker.fluxLevel > Preset.holdFireThreshold -> true

            else -> false
        }

        if (isHoldingFire) {
            val fluxWeapons = ship.allWeapons.filter { !it.isPD && it.fluxCostToFire != 0f }
            fluxWeapons.mapNotNull { it.autofirePlugin }.forEach { it.forceOff() }
        }
    }

    /** Force vent when the ship is backing off,
     * not shooting and with shields down. */
    private fun ventIfNeeded() {
        val shouldVent = when {
            // Already venting.
            ship.fluxTracker.isVenting -> false

            // No need to vent.
            ship.fluxLevel < 0.01f -> false

            // Don't interrupt the ship system.
            ship.system?.isOn == true -> false

            // Ship with no shield vents when it can't fire anymore.
            ship.shield == null -> {
                val fluxLeft = ship.fluxTracker.maxFlux - ship.fluxTracker.currFlux
                ship.allWeapons.any { it.fluxCostToFire >= fluxLeft }
            }

            !isBackingOff -> false

            // Vent regardless of situation when already passively
            // dissipated below Preset.backoffLowerThreshold
            ship.fluxLevel < Preset.backoffLowerThreshold -> true

            idleTime < Preset.shieldDownVentTime -> false

            // Don't vent when any weapon is firing.
            ship.allWeapons.any { it.customAI?.shouldFire() == true } -> false

            else -> true
        }

        if (shouldVent) ship.giveCommand(ShipCommand.VENT_FLUX, null, 0)
    }

    private fun findNewAttackTarget(): Pair<Broadside, ShipAPI?> {
        // Find best attack opportunity for each possible broadside.
        val broadsideTargets: Map<Broadside, ShipAPI> = stats.broadsides.associateWith { broadside ->
            val opportunities = threats.filter { range(it) < broadside.maxRange }

            val foldInit: Pair<ShipAPI?, Float> = Pair(null, Float.MAX_VALUE)
            opportunities.fold(foldInit) { best, it ->
                val eval = evaluateTarget(it, broadside)
                if (eval < best.second) Pair(it, eval)
                else best
            }.first
        }.filterValues { it != null }.mapValues { it.value!! }

//        debugPrint.clear()
//
//        broadsideTargets.forEach {
//            debugPrint[it.key] = "${it.value.name} ${evaluateTarget(it.value, it.key)}"
//        }

        val bestTarget = broadsideTargets.minOfWithOrNull(compareBy { evaluateTarget(it.value, it.key) }) { it }
        if (bestTarget != null) return bestTarget.toPair()

        val altTarget: ShipAPI? = when {
            maneuverTarget != null -> maneuverTarget

            // Try to find a target near move location.
            assignmentLocation != null -> {
                shipSequence(assignmentLocation!!, 200f).firstOrNull { isThreat(it) }
            }

            else -> null
        }
        return Pair(stats.broadsides[0], altTarget)
    }

    /** Evaluate if target is worth attacking. The lower the score, the better the target. */
    private fun evaluateTarget(target: ShipAPI, broadside: Broadside): Float {
        // Prioritize targets closer to ship facing.
        val angle = ship.shortestRotationToTarget(target.location, broadside.facing) * PI.toFloat() / 180.0f
        val angleWeight = 0.75f
        val evalAngle = abs(angle) * angleWeight

        // Prioritize closer targets. Avoid attacking targets out of effective weapons range.
        val dist = range(target)
        val distWeight = 1f / broadside.dpsFractionAtRange(dist)
        val evalDist = (dist / broadside.maxRange) * distWeight

        // Prioritize targets high on flux. Avoid hunting low flux phase ships.
        val fluxLeft = (1f - target.fluxLevel)
        val fluxFactor = if (target.phaseCloak?.specAPI?.isPhaseCloak == true) 2f else 0.5f
        val evalFlux = fluxLeft * fluxFactor

        // Avoid attacking bricks, especially Monitors.
        val evalDamper = if (target.system?.id == "damper" && !target.isFrigate) 1f else 0f
        val evalShunt = if (target.variant.hasHullMod("fluxshunt") && target.isFrigate) 256f else 0f

        // Assign lower priority to frigates.
        val evalType = if (target.isFrigate && !target.isModule) 1f else 0f

        // TODO avoid wrecks

        return evalAngle + evalDist + evalFlux + evalDamper + evalShunt + evalType
    }

    private fun isThreat(target: ShipAPI): Boolean {
        return target.owner != ship.owner && target.isAlive && !target.isExpired && target.isShip
    }

    internal fun range(target: ShipAPI): Float {
        return (target.location - ship.location).length() - target.collisionRadius / 2f
    }
}
