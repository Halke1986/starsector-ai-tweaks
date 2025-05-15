package com.genir.starfarer.combat.ai.system;

import com.genir.starfarer.combat.entities.Ship;
import org.lwjgl.util.vector.Vector2f;

// OBFUSCATED
public interface SystemAI {
    // OBFUSCATED
    void systemAI_advance(float dt, Vector2f missileDangerDir, Vector2f collisionDangerDir, Ship target);
}
