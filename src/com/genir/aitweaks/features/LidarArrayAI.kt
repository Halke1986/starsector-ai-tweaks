package com.genir.aitweaks.features

import com.fs.starfarer.api.combat.*
import com.genir.aitweaks.debugPlugin
import com.genir.aitweaks.utils.ShipTargetTracker
import org.lwjgl.util.vector.Vector2f

class LidarArrayAI : ShipSystemAIScript {
    private var ai: LidarArrayAIImpl? = null

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        ship ?: return
        system ?: return
        flags ?: return
        engine ?: return

        this.ai = LidarArrayAIImpl(ship, system, flags, engine)
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        ai?.advance()
    }
}

class LidarArrayAIImpl(private val ship: ShipAPI, private val system: ShipSystemAPI, private val flags: ShipwideAIFlags, private val engine: CombatEngineAPI) {
    private val targetTracker = ShipTargetTracker(ship)

    fun advance() {
        targetTracker.advance()

        debugPlugin[0] = targetTracker.target?.toString() ?: "null"
    }
}