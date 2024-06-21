package com.genir.aitweaks.features.shipai.adapters;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.combat.entities.Ship;
import org.lwjgl.util.vector.Vector2f;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * ManeuverAdapter wraps AI Tweaks Maneuver in vanilla Maneuver interface with
 * obfuscated method names. ManeuverAdapter class is built at runtime and injected
 * into system class loader. The system class loader has no access to AI Tweaks
 * Maneuver, so the AI Tweaks Maneuver needs to be loaded with ScriptClassLoader
 * and accessed through reflection.
 */
public class ManeuverAdapter implements ManeuverInterface {
    private final Object maneuver;

    private final MethodHandle advance;
    private final MethodHandle getTarget;
    private final MethodHandle doManeuver;
    private final MethodHandle getDesiredHeading;
    private final MethodHandle getDesiredFacing;

    ManeuverAdapter(Ship ship, Ship target, Vector2f location) {
        try {
            // Load AI Tweaks class loader. This is required when ManeuverAdapter was loaded
            // from aitweaks-shipai.jar by system class loader, because the system class loader
            // is not aware of AI Tweaks classes.
            ClassLoader scriptLoader = Global.getSettings().getScriptClassLoader();
            Class<?> aitLoaderClass = scriptLoader.loadClass("com.genir.aitweaks.launcher.AitLoaderGetter");
            MethodType getAitLoaderType = MethodType.methodType(ClassLoader.class);
            MethodHandle getAitLoader = MethodHandles.lookup().findVirtual(aitLoaderClass, "getAitLoader", getAitLoaderType);
            ClassLoader aitLoader = (ClassLoader) getAitLoader.invoke(aitLoaderClass.newInstance());

            // Load Maneuver class.
            Class<?> m = aitLoader.loadClass("com.genir.aitweaks.features.shipai.ai.Maneuver");

            // Construct Maneuver object.
            MethodType typ = MethodType.methodType(void.class, ShipAPI.class, ShipAPI.class, Vector2f.class);
            MethodHandle ctor = MethodHandles.lookup().findConstructor(m, typ);
            maneuver = ctor.invoke(ship, target, location);

            advance = MethodHandles.lookup().findVirtual(m, "advance", MethodType.methodType(void.class, float.class));
            getTarget = MethodHandles.lookup().findVirtual(m, "getManeuverTarget", MethodType.methodType(ShipAPI.class));
            doManeuver = MethodHandles.lookup().findVirtual(m, "doManeuver", MethodType.methodType(void.class));
            getDesiredHeading = MethodHandles.lookup().findVirtual(m, "getDesiredHeading", MethodType.methodType(float.class));
            getDesiredFacing = MethodHandles.lookup().findVirtual(m, "getDesiredFacing", MethodType.methodType(float.class));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void advanceObf(float p0) {
        try {
            advance.invoke(maneuver, p0);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CombatEntityAPI getTargetObf() {
        try {
            return (CombatEntityAPI) getTarget.invoke(maneuver);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isDirectControlObf() {
        return true;
    }

    @Override
    public void doManeuverObf() {
        try {
            doManeuver.invoke(maneuver);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public float getDesiredHeadingObf() {
        try {
            return (float) getDesiredHeading.invoke(maneuver);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public float getDesiredFacingObf() {
        try {
            return (float) getDesiredFacing.invoke(maneuver);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
