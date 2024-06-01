package com.genir.aitweaks.features.shipai.adapters;

import com.fs.starfarer.combat.ai.movement.BasicEngineAI;
import com.fs.starfarer.combat.ai.movement.EngineAI;
import com.fs.starfarer.combat.entities.Ship;

public class EngineAIAdapter implements EngineAI {
    private BasicEngineAI vanillaAI;

    EngineAIAdapter(Ship ship) {
        vanillaAI = new BasicEngineAI(ship);
    }

    @Override
    public void advance(float v) {
    }

    @Override
    public boolean isAlwaysAccelerate() {
        return vanillaAI.isAlwaysAccelerate();
    }

    @Override
    public void setAlwaysAccelerate(boolean b) {
        vanillaAI.setAlwaysAccelerate(b);
    }

    @Override
    public void setDesiredHeading(float v, float v1) {
        vanillaAI.setDesiredHeading(v, v1);
    }

    @Override
    public void setDesiredFacing(float v) {
        vanillaAI.setDesiredFacing(v);
    }

    @Override
    public void setAvoidingCollision(boolean b) {
        vanillaAI.setAvoidingCollision(b);
    }

    @Override
    public void render() {
        vanillaAI.render();
    }
}
