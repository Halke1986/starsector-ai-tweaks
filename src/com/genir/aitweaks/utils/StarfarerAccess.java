package com.genir.aitweaks.utils;

import com.fs.starfarer.combat.CombatState;
import com.fs.starfarer.combat.entities.Ship;

/**
 * Java wrapper around access to starfarer classes.
 * Accessing the classes in Kotlin triggers false-positive
 * error inspection in Intellij.
 */
public class StarfarerAccess {
    public static void setShipAI(Ship ship, Ship.ShipAIWrapper ai) {
        ship.setAI(ai);
    }

    public static boolean getAutoOmni() {
        return CombatState.AUTO_OMNI_SHIELDS;
    }

    public static void setAutoOmni(boolean val) {
        CombatState.AUTO_OMNI_SHIELDS = val;
    }
}
