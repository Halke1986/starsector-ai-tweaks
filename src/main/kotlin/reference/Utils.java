//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package reference;

//import com.fs.graphics.Sprite;
//import com.fs.starfarer.Object;
//import com.fs.starfarer.api.campaign.events.CampaignEventPlugin;
//import com.fs.starfarer.api.combat.CombatReadinessPlugin;
//import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
//import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
//import com.fs.starfarer.api.fleet.FleetMemberAPI;
//import com.fs.starfarer.api.fleet.FleetMemberType;
//import com.fs.starfarer.api.impl.combat.PhaseCloakStats;
//import com.fs.starfarer.api.util.Misc;
//import com.fs.starfarer.campaign.fleet.CampaignFleet;
//import com.fs.starfarer.campaign.fleet.CrewComposition;
//import com.fs.starfarer.campaign.fleet.FleetMember;
//import com.fs.starfarer.combat.CombatEngine;
//import com.fs.starfarer.combat.E.B;
//import com.fs.starfarer.combat.entities.Ship;
//import com.fs.starfarer.combat.entities.ship.class;
//import com.fs.starfarer.loading.E;
//import com.fs.starfarer.loading.SkillSpec;
//import com.fs.starfarer.loading.SpecStore;
//import com.fs.starfarer.loading.StarfarerStrings;
//import com.fs.starfarer.loading.specs.HullVariantSpec;
//import com.fs.starfarer.loading.specs.oO0O;
//import com.fs.starfarer.loading.specs.private;
//import com.fs.starfarer.loading.specs.voidsuper;
//import com.fs.starfarer.renderers.A.I;
//import com.fs.starfarer.settings.StarfarerSettings;
//import com.fs.starfarer.title.Object.G;
//import com.fs.starfarer.title.Object.new;
//import com.fs.starfarer.ui.OoO0;
//import com.fs.starfarer.ui.Ooo0;
//import com.fs.starfarer.ui.b;
//import com.fs.starfarer.ui.donew;
//import com.fs.starfarer.ui.dosuper;
//import com.fs.starfarer.ui.interfacenew;
//import com.fs.starfarer.ui.A.OoOO;
//import com.fs.starfarer.ui.impl.StandardTooltipV2;
//import java.awt.Color;
//import java.io.File;
//import java.io.IOException;
//import java.lang.management.ManagementFactory;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

import org.lwjgl.util.vector.Vector2f;

import static reference.UtilsAlias.*;

//import org.lwjgl.util.vector.Vector3f;

public class Utils {
    public static float ÓO0000 = 0.017453292F;
    public static float class_ = 57.295784F; // "class"
    //    private static Vector2f OO0000 = new Vector2f();
//    public static Vector2f ÔO0000 = new Vector2f(0.0F, 0.0F);
//    private static Vector2f õ00000 = null;
//    private static Vector2f new = new Vector2f();
//    private static Vector3f o00000 = new Vector3f();
//    private static Vector2f interface = new Vector2f();
//    private static final int null = 10;
//    private static final int Ö00000 = 20;
//    private static final int float = 1048575;
//    private static final int Ô00000 = 1048576;
    private static final int int1024 = (int) Math.sqrt(1048576.0);
    private static final float float2toPowerMinus10;
    //    private static final float ö00000 = 57.295776F;
    private static final float[] atan2Lookup; // "while"
//    public static final String oO0000 = "sun.java.command";

    static {
        float2toPowerMinus10 = 1.0F / (float) (int1024 - 1);
        atan2Lookup = new float[1048576];

        for (int var0 = 0; var0 < int1024; ++var0) {
            for (int var1 = 0; var1 < int1024; ++var1) {
                float var2 = (float) var0 / (float) int1024;
                float var3 = (float) var1 / (float) int1024;
                atan2Lookup[var1 * int1024 + var0] = (float) Math.atan2((double) var3, (double) var2);
            }
        }
    }

