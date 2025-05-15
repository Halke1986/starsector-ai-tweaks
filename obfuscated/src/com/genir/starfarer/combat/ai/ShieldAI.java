package com.genir.starfarer.combat.ai;

import com.genir.starfarer.combat.entities.Ship;
import org.lwjgl.util.vector.Vector2f;

// OBFUSCATED
public interface ShieldAI {
    // OBFUSCATED
    void shieldAI_advance(float dt, ThreatEvaluator threatEvalAI, Vector2f missileDangerDir, Vector2f collisionDangerDir, Ship target);
}
