package com.fs.starfarer.combat.collision;

import org.lwjgl.util.vector.Vector2f;

import java.util.List;

// OBFUSCATED
public class Bounds {
    // UNOBFUSCATED
    public List<Segment> origSegments;

    // OBFUSCATED
    public static class Segment {
        public float x1;
        public float y1;
        public float x2;
        public float y2;
        public Vector2f p1;
        public Vector2f p2;
    }
}
