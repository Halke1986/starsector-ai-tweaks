package com.fs.starfarer.combat.systems;

import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.combat.entities.ship.trackers.AimTracker;

// OBFUSCATED
public interface Weapon extends WeaponAPI {
    // UNOBFUSCATED
    AimTracker getAimTracker();
}
