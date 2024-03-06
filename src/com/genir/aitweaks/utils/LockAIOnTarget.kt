package com.genir.aitweaks.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.ai.attack.AttackAIModule
import com.fs.starfarer.combat.ai.movement.maneuvers.StrafeTargetManeuverV2
import com.fs.starfarer.combat.ai.movement.maneuvers.oO0O
import com.fs.starfarer.combat.entities.Ship
import com.fs.starfarer.combat.systems.G
import org.lwjgl.util.vector.Vector2f

/** Force ship AI to continuously strafe the given target. */
class LockAIOnTarget(private val ship: ShipAPI, private val target: ShipAPI?, private val flags: List<AIFlags> = listOf()) {
    private var lockManeuver: StrafeTargetManeuverV2? = null

    init {
        try {
            if (ship is Ship && target is Ship && target.isAlive && ship.ai is BasicShipAI) {
                val badTarget = false
                val tilt = 0f
                val unknownFloat = 10000f
                val unknownBool = false

                val ai = ship.ai as BasicShipAI
                lockManeuver = StrafeTargetManeuverV2(ship, target, badTarget, tilt, unknownFloat, ai.flockingAI, KeepTargetAI(ai), unknownBool)
                ai.setManeuver(lockManeuver, 100f)
                setFlags(true)
            }
        } catch (e: Exception) {
            Global.getLogger(this.javaClass).error(e)
        }
    }

    /** Advance ensures the AI remains locked on target,
     * as long as the target is alive. */
    fun advance() {
        try {
            if (lockManeuver == null) return

            if (target?.isAlive != true) {
                unlock()
                return
            }

            val ai = ship.ai as? BasicShipAI ?: return
            if (ai.currentManeuver != lockManeuver) {
                ai.setManeuver(lockManeuver, 100f)
            }

            setFlags(true)

        } catch (e: Exception) {
            Global.getLogger(this.javaClass).error(e)
        }
    }

    private fun setFlags(set: Boolean) {
        flags.forEach { f -> ship.aiFlags.let { if (set) it.setFlag(f) else it.unsetFlag(f) } }
    }

    /** Unlock the AI. Once unlock is called, the
     * LockAIOnTarget object cannot be reused. */
    fun unlock() {
        if (lockManeuver == null) return

        setFlags(false)
        (ship.ai as? BasicShipAI)?.cancelCurrentManeuver()
        lockManeuver = null
    }

    fun isLocked() = lockManeuver != null
}

private class KeepTargetAI(ai: BasicShipAI) : BasicShipAIo(ai) {
    override fun keepTarget() = true
}

private open class BasicShipAIo(private val ai: BasicShipAI) : oO0O.o {
    override fun cancelCurrentManeuver() = ai.cancelCurrentManeuver()

    override fun getAttackingGroup() = ai.attackingGroup

    override fun getEvaluationFor(p0: G?) = ai.getEvaluationFor(p0)

    override fun doesShipVelocityMatterForAim() = ai.doesShipVelocityMatterForAim()

    override fun getCollisionRadius() = ai.collisionRadius

    override fun getLocation(): Vector2f = ai.location

    override fun isFighter() = ai.isFighter

    override fun keepTarget() = ai.keepTarget()

    override fun getMaxFiringRange() = ai.maxFiringRange

    override fun getOptimalNonMissileRange() = ai.optimalNonMissileRange

    override fun setMaxTargetRange(p0: Float) = ai.setMaxTargetRange(p0)

    override fun getAIFlags(): ShipwideAIFlags = ai.aiFlags

    override fun getThreatEvaluator() = ai.threatEvaluator

    override fun getAttackAI(): AttackAIModule = ai.attackAI

    override fun getFlockingAI() = ai.flockingAI

    override fun getShieldAI() = ai.shieldAI

    override fun getEval() = ai.eval

    override fun getConfig(): ShipAIConfig = ai.config
}