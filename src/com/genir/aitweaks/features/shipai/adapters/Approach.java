package com.genir.aitweaks.features.shipai.adapters;

import com.fs.starfarer.combat.entities.Ship;

public class Approach extends ManeuverAdapter {
    Approach(Ship ship, Ship target, float var3, FlockingAI flockingAI, ShipAI shipAI) {
        super(ship, target, null);
    }
}
