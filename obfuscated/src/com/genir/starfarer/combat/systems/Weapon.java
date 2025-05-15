package com.genir.starfarer.combat.systems;

import com.fs.starfarer.api.combat.WeaponAPI;
import com.genir.starfarer.combat.entities.ship.trackers.AimTracker;

// OBFUSCATED
public interface Weapon extends WeaponAPI {
    // UNOBFUSCATED
    AimTracker getAimTracker();
}
