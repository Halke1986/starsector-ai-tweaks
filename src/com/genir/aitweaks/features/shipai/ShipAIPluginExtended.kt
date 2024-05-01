package com.genir.aitweaks.features.shipai

import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.combat.ai.movement.maneuvers.oO0O

interface ShipAIPluginExtended : ShipAIPlugin {
    fun getCurrentManeuver(): oO0O
}