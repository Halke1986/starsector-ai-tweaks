package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.Global
import kotlin.math.max

class Preset {
    @Suppress("ConstPropertyName")
    companion object {
        val threatSearchRange: Float
            get() = mapSize / 8f

        const val aimOffsetSamples = 45

        const val effectiveDpsThreshold = 0.80f

        // Flux management
        const val backoffUpperThreshold = 0.75f
        const val forceVentThreshold = backoffUpperThreshold * 0.6f // will force vent
        const val backoffLowerThreshold = 0.1f
        const val holdFireThreshold = 0.93f
        const val damageHistoryDuration = 4f

        // Idle time calculation
        const val shieldDownVentTime = 2.0f
        const val shieldFlickerThreshold = 0.5f

        // Map movement calculation
        val arrivedAtLocationRadius: Float
            get() = mapSize / 15f

        const val borderCornerRadius = 4000f
        const val borderNoGoZone = 3000f
        const val borderHardNoGoZone = borderNoGoZone / 2f

        // Collision avoidance
        const val collisionBuffer = 30f

        // Weapon groups
        const val validWeaponGroupDPSThreshold = 0.9f

        const val noWeaponsAttackRange = 2000f
        const val weaponMaxReloadTime = 12f


        private val mapSize: Float
            get() {
                val engine = Global.getCombatEngine()
                return max(engine.mapHeight, engine.mapWidth)
            }
    }
}
