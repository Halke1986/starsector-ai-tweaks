package com.genir.aitweaks.features.shipai.adapters;

import com.fs.starfarer.combat.entities.Ship;

public class Strafe extends ManeuverAdapter {
    public Strafe(Ship ship, Ship target, boolean p2, float p3, float p4, FlockingAI p5, ShipAI p6, boolean p7) {
        super(ship, target, null);
    }

    public Strafe(Ship ship, Ship target, boolean p2, float p3, float p4, float p5, FlockingAI p6, ShipAI p7, boolean p8) {
        super(ship, target, null);
    }

    float getDesiredStrafeHeadingObf(boolean p0) {
        return maneuver.getDesiredHeading();
    }
}
