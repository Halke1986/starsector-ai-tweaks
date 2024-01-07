//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package reference;

import com.fs.starfarer.api.combat.BoundsAPI;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Bounds implements Cloneable, BoundsAPI {
    public List<Segment> segments = new ArrayList();
    public List<Segment> origSegments = new ArrayList();
    private float o00000;
    private float Ò00000;

    public Bounds() {
    }

    public void addSegment(float var1, float var2, float var3, float var4) {
        this.o00000 = var3;
        this.Ò00000 = var4;
        this.segments.add(new Segment(var1, var2, var3, var4));
        this.origSegments.add(new Segment(var1, var2, var3, var4));
    }

    public void addSegment(float var1, float var2) {
        this.addSegment(this.o00000, this.Ò00000, var1, var2);
    }

    public void rotateAndTranslate(float facing, Vector2f location) {
        float cosF = (float) Math.cos(Math.toRadians((double) facing));
        float sinF = (float) Math.sin(Math.toRadians((double) facing));

        for (int i = 0; i < this.origSegments.size(); ++i) {
            Segment var6 = (Segment) this.origSegments.get(i);
            Segment var7 = (Segment) this.segments.get(i);
            var7.isBreakEdge = var6.isBreakEdge;
            var7.x1 = var6.x1 * cosF - var6.y1 * sinF + location.x;
            var7.y1 = var6.x1 * sinF + var6.y1 * cosF + location.y;
            var7.x2 = var6.x2 * cosF - var6.y2 * sinF + location.x;
            var7.y2 = var6.x2 * sinF + var6.y2 * cosF + location.y;
            var7.p1.set(var7.x1, var7.y1);
            var7.p2.set(var7.x2, var7.y2);
        }

    }

    public Bounds clone() {
        try {
            Bounds var1 = (Bounds) super.clone();
            var1.segments = new ArrayList();
            Iterator var3 = this.segments.iterator();

            Segment var2;
            while (var3.hasNext()) {
                var2 = (Segment) var3.next();
                var1.segments.add(var2.clone());
            }

            var1.origSegments = new ArrayList();
            var3 = this.origSegments.iterator();

            while (var3.hasNext()) {
                var2 = (Segment) var3.next();
                var1.origSegments.add(var2.clone());
            }

            return var1;
        } catch (CloneNotSupportedException var4) {
            return null;
        }
    }

    public List<BoundsAPI.SegmentAPI> getSegments() {
        return new ArrayList(this.segments);
    }

    public List<BoundsAPI.SegmentAPI> getOrigSegments() {
        return new ArrayList(this.origSegments);
    }

    public void update(Vector2f location, float facing) {
        this.rotateAndTranslate(facing, location);
    }

    public void clear() {
        this.origSegments.clear();
        this.segments.clear();
    }

    public static class Segment implements Cloneable, BoundsAPI.SegmentAPI {
        public boolean isBreakEdge = false;
        public float x1;
        public float y1;
        public float x2;
        public float y2;
        public Vector2f p1;
        public Vector2f p2;

        public Segment(float x1, float y1, float x2, float y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.p1 = new Vector2f(x1, y1);
            this.p2 = new Vector2f(x2, y2);
        }

        public void set(float x1, float y1, float x2, float y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.p1.x = x1;
            this.p1.y = y1;
            this.p2.x = x2;
            this.p2.y = y2;
        }

        public Segment clone() {
            try {
                Segment var1 = (Segment) super.clone();
                var1.p1 = new Vector2f(this.p1);
                var1.p2 = new Vector2f(this.p2);
                return var1;
            } catch (CloneNotSupportedException var2) {
                return null;
            }
        }

        public Vector2f getP1() {
            return this.p1;
        }

        public Vector2f getP2() {
            return this.p2;
        }
    }
}
