package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.Global
import kotlin.math.max

class Preset {
    companion object {
        val threatSearchRange: Float
            get() = mapSize / 8f

        // Flux management
        const val backoffUpperThreshold = 0.75f
        const val backoffLowerThreshold = 0.1f
        const val holdFireThreshold = 0.93f
        const val damageHistoryDuration = 4f

        // Map movement calculation
        val arrivedAtLocationRadius: Float
            get() = mapSize / 15f

        const val borderCornerRadius = 4000f
        const val borderNoGoZone = 3000f
        const val borderHardNoGoZone = borderNoGoZone / 2f

        // Collision avoidance
        const val collisionBuffer = 30f
        const val hulkSizeFactor = 0.95f

        // Targeting & Weapon groups
        const val validWeaponGroupDPSThreshold = 0.9f
        const val noWeaponsAttackRange = 2000f
        const val weaponMaxReloadTime = 12f
        const val effectiveDpsThreshold = 0.80f
        const val targetThickness = 60f

        private val mapSize: Float
            get() {
                val engine = Global.getCombatEngine()
                return max(engine.mapHeight, engine.mapWidth)
            }
    }
}
