package com.genir.aitweaks.features.shipai.adapters;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.combat.entities.Ship;
import com.genir.aitweaks.features.shipai.Maneuver;
import org.lwjgl.util.vector.Vector2f;

public class ManeuverAdapter implements ManeuverInterface {
    protected Maneuver maneuver;

    ManeuverAdapter(Ship ship, Ship target, Vector2f location) {
        maneuver = new Maneuver(ship, target, location);
    }

    @Override
    public void advanceObf(float p0) {
        maneuver.advance(p0);
    }

    @Override
    public CombatEntityAPI getTargetObf() {
        return maneuver.getManeuverTarget();
    }

    @Override
    public boolean isDirectControlObf() {
        return true;
    }

    @Override
    public void doManeuverObf() {
        maneuver.doManeuver();
    }

    @Override
    public float getDesiredHeadingObf() {
        return maneuver.getDesiredHeading();
    }

    @Override
    public float getDesiredFacingObf() {
        return maneuver.getDesiredFacing();
    }
}