    public Utils() {
    }
//
//    public static float o00000(voidsuper var0, Ship var1) {
//        if (var1 != null && var1.getHullSpec().getHints().contains(ShipTypeHints.WEAPONS_FRONT_TO_BACK)) {
//            return Math.abs(var0.getNode().new().y) / 50000.0F + var0.getNode().new().x / 10000.0F;
//        } else {
//            return var1 != null && var1.getHullSpec().getHints().contains(ShipTypeHints.WEAPONS_BACK_TO_FRONT) ? Math.abs(var0.getNode().new().y) / 50000.0F - var0.getNode().new().x / 10000.0F : Math.abs(var0.getNode().new().y) / 10000.0F + Math.abs(var0.getNode().new().x / 1.0E7F);
//        }
//    }
//
//    public static float o00000() {
//        return (float)Math.random() > 0.5F ? 1.0F : -1.0F;
//    }
//
//    public static Vector2f o00000(Vector2f var0, float var1) {
//        float var2 = (float)Math.cos((double)(var1 * ÓO0000));
//        float var3 = (float)Math.sin((double)(var1 * ÓO0000));
//        Vector2f var4 = new Vector2f();
//        var4.x = var0.x * var2 - var0.y * var3;
//        var4.y = var0.x * var3 + var0.y * var2;
//        return var4;
//    }
//
//    public static Vector2f o00000(Vector2f var0, Vector2f var1, float var2) {
//        float var3 = (float)Math.cos((double)(var2 * ÓO0000));
//        float var4 = (float)Math.sin((double)(var2 * ÓO0000));
//        new Vector2f();
//        var0.x += var1.x * var3 - var1.y * var4;
//        var0.y += var1.x * var4 + var1.y * var3;
//        return var0;
//    }
//
//    public static Vector2f o00000(Vector2f var0, Vector2f var1, float var2, float var3, float var4, float var5, float var6) {
//        Vector2f var7 = new Vector2f(var0);
//        Vector2f var8 = new Vector2f(var1);
//        Vector2f var9 = Object(var2);
//        float var10 = 0.75F;
//        float var11 = 1.0F - var10;
//        float var13 = var8.length() / var3;
//        if (var13 > 1.0F) {
//            var13 = 1.0F;
//        }
//
//        o00000(var8);
//        var9.scale(var10 * var6);
//        var8.scale(var13 * var11 * var6);
//        Vector2f.add(var9, var8, OO0000);
//        if (õ00000 == null) {
//            õ00000 = new Vector2f();
//            õ00000.set(var0);
//        }
//
//        Vector2f var15 = ô00000(OO0000, ÔO0000);
//        if (var15.length() > 1.0F) {
//            var15.scale(var5 * 2.0F);
//            float var16 = 20.0F * var5;
//            if (var15.length() < var16) {
//                o00000(var15);
//                var15.scale(var16);
//            }
//        } else {
//            var15.set(0.0F, 0.0F);
//            ÔO0000.set(OO0000);
//        }
//
//        ÔO0000.x += var15.x;
//        ÔO0000.y += var15.y;
//        õ00000.x = var7.x + ÔO0000.x;
//        õ00000.y = var7.y + ÔO0000.y;
//        return new Vector2f(õ00000);
//    }
//
//    public static float Õ00000(float var0, float var1) {
//        return 2.0F * var0 / (6.2831855F * var1) * 360.0F;
//    }

    // UtilsAlias.DirectionalVector
    public static Vector2f Object(float facing) {
        Vector2f var1 = new Vector2f();
        float var2 = facing * degToRadConversion;
        var1.x = (float) Math.cos((double) var2);
        var1.y = (float) Math.sin((double) var2);
        return var1;
    }

//    public static Vector2f Ò00000(float var0) {
//        Vector2f var1 = new Vector2f();
//        var1.x = (float)Math.cos((double)var0);
//        var1.y = (float)Math.sin((double)var0);
//        return var1;
//    }

    // UtilsAlias.shortestRotationToTarget
    public static float Ò00000(float weaponFacing, Vector2f weaponLocation, Vector2f targetLocation) {
        Vector2f var3 = Vector2f.sub(targetLocation, weaponLocation, new Vector2f());
        return shortestRotation(weaponFacing, getFacing(var3));
    }

//    public static float Ô00000(Vector2f var0, Vector2f var1) {
//        return o00000(Object(var0), Object(var1));
//    }

    // UtilsAlias.shortestRotation
    public static float o00000(float from, float to) {
        float var2 = clampAngle(from - to);
        return var2 > 180.0F ? 360.0F - var2 : var2;
    }

//    public static Vector2f o00000(Vector2f var0) {
//        return var0.lengthSquared() > Float.MIN_VALUE ? (Vector2f)var0.normalise() : var0;
//    }
//
//    public static Vector2f Ô00000(Vector2f var0) {
//        Vector2f var1 = new Vector2f();
//        var1.x = var0.y;
//        var1.y = -var0.x;
//        return var1;
//    }

    // UtilsAlias.distance
    public static float Ø00000(Vector2f var0, Vector2f var1) {
        return (float) Math.sqrt((double) ((var0.x - var1.x) * (var0.x - var1.x) + (var0.y - var1.y) * (var0.y - var1.y)));
    }

//    public static float return(Vector2f var0, Vector2f var1) {
//        return (var0.x - var1.x) * (var0.x - var1.x) + (var0.y - var1.y) * (var0.y - var1.y);
//    }
//
//    public static float Ò00000(Vector2f var0, Vector2f var1) {
//        float var2 = StarfarerSettings.Ó00000();
//        float var3 = Ø00000(var0, var1) / var2;
//        return var3;
//    }
//
//    public static float o00000(Vector3f var0, Vector3f var1) {
//        return Vector3f.sub(var0, var1, o00000).length();
//    }
//
//    public static Vector2f ô00000(Vector2f var0, Vector2f var1) {
//        return Vector2f.sub(var0, var1, new Vector2f());
//    }
//
//    public static float Ò00000(Vector2f var0) {
//        return (float)Math.atan2((double)var0.y, (double)var0.x) * class;
//    }

    // UtilsAlias.getFacing
    public static float Object(Vector2f var0) {
        return atan2(var0.y, var0.x) * radToDegConversion;
    }

//    public static float Õ00000(Vector2f var0, Vector2f var1) {
//        float var2 = (float)Math.atan2((double)(var1.y - var0.y), (double)(var1.x - var0.x)) * class;
//        return var2;
//    }

    // UtilsAlias.angleTowards
    public static float Object(Vector2f from, Vector2f to) {
        return atan2(to.y - from.y, to.x - from.x) * radToDegConversion;
    }

    // UtilsAlias.willHitShield
    public static boolean o00000(float shieldFacing, float shieldArc, Vector2f shieldLocation, Vector2f weaponLocation) {
        if (shieldArc >= 360.0F) {
            return true;
        } else {
            if (shieldFacing < 0.0F) {
                shieldFacing += 360.0F;
            }

            Vector2f var4 = new Vector2f(weaponLocation.x - shieldLocation.x, weaponLocation.y - shieldLocation.y);
            if (var4.lengthSquared() == 0.0F) {
                return false;
            } else {
                float var5 = getFacing(var4);
                if (var5 < 0.0F) {
                    var5 += 360.0F;
                }

                float var6 = shieldFacing - shieldArc / 2.0F;
                if (var6 < 0.0F) {
                    var6 += 360.0F;
                }

                if (var6 > 360.0F) {
                    var6 -= 360.0F;
                }

                float var7 = shieldFacing + shieldArc / 2.0F;
                if (var7 < 0.0F) {
                    var7 += 360.0F;
                }

                if (var7 > 360.0F) {
                    var7 -= 360.0F;
                }

                if (var5 >= var6 && var5 <= var7) {
                    return true;
                } else if (var5 >= var6 && var6 > var7) {
                    return true;
                } else {
                    return var5 <= var7 && var6 > var7;
                }
            }
        }
    }

//    public static boolean o00000(Vector2f var0, Vector2f var1, float var2, Vector2f var3, Vector2f var4) {
//        return o00000(Object(var0, var1), var2, var3, var4);
//    }
//
//    public static boolean Ô00000(float var0, float var1, float var2) {
//        if (var1 < 0.0F) {
//            return false;
//        } else {
//            var0 = o00000(var0);
//            var2 = o00000(var2);
//            float var3 = Math.abs(var2 - var0) - var1 / 2.0F;
//            float var4 = Math.abs(360.0F - Math.abs(var2 - var0)) - var1 / 2.0F;
//            return var3 <= 0.0F || var4 <= 0.0F;
//        }
//    }
//
//    public static float Ò00000(float var0, float var1, float var2) {
//        if (Ô00000(var0, var1, var2)) {
//            return var2;
//        } else {
//            float var3 = o00000(var2, var0 - var1 / 2.0F);
//            float var4 = o00000(var2, var0 + var1 / 2.0F);
//            return o00000(var3 > var4 ? var0 + var1 / 2.0F : var0 - var1 / 2.0F);
//        }
//    }

