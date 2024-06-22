package com.genir.aitweaks.core.features.shipai.adapters;

import com.fs.starfarer.combat.entities.Ship;
import org.lwjgl.util.vector.Vector2f;

public class Move extends ManeuverAdapter {
    public Move(Ship ship, Vector2f location, ShipAI shipAI) {
        super(ship, null, location);
    }
}
