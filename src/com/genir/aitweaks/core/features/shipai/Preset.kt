package com.genir.aitweaks.core.features.shipai

class Preset {
    @Suppress("ConstPropertyName")
    companion object {
        const val threatEvalRadius = 2500f
        const val aimOffsetSamples = 45

        const val effectiveDpsThreshold = 0.80f

        // Flux management
        const val backoffUpperThreshold = 0.75f
        const val backoffLowerThreshold = backoffUpperThreshold * 0.6f // will force vent
        const val holdFireThreshold = 0.9f
        const val damageHistoryDuration = 4f

        // Idle time calculation
        const val shieldDownVentTime = 2.0f
        const val shieldFlickerThreshold = 0.5f

        // Map movement calculation
        const val arrivedAtLocationRadius = 2000f
        const val borderCornerRadius = 4000f
        const val borderNoGoZone = 3000f
        const val borderHardNoGoZone = borderNoGoZone / 2f

        // Collision avoidance
        const val collisionBuffer = 30f

        // Broadside
        const val broadsideDPSThreshold = 1.33f
        const val broadsideFacingPadding = 7f
        const val maxBroadsideFacing = 80f

        const val weaponMaxReloadTime = 12f
    }

    class BurnDrive {
        @Suppress("ConstPropertyName")
        companion object {
            const val approachToMinRangeFraction = 0.75f
            const val maxAngleToTarget = 45f
            const val stopBeforeCollision = 0.2f // seconds
            const val ignoreMassFraction = 0.25f
            const val minBurnDistFraction = 0.33f
        }
    }
}
