package com.genir.aitweaks.features.shipai.adapters;

import com.fs.starfarer.api.combat.CombatEntityAPI;

public interface ManeuverInterface {
    void advanceObf(float p0);

    CombatEntityAPI getTargetObf();

    boolean isDirectControlObf();

    void doManeuverObf();

    float getDesiredHeadingObf();

    float getDesiredFacingObf();
}
