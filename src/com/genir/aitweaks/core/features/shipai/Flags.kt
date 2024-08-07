package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.ShipwideAIFlags

val respectedFlags = listOf(
    // Overrides distance from maneuver target and BurnDriveToggle approach distance.
    // Set by LidarArray.
    ShipwideAIFlags.AIFlags.MANEUVER_RANGE_FROM_TARGET
)
