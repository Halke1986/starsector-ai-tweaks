package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.Global
import kotlin.math.max

class Preset {
    companion object {
        val threatSearchRange: Float
            get() = mapSize / 8f

        // Flux management
        const val holdFireThreshold = 0.93f
        const val damageHistoryDuration = 4f

        // Map movement calculation
        val arrivedAtLocationRadius: Float
            get() = mapSize / 15f

        const val borderCornerRadius = 4000f
        const val borderNoGoZone = 3000f
        const val borderHardNoGoZone = borderNoGoZone / 2f

        // Collision avoidance
        const val hulkCollisionSizeFactor = 0.85f
        const val enemyCollisionSizeFactor = 2f

        // Targeting & Weapon groups
        const val weaponGroupPerformanceThreshold = 0.93f
        const val noWeaponsAttackRange = 2000f
        const val effectiveDpsThreshold = 0.80f
        const val targetThickness = 60f
        const val assaultShipApproachFactor = 0.875f
        const val fullAssaultApproachFactor = 0.666f

        private val mapSize: Float
            get() {
                val engine = Global.getCombatEngine()
                return max(engine.mapHeight, engine.mapWidth)
            }
    }
}
