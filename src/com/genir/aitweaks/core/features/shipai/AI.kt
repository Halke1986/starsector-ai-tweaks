package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.FLAG_DURATION
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize.SMALL
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.utils.ShipSystemAiType.BURN_DRIVE_TOGGLE
import com.genir.aitweaks.core.utils.extensions.*
import com.genir.aitweaks.core.utils.shieldUptime
import com.genir.aitweaks.core.utils.shipSequence
import com.genir.aitweaks.core.utils.times
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign
import kotlin.random.Random

@Suppress("MemberVisibilityCanBePrivate")
class AI(val ship: ShipAPI) {
    // Subclasses.
    val vanilla: Vanilla = Vanilla(ship)
    val movement: Movement = Movement(this)
    val burnDriveAI: BurnDrive? = if (ship.system?.specAPI?.AIType == BURN_DRIVE_TOGGLE) BurnDrive(ship, this) else null // TODO move to Movement
    val damageTracker: DamageTracker = DamageTracker(ship)

    // Ship stats.
    var effectiveRange: Float = 0f
    var minRange: Float = 0f
    var maxRange: Float = 0f
    var totalCollisionRadius: Float = 0f
    val broadsideFacing = calculateBroadsideFacing()

    // Standing orders.
    var moveOrderLocation: Vector2f? = null // TODO handle move order
    var maneuverTarget: ShipAPI? = null
    var attackTarget: ShipAPI? = null

    var headingPoint: Vector2f? = null
    var aimPoint: Vector2f? = null

    // State
    var vanillaFlags: ShipwideAIFlags = vanilla.flags()
    var isBackingOff: Boolean = false
    var isHoldingFire: Boolean = false
    var isAvoidingBorder: Boolean = false
    var is1v1: Boolean = false

    private var idleTime = 0f
    private var threats: List<ShipAPI> = listOf()
    internal var threatVector = Vector2f()

    private var maneuverUpdateInterval: IntervalUtil = IntervalUtil(0.75f, 1.25f)

    fun advance(dt: Float) {
        // Update state.
        damageTracker.advance()
        updateThreats()
        updateStats()

        updateIdleTime(dt)
        updateBackoffStatus()
        updateManeuverTarget(dt)
        updateAttackTarget()
        update1v1Status()

        vanilla.advance(dt, attackTarget)
        ventIfNeeded()
        holdFireIfOverfluxed()
        ensureAutofire()

        movement.advance(dt)

        vanillaFlags.setFlag(MANEUVER_RANGE_FROM_TARGET, minRange)
        vanillaFlags.setFlag(MANEUVER_TARGET, FLAG_DURATION, maneuverTarget)
    }

    private fun updateStats() {
        effectiveRange = ship.effectiveRange(Preset.effectiveDpsThreshold)
        minRange = ship.minRange
        maxRange = ship.maxRange
        totalCollisionRadius = ship.totalCollisionRadius
    }

    private fun updateManeuverTarget(dt: Float) {
        maneuverUpdateInterval.advance(dt)

        val needsUpdate = when {
            maneuverTarget == null -> true

            maneuverUpdateInterval.intervalElapsed() -> true

            maneuverTarget?.isValidTarget != true -> true

            else -> false
        }

        if (needsUpdate) {
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

        //init {
        //        maneuverTarget = when {
        //            noOrders -> oldManeuver?.maneuverTarget
        //
        //            // Don't change target when burn drive is on.
        //            burnDriveAI != null && ship.system.isOn && oldManeuver?.maneuverTarget?.isValidTarget == true -> {
        //                oldManeuver.maneuverTarget
        //            }
        //
        //            // Custom ship AI uses fleet cohesion directly, instead of through orders.
        //            else -> {
        //                combatState().fleetCohesion?.get(ship.owner)?.findValidTarget(ship, vanillaManeuverTarget)
        //                    ?: vanillaManeuverTarget
        //            }
        //        }
        //    }
//        return when {
//            // Target ship was destroyed.
//            maneuverTarget != null && (maneuverTarget.isExpired || !maneuverTarget.isAlive) -> {
//                true
//            }
//
//            // Arrived at location.
//            moveOrderLocation != null && (ship.location - moveOrderLocation).length() <= Preset.arrivedAtLocationRadius -> {
//                true
//            }
//
//            else -> false
//        }
    }

    /** Select which enemy ship to attack. This may be different
     * from the maneuver target provided by the ShipAI. */
    private fun updateAttackTarget() {
        val currentTarget = attackTarget

        val updateTarget = when {
            currentTarget == null -> true
            !currentTarget.isValidTarget -> true

            // Do not interrupt weapon bursts.
            ship.primaryWeapons.any { it.size != SMALL && it.isInFiringSequence && it.autofireAI?.targetShip == attackTarget } -> false

            // Target is out of range.
            range(currentTarget) > maxRange -> true

            // Finish helpless target.
            currentTarget.fluxTracker.isOverloadedOrVenting -> false

            else -> true
        }

        val updatedTarget = if (updateTarget) findNewAttackTarget()
        else currentTarget

        ship.shipTarget = updatedTarget
        attackTarget = updatedTarget
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
        val rangeEnvelope = 500f
        val r = max(Preset.threatEvalRadius, maxRange + rangeEnvelope)
        threats = shipSequence(ship.location, r).filter { isThreat(it) }.toList()

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
        isHoldingFire = ship.fluxTracker.fluxLevel > Preset.holdFireThreshold

        if (isHoldingFire) {
            ship.allWeapons.filter { !it.isPD && it.fluxCostToFire != 0f }.mapNotNull { it.autofirePlugin }.forEach { it.forceOff() }
        }
    }

    /** Force vent when the ship is backing off,
     * not shooting and with shields down. */
    private fun ventIfNeeded() {
        val shouldVent = when {
            !isBackingOff -> false
            ship.fluxTracker.isVenting -> false
            ship.fluxLevel < 0.01f -> false
            ship.fluxLevel < Preset.backoffLowerThreshold -> true
            idleTime < Preset.shieldDownVentTime -> false
            ship.allWeapons.firstOrNull { it.autofireAI?.shouldFire() == true } != null -> false
            else -> true
        }

        if (shouldVent) ship.giveCommand(ShipCommand.VENT_FLUX, null, 0)
    }

    private fun findNewAttackTarget(): ShipAPI? {
        val opportunities = threats.filter { range(it) < maxRange }

        val foldInit: Pair<ShipAPI?, Float> = Pair(null, Float.MAX_VALUE)
        val bestOpportunity = opportunities.fold(foldInit) { best, it ->
            val eval = evaluateTarget(it)
            if (eval < best.second) Pair(it, eval)
            else best
        }.first

        return when {
            bestOpportunity != null -> bestOpportunity

            maneuverTarget != null -> maneuverTarget

            // Try to find a target near move location.
            moveOrderLocation != null -> {
                shipSequence(moveOrderLocation!!, 200f).firstOrNull { isThreat(it) }
            }

            else -> null
        }
    }

    /** Evaluate if target is worth attacking. The lower the score, the better the target. */
    private fun evaluateTarget(target: ShipAPI): Float {
        // Prioritize targets closer to ship facing.
        val angle = ship.shortestRotationToTarget(target.location, broadsideFacing) * PI.toFloat() / 180.0f
        val angleWeight = 0.75f
        val evalAngle = abs(angle) * angleWeight

        // Prioritize closer targets. Avoid attacking targets out of effective weapons range.
        val dist = range(target)
        val distWeight = 1f / ship.dpsFractionAtRange(dist)
        val evalDist = (dist / maxRange) * distWeight

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

    private fun calculateBroadsideFacing(): Float {
        // Find all important weapons.
        val weapons = ship.allWeapons.filter { weapon ->
            when {
                weapon.type == WeaponAPI.WeaponType.MISSILE -> false
                weapon.derivedStats.dps == 0f -> false
                weapon.isPD -> false
                else -> true
            }
        }

        // Find firing arc boundary closes to ship front for each weapon,
        // or 0f for front facing weapons. Shuffle to randomly chose
        // one broadside for symmetric broadside ships.
        val angles = weapons.fold(setOf(0f)) { angles, weapon ->
            val facing = MathUtils.getShortestRotation(0f, weapon.arcFacing)

            val angle = if (weapon.isAngleInArc(0f)) 0f
            else facing - facing.sign * (weapon.arc / 2f - 0.1f)

            angles + angle
        }.filter { abs(it) <= Preset.maxBroadsideFacing }.shuffled(Random(ship.id.hashCode()))

        // Calculate DPS at each weapon arc boundary angle.
        val angleDPS: Map<Float, Float> = angles.associateWith { angle ->
            weapons.filter { it.isAngleInArc(angle) }.sumOf { it.derivedStats.dps.toDouble() }.toFloat()
        }

        val bestAngleDPS: Map.Entry<Float, Float> = angleDPS.maxByOrNull { it.value } ?: return 0f

        // Prefer non-broadside orientation.
        if (bestAngleDPS.value * Preset.broadsideDPSThreshold < angleDPS[0f]!!) return 0f

        return bestAngleDPS.key + bestAngleDPS.key.sign * Preset.broadsideFacingPadding
    }

    private fun isThreat(target: ShipAPI): Boolean {
        return target.owner != ship.owner && target.isAlive && !target.isExpired && target.isShip
    }

    internal fun range(target: ShipAPI): Float {
        return (target.location - ship.location).length() - target.collisionRadius / 2f
    }
}
