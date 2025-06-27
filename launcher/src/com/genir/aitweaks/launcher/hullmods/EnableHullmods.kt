package com.genir.aitweaks.launcher.hullmods

import lunalib.lunaSettings.LunaSettings

fun enableHullmods(): Boolean {
    return LunaSettings.getBoolean("aitweaks", "aitweaks_use_vanilla_ai") != true
}