    // UtilsAlias.clampAngle
    public static float o00000(float var0) {
        return (var0 % 360.0F + 360.0F) % 360.0F;
    }

//    public static float Object(float var0, float var1, float var2) {
//        var0 = o00000(var0);
//        var2 = o00000(var2);
//        float var3 = Math.abs(var2 - var0) - var1 / 2.0F;
//        float var4 = Math.abs(360.0F - Math.abs(var2 - var0)) - var1 / 2.0F;
//        if (!(var3 <= 0.0F) && !(var4 <= 0.0F)) {
//            return var3 > var4 ? var4 : var3;
//        } else {
//            return 0.0F;
//        }
//    }
//
//    public static float o00000(float var0, Vector2f var1, Vector2f var2) {
//        float var3 = Misc.normalizeAngle(Object(var1, var2)) - Misc.normalizeAngle(var0);
//        if (var3 < 0.0F) {
//            var3 += 360.0F;
//        }
//
//        if (var3 != 0.0F && var3 != 360.0F) {
//            return var3 > 180.0F ? -1.0F : 1.0F;
//        } else {
//            return 0.0F;
//        }
//    }
//
//    public static float o00000(Vector2f var0, Vector2f var1) {
//        return o00000(Object(var0), new Vector2f(0.0F, 0.0F), var1);
//    }
//
//    public static float Ò00000(float var0, float var1) {
//        float var2 = Misc.normalizeAngle(var1) - Misc.normalizeAngle(var0);
//        if (var2 < 0.0F) {
//            var2 += 360.0F;
//        }
//
//        if (var2 != 0.0F && var2 != 360.0F) {
//            return var2 > 180.0F ? -1.0F : 1.0F;
//        } else {
//            return 0.0F;
//        }
//    }
//
//    public static boolean o00000(float var0, float var1, float var2) {
//        var0 = o00000(var0);
//        var1 = o00000(var1);
//        var2 = o00000(var2);
//        if (var2 >= var0 && var2 <= var1) {
//            return true;
//        } else {
//            if (var0 > var1) {
//                if (var2 <= var1) {
//                    return true;
//                }
//
//                if (var2 >= var0) {
//                    return true;
//                }
//            }
//
//            return false;
//        }
//    }
//
//    public static boolean o00000(Vector2f var0, Vector2f var1, float var2, float var3) {
//        float var4 = Ø00000(var0, var1);
//        float var5 = var2 * var3;
//        if (var4 < var5) {
//            var0.set(var1);
//            return true;
//        } else {
//            Vector2f var6 = o00000(Vector2f.sub(var1, var0, new Vector2f()));
//            var0.x += var6.x * var5;
//            var0.y += var6.y * var5;
//            return false;
//        }
//    }
//
//    public static float Ò00000(float var0, float var1, float var2, float var3) {
//        if (var3 >= 360.0F) {
//            return Ò00000(var0, var1);
//        } else {
//            var0 = o00000(var0);
//            var1 = o00000(var1);
//            if (!Ô00000(var2, var3, var1)) {
//                return Ò00000(var0, var1);
//            } else {
//                float var4 = o00000(var2 + 180.0F);
//                if (var0 == var1) {
//                    return 0.0F;
//                } else {
//                    return o00000(var0, var1, var4) ? -1.0F : 1.0F;
//                }
//            }
//        }
//    }
//
//    public static Color o00000(int var0) {
//        if (var0 == CombatEngine.getInstance().getPlayerId()) {
//            return Object.ôÕ0000;
//        } else {
//            return var0 == 100 ? Object.return.Object : Object.ÒÔ0000;
//        }
//    }
//
//    public static Color o00000(int var0, boolean var1) {
//        if (var0 == CombatEngine.getInstance().getPlayerId()) {
//            return var1 ? Object.oÒ0000 : Object.ø00000;
//        } else {
//            return var0 == 100 ? Object.return.Object : Object.return;
//        }
//    }
//
//    public static Color Ò00000(int var0, boolean var1) {
//        if (var0 == CombatEngine.getInstance().getPlayerId()) {
//            return var1 ? Object.oÒ0000 : Object.ø00000;
//        } else {
//            return var0 == 100 ? Object.ÒÕ0000 : Object.return;
//        }
//    }
//
//    public static Color o00000(CampaignFleet var0) {
//        return var0.getFaction().getBaseUIColor();
//    }
//
//    public static void o00000(Sprite var0, float var1, float var2) {
//        if (var0 != null) {
//            float var3 = var0.getWidth() / var0.getHeight();
//            float var4 = var2 * var3;
//            float var5 = var2;
//            if (var4 > var1) {
//                var4 = var1;
//                var5 = var1 / var3;
//            }
//
//            var0.setSize(var4, var5);
//        }
//    }
//
//    public static Vector2f o00000(float var0, float var1, float var2, float var3) {
//        float var4 = var0 / var1;
//        float var5 = var3 * var4;
//        float var6 = var3;
//        if (var5 > var2) {
//            var5 = var2;
//            var6 = var2 / var4;
//        }
//
//        return new Vector2f(var5, var6);
//    }
//
//    public static void o00000(Color var0, interfacenew var1) {
//        Iterator var3 = var1.getChildrenCopy().iterator();
//
//        while(var3.hasNext()) {
//            b var2 = (b)var3.next();
//            if (var2 instanceof dosuper) {
//                ((dosuper)var2).getRenderer().new(var0);
//            } else if (var2 instanceof I) {
//                ((I)var2).setColor(var0);
//            } else if (var2 instanceof OoO0) {
//                ((OoO0)var2).setColor(var0);
//            }
//        }
//
//    }
//
//    public static void o00000(Ooo0 var0, Color var1) {
//        o00000(var0, var1, 0.0F);
//    }
//
//    public static void o00000(Ooo0 var0, Color var1, float var2) {
//        var2 = 0.0F;
//        OoOO var3 = (OoOO)var0.getRenderer();
//        G var4 = new G(new new(var3.Õo0000(), var1));
//        var4.getFader().setDuration(0.0F, 0.0F);
//        var4.getFader().forceOut();
//        var0.setTooltip(var2, var4);
//    }
//
//    public static void o00000(String var0, String var1, donew var2) {
//        StarfarerStrings.o var3 = StarfarerStrings.Ò00000(var0, var1);
//        o00000((String)var3.new, (String)var3.o00000, (com.fs.starfarer.title.A.OoOO.oo)null, 0.0F, (donew)var2);
//    }
//
//    public static void o00000(String var0, String var1, com.fs.starfarer.title.A.OoOO.oo var2, donew var3) {
//        StarfarerStrings.o var4 = StarfarerStrings.Ò00000(var0, var1);
//        o00000(var4.new, var4.o00000, var2, 0.0F, var3);
//    }
//
//    public static StandardTooltipV2 o00000(String var0, String var1, com.fs.starfarer.title.A.OoOO.oo var2, float var3, donew var4) {
//        var3 = 0.0F;
//        StandardTooltipV2 var5 = StandardTooltipV2.createTextTooltip(var0, var1);
//        var5.getFader().setDuration(0.0F, 0.0F);
//        var4.setTooltip(var3, var5);
//        return var5;
//    }
//
//    public static float Object(List<Float> var0) {
//        if (var0.isEmpty()) {
//            return 0.0F;
//        } else {
//            Collections.sort(var0);
//            var0.add((Float)var0.get(0));
//            float var1 = 0.0F;
//            float var2 = (Float)var0.get(0);
//            int var3 = var0.size() - 1;
//
//            for(int var4 = 1; var4 < var0.size(); ++var4) {
//                float var5;
//                if (var4 == var3) {
//                    var5 = (Float)var0.get(var4) + 360.0F - var2;
//                } else {
//                    var5 = (Float)var0.get(var4) - var2;
//                }
//
//                if (var5 > var1) {
//                    var1 = var5;
//                }
//
//                var2 = (Float)var0.get(var4);
//            }
//
//            return var1;
//        }
//    }
//
//    public static float o00000(List<Float> var0) {
//        if (var0.isEmpty()) {
//            return 0.0F;
//        } else {
//            Collections.sort(var0);
//            var0.add((Float)var0.get(0));
//            float var1 = 0.0F;
//            float var2 = (Float)var0.get(0);
//            float var3 = -1.0F;
//            int var4 = var0.size() - 1;
//
//            for(int var5 = 1; var5 < var0.size(); ++var5) {
//                float var6;
//                if (var5 == var4) {
//                    var6 = (Float)var0.get(var5) + 360.0F - var2;
//                } else {
//                    var6 = (Float)var0.get(var5) - var2;
//                }
//
//                if (var6 > var1) {
//                    var1 = var6;
//                    var3 = var2 + var6 / 2.0F;
//                }
//
//                var2 = (Float)var0.get(var5);
//            }
//
//            return var3;
//        }
//    }
//
//    public static void Ò00000(Vector2f var0, Vector2f var1, float var2, float var3, float var4) {
//        float var5 = var1.x - var0.x;
//        float var6 = var1.y - var0.y;
//        float var7 = (var2 * Math.signum(var5) + var5 * var3) * var4;
//        float var8 = (var2 * Math.signum(var6) + var6 * var3) * var4;
//        if (Math.abs(var7) > Math.abs(var5)) {
//            var7 = var5;
//        }
//
//        if (Math.abs(var8) > Math.abs(var6)) {
//            var8 = var6;
//        }
//
//        var0.x += var7;
//        var0.y += var8;
//    }
//
//    public static void o00000(Vector2f var0, Vector2f var1, float var2, float var3, float var4) {
//        float var5 = Misc.getDistance(var0, var1);
//        float var6 = (var2 * Math.signum(var5) + var5 * var3) * var4;
//        if (Math.abs(var6) > Math.abs(var5)) {
//            var6 = var5;
//        }
//
//        Vector2f var7 = Vector2f.sub(var1, var0, new Vector2f());
//        o00000(var7);
//        var7.scale(var6);
//        Vector2f.add(var0, var7, var0);
//    }
//
//    public static float o00000(float var0, float var1, float var2, float var3, float var4, float var5) {
//        float var6 = o00000(var0, var1);
//        float var7 = (var3 + var6 * var4) * var5;
//        if (var7 > var6) {
//            var7 = var6;
//        }
//
//        return var0 + Math.abs(var7) * var2;
//    }
//
//    public static float o00000(float var0, float var1, float var2, float var3, float var4) {
//        float var5 = var1 - var0;
//        float var6 = (Math.signum(var5) * var2 + var5 * var3) * var4;
//        if (Math.abs(var6) > Math.abs(var5)) {
//            var6 = var5;
//        }
//
//        return var0 + var6;
//    }
//
//    public static void Ò00000(Vector2f var0, Vector2f var1, float var2, float var3) {
//        var0.x += var1.x * var3;
//        var0.y += var1.y * var3;
//        float var4 = var0.length();
//        if (var4 >= var2) {
//            var0.scale(var2 / var4);
//        }
//
//    }
//
//    public static float o00000(B var0, B var1) {
//        if (var0 != null && var1 != null) {
//            Vector2f var2 = new Vector2f(var0.getLocation());
//            Vector2f var3 = new Vector2f(var1.getLocation());
//            var2.x += var0.getVelocity().x * 1.0F;
//            var2.y += var0.getVelocity().y * 1.0F;
//            var3.x += var1.getVelocity().x * 1.0F;
//            var3.y += var1.getVelocity().y * 1.0F;
//            float var4 = Object(var0.getLocation(), var1.getLocation());
//            float var5 = Object(var2, var3);
//            return o00000(var4, var5) * Ò00000(var4, var5);
//        } else {
//            return 0.0F;
//        }
//    }
//
//    public static o o00000(com.fs.graphics.A.Object var0, CampaignEventPlugin var1, String var2) {
//        Map var3 = var1.getTokenReplacements();
//        ArrayList var4 = new ArrayList(var3.keySet());
//        Collections.sort(var4, new Comparator<String>() {
//            public int o00000(String var1, String var2) {
//                return var2.length() - var1.length();
//            }
//        });
//        String var5 = var0.oO0000();
//        String var6;
//        String var8;
//        if (var5 != null && !var5.equals("")) {
//            for(Iterator var7 = var4.iterator(); var7.hasNext(); var5 = var5.replaceAll("(?s)\\" + var6, var8)) {
//                var6 = (String)var7.next();
//                var8 = (String)var3.get(var6);
//                if (var8 == null) {
//                    var8 = "null";
//                }
//            }
//        }
//
//        var0.Ó00000(var5);
//        o var9 = o00000(var5);
//        if (var9 != null) {
//            var0.Ó00000(var9.Ô00000);
//        }
//
//        return var9;
//    }
//
//    public static o o00000(String var0) {
//        if (var0.indexOf("<c") == -1) {
//            return null;
//        } else {
//            o var1 = new o();
//            Pattern var2 = Pattern.compile("(?s)(.*?)<c((=(.*?)>(.*?)</c>)|(>(.*?)</c>))");
//            Matcher var3 = var2.matcher(var0);
//            boolean[] var4 = new boolean[var0.length()];
//            int[] var5 = new int[var0.length()];
//            ArrayList var6 = new ArrayList();
//            int var7 = -1;
//            int var8 = 0;
//
//            for(int var9 = 0; var3.find(); var7 = var3.end()) {
//                String var10 = var3.group(1);
//                String var11 = var3.group(4);
//                String var12 = var3.group(5);
//                Color var13 = StarfarerSettings.float(var11);
//                var6.add(var13);
//                var1.Ô00000 = var1.Ô00000 + var10 + var12;
//
//                for(int var14 = var8; var14 < var1.Ô00000.length(); ++var14) {
//                    if (var14 < var1.Ô00000.length() - var12.length()) {
//                        var4[var14] = false;
//                    } else {
//                        var4[var14] = true;
//                        var5[var14] = var9;
//                    }
//                }
//
//                ++var9;
//                var8 = var1.Ô00000.length();
//            }
//
//            if (var7 > 0) {
//                var1.Ô00000 = var1.Ô00000 + var0.substring(var7);
//            }
//
//            var1.o00000 = (Color[])var6.toArray(new Color[0]);
//            var1.Ó00000 = new int[var1.Ô00000.length()];
//            var1.new = new boolean[var1.Ô00000.length()];
//
//            for(int var15 = 0; var15 < var1.new.length; ++var15) {
//                var1.Ó00000[var15] = var5[var15];
//                var1.new[var15] = var4[var15];
//            }
//
//            return var1;
//        }
//    }
//
//    public static void o00000(com.fs.graphics.A.Object var0, CampaignEventPlugin var1, String var2, float var3, o var4) {
//        if (var4 == null) {
//            String[] var5 = var1.getHighlights(var2);
//            Color[] var6 = var1.getHighlightColors(var2);
//            if (var5 == null || var6 == null) {
//                return;
//            }
//
//            for(int var7 = 0; var7 < var6.length; ++var7) {
//                var6[var7] = com.fs.graphics.util.OoOO.Õ00000(var6[var7], var3);
//            }
//
//            var0.o00000(var5);
//            var0.o00000(var6);
//        } else {
//            Color[] var8 = (Color[])Arrays.copyOf(var4.o00000, var4.o00000.length);
//
//            for(int var9 = 0; var9 < var8.length; ++var9) {
//                var8[var9] = com.fs.graphics.util.OoOO.Õ00000(var8[var9], var3);
//            }
//
//            var0.o00000(var4.new);
//            var0.o00000(var8);
//            var0.o00000(var4.Ó00000);
//        }
//
//    }
//
//    public static float o00000(FleetMember var0, boolean var1) {
//        float var2 = var0.getStats().getMaxSpeed().getModifiedValue();
//        if (var0.getStats().getZeroFluxMinimumFluxLevel().getModifiedValue() > 0.0F) {
//            var2 += var0.getStats().getZeroFluxSpeedBoost().getModifiedValue();
//        }
//
//        CombatReadinessPlugin var3 = StarfarerSettings.õÕ0000();
//        float var4 = var0.getCR();
//        float var5 = var3.getCriticalMalfunctionThreshold(var0.getStats()) + 0.1F;
//        if (var4 < var5 && var5 > 0.0F) {
//            var2 *= 0.5F + 0.5F * var4 / var5;
//        }
//
//        String var6 = var0.getHullSpec().getShipSystemId();
//        if (var6 != null && !var6.isEmpty() && var4 > 0.0F) {
//            oO0O var7 = (oO0O)SpecStore.o00000(oO0O.class, var6);
//            var2 += var7.getAIHints().class();
//            var2 *= var7.getAIHints().new();
//        }
//
//        if (var0.getHullSpec().getDefenseType() == ShieldType.PHASE && !var1) {
//            var2 *= Math.max(PhaseCloakStats.getMaxTimeMult(var0.getStats()) - 1.0F, 1.0F);
//        }
//
//        return var2;
//    }
//
//    public static float[] Õ00000(List<Float> var0) {
//        ArrayList var1 = new ArrayList();
//
//        for(int var2 = 0; var2 < var0.size(); ++var2) {
//            var1.add((float)var2);
//        }
//
//        return o00000((List)var1, (List)var0);
//    }
//
//    public static float[] o00000(List<Float> var0, List<Float> var1) {
//        float var2 = 0.0F;
//        float var3 = 0.0F;
//        if (var0.isEmpty() && var1.isEmpty()) {
//            return new float[]{0.0F, 0.0F};
//        } else if (!var0.isEmpty() && !var1.isEmpty() && var0.size() == var1.size()) {
//            Float var4;
//            Iterator var5;
//            for(var5 = var0.iterator(); var5.hasNext(); var2 += var4) {
//                var4 = (Float)var5.next();
//            }
//
//            var2 /= (float)var0.size();
//
//            for(var5 = var1.iterator(); var5.hasNext(); var3 += var4) {
//                var4 = (Float)var5.next();
//            }
//
//            var3 /= (float)var1.size();
//            float var9 = 0.0F;
//            float var10 = 0.0F;
//
//            float var7;
//            for(int var6 = 0; var6 < var0.size(); ++var6) {
//                var7 = (Float)var0.get(var6);
//                float var8 = (Float)var1.get(var6);
//                var9 += (var7 - var2) * (var8 - var3);
//                var10 += (var7 - var2) * (var7 - var2);
//            }
//
//            float var11 = 0.0F;
//            if (var10 > 0.0F) {
//                var11 = var9 / var10;
//            }
//
//            var7 = var3 - var11 * var2;
//            return new float[]{var11, var7};
//        } else {
//            throw new RuntimeException("Invalid parameters to computeBestFitLine()");
//        }
//    }
//
//    public static void Ô00000(List<FleetMemberAPI> var0) {
//        Iterator var2 = (new ArrayList(var0)).iterator();
//
//        while(true) {
//            FleetMemberAPI var1;
//            do {
//                do {
//                    if (!var2.hasNext()) {
//                        var2 = (new ArrayList(var0)).iterator();
//
//                        while(var2.hasNext()) {
//                            var1 = (FleetMemberAPI)var2.next();
//                            if (var1.isMothballed()) {
//                                var0.remove(var1);
//                                var0.add(var1);
//                            }
//                        }
//
//                        return;
//                    }
//
//                    var1 = (FleetMemberAPI)var2.next();
//                } while(var1.isMothballed());
//            } while(var1.getRepairTracker().getCR() <= 0.0F);
//
//            int var3 = var1.getOwner();
//            Iterator var5 = var1.getVariant().getFittedWings().iterator();
//
//            while(var5.hasNext()) {
//                String var4 = (String)var5.next();
//                FleetMember var6 = new FleetMember(var3, var4, FleetMemberType.FIGHTER_WING);
//                var6.getRepairTracker().setCR(var1.getRepairTracker().getCR());
//                CrewComposition var7 = new CrewComposition();
//                var7.addCrew(10000.0F);
//                var6.setCrewComposition(var7);
//                var0.add(var6);
//            }
//        }
//    }
//
//    public static void Ò00000(List<FleetMember> var0) {
//        Iterator var2 = (new ArrayList(var0)).iterator();
//
//        while(true) {
//            FleetMember var1;
//            do {
//                do {
//                    if (!var2.hasNext()) {
//                        var2 = (new ArrayList(var0)).iterator();
//
//                        while(var2.hasNext()) {
//                            var1 = (FleetMember)var2.next();
//                            if (var1.isMothballed()) {
//                                var0.remove(var1);
//                                var0.add(var1);
//                            }
//                        }
//
//                        return;
//                    }
//
//                    var1 = (FleetMember)var2.next();
//                } while(var1.isMothballed());
//            } while(var1.getCR() <= 0.0F);
//
//            int var3 = var1.getOwner();
//            Iterator var5 = var1.getVariant().getFittedWings().iterator();
//
//            while(var5.hasNext()) {
//                String var4 = (String)var5.next();
//                FleetMember var6 = new FleetMember(var3, var4, FleetMemberType.FIGHTER_WING);
//                var6.getRepairTracker().setCR(var1.getCR());
//                CrewComposition var7 = new CrewComposition();
//                var7.addCrew(10000.0F);
//                var6.setCrewComposition(var7);
//                var0.add(var6);
//            }
//        }
//    }
//
//    public static final float Ô00000(float var0, float var1) {
//        return return(var0, var1) * 57.295776F;
//    }
//
//    public static final float Object(float var0, float var1) {
//        return (float)Math.atan2((double)var0, (double)var1) * 57.295776F;
//    }

    // UtilsAlias.atan2
    public static float return_(float y, float x) {
        float var2;
        float var3;
        if (x < 0.0F) {
            if (y < 0.0F) {
                x = -x;
                y = -y;
                var3 = 1.0F;
            } else {
                x = -x;
                var3 = -1.0F;
            }

            var2 = -3.1415927F;
        } else {
            if (y < 0.0F) {
                y = -y;
                var3 = -1.0F;
            } else {
                var3 = 1.0F;
            }

            var2 = 0.0F;
        }

        float var4 = 1.0F / ((Math.max(x, y)) * float2toPowerMinus10);
        int var5 = (int) (x * var4);
        int var6 = (int) (y * var4);
        int var7 = var6 * int1024 + var5;
        return var7 >= 0 && var7 < atan2Lookup.length ? (atan2Lookup[var7] + var2) * var3 : 0.0F;
    }

//    public static int o00000(HullVariantSpec var0) {
//        int var1 = 0;
//        if (var0 != null) {
//            class var2 = com.fs.starfarer.combat.entities.ship.class.create(var0);
//            Iterator var4 = var0.getAllMods().iterator();
//
//            while(var4.hasNext()) {
//                private var3 = (private)var4.next();
//                if (var3.getEffect() != null) {
//                    var3.getEffect().applyEffectsBeforeShipCreation(var0.getHullSize(), var2, var3.getId());
//                }
//            }
//
//            var1 = (int)var2.getNumFighterBays().getModifiedValue();
//            if (var1 < 0) {
//                var1 = 0;
//            }
//        }
//
//        return var1;
//    }
//
//    public static float o00000(float[] var0, int var1) {
//        if (var0 != null && var0.length > var1 && var1 >= 0) {
//            int var2 = 0;
//            int var3 = var0.length - 1;
//
//            while(var2 < var3) {
//                int var4 = var2;
//                int var5 = var3;
//                float var6 = var0[(var2 + var3) / 2];
//
//                while(var4 < var5) {
//                    if (var0[var4] >= var6) {
//                        float var7 = var0[var5];
//                        var0[var5] = var0[var4];
//                        var0[var4] = var7;
//                        --var5;
//                    } else {
//                        ++var4;
//                    }
//                }
//
//                if (var0[var4] > var6) {
//                    --var4;
//                }
//
//                if (var1 <= var4) {
//                    var3 = var4;
//                } else {
//                    var2 = var4 + 1;
//                }
//            }
//
//            return var0[var1];
//        } else {
//            return -1.0F;
//        }
//    }
//
//    public static void o00000(String[] var0) {
//        float var1 = 0.0F;
//        float var2 = 0.0F;
//        float var3 = 10000.0F;
//
//        for(int var4 = 0; (float)var4 < var3; ++var4) {
//            Vector2f var5 = Object((float)Math.random() * 360.0F);
//            var5.scale((float)Math.random() * 0.01F);
//            float var6 = Object(var5);
//            float var7 = Ò00000(var5);
//            float var8 = Math.abs(var6 - var7);
//            System.out.println("Diff: " + var8);
//            if (var8 > var1) {
//                var1 = var8;
//            }
//
//            var2 += var8;
//        }
//
//        System.out.println();
//        System.out.println("Max: " + var1);
//        System.out.println("Average: " + var2 / var3);
//    }
//
//    public static Color o00000(SkillSpec var0) {
//        E var1 = var0.getGoverningAptitude();
//        Color var2 = com.fs.graphics.util.OoOO.super(var1.Ó00000(), com.fs.graphics.util.OoOO.Ò00000(Object.oo0000, 255), 0.35F);
//        return var2;
//    }
//
//    public static boolean o00000(Runnable var0, int var1, boolean var2, String var3) throws IOException {
//        try {
//            if (System.getProperty("sun.java.command") == null) {
//                return false;
//            } else {
//                String var4 = System.getProperty("java.home") + "/bin/java";
//                if (StarfarerSettings.OO0000()) {
//                    System.setProperty("jdk.lang.Process.launchMechanism", "FORK");
//                }
//
//                List var5 = ManagementFactory.getRuntimeMXBean().getInputArguments();
//                StringBuffer var6 = new StringBuffer();
//                Iterator var8 = var5.iterator();
//
//                while(var8.hasNext()) {
//                    String var7 = (String)var8.next();
//                    if (var1 > 0) {
//                        if (var7.startsWith("-Xms")) {
//                            var7 = "-Xms" + var1 + "m";
//                        } else if (var7.startsWith("-Xmx")) {
//                            var7 = "-Xmx" + var1 + "m";
//                        }
//                    }
//
//                    if (!var7.contains("-agentlib")) {
//                        var6.append(var7);
//                        var6.append(" ");
//                    }
//                }
//
//                if (var2) {
//                    var6.append("-DlaunchDirect");
//                    var6.append(" ");
//                }
//
//                if (var3 != null) {
//                    var6.append(var3);
//                    var6.append(" ");
//                }
//
//                final StringBuffer var11 = new StringBuffer("\"" + var4 + "\" " + var6);
//                String[] var12 = System.getProperty("sun.java.command").split(" ");
//                if (var12[0].endsWith(".jar")) {
//                    var11.append("-jar " + (new File(var12[0])).getPath());
//                } else {
//                    var11.append("-cp \"" + System.getProperty("java.class.path") + "\" " + var12[0]);
//                }
//
//                for(int var9 = 1; var9 < var12.length; ++var9) {
//                    var11.append(" ");
//                    var11.append(var12[var9]);
//                }
//
//                Runtime.getRuntime().addShutdownHook(new Thread() {
//                    public void run() {
//                        try {
//                            if (StarfarerSettings.OO0000()) {
//                                Runtime.getRuntime().exec("open /Applications/Starsector.app");
//                            } else {
//                                Runtime.getRuntime().exec(var11.toString());
//                            }
//                        } catch (IOException var2) {
//                            var2.printStackTrace();
//                        }
//
//                    }
//                });
//                if (var0 != null) {
//                    var0.run();
//                }
//
//                System.exit(0);
//                return true;
//            }
//        } catch (Exception var10) {
//            throw new IOException("Error while trying to restart Starsector: \n" + var10.getMessage(), var10);
//        }
//    }
//
//    public static class o {
//        public String Ô00000;
//        public Color[] o00000;
//        public int[] Ó00000;
//        public boolean[] new;
//
//        public o() {
//        }
//    }
}

class UtilsAlias {
    public static float degToRadConversion = Utils.ÓO0000;
    public static float radToDegConversion = Utils.class_;

    public static boolean willHitShield(float shieldFacing, float shieldArc, Vector2f shieldLocation, Vector2f weaponLocation) {
        return Utils.o00000(shieldFacing, shieldArc, shieldLocation, weaponLocation);
    }

    public static float getFacing(Vector2f var0) {
        return Utils.Object(var0);
    }

    public static Vector2f directionalVector(float facing) {
        return Utils.Object(facing);
    }

    public static float atan2(float y, float x) {
        return Utils.return_(y, x);
    }

    public static float angleTowards(Vector2f from, Vector2f to) {
        return Utils.Object(from, to);
    }

    public static float distance(Vector2f var0, Vector2f var1) {
        return Utils.Ø00000(var0, var1);
    }

    public static float clampAngle(float angle) {
        return Utils.o00000(angle);
    }

    public static float shortestRotation(float from, float to) {
        return Utils.o00000(from, to);
    }

    public static float shortestRotationToTarget(float weaponFacing, Vector2f weaponLocation, Vector2f targetLocation) {
        return Utils.Ò00000(weaponFacing, weaponLocation, targetLocation);
    }
}