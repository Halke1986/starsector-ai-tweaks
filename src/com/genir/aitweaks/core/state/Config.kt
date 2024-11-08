package com.genir.aitweaks.core.state

import lunalib.lunaSettings.LunaSettings

class Config {
    val devMode: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_devmode")!!
    val highlightCustomAI: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_highlight_custom_ai")!!
    val enableTitleScreenFire: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_title_screen_fire")!!
    val enableCustomAI: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_custom_ship_ai")!!
    val enableFleetCohesion: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_fleet_cohesion_ai")!!
    val enabledStaggeredFire: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_staggered_fire")!!

    val aiPersonality: String? = LunaSettings.getString("aitweaks", "aitweaks_ai_core_personality")

    val omniShieldKeybind: Int = LunaSettings.getInt("aitweaks", "aitweaks_omni_shield_keybind")!!
    val aimAssistKeybind: Int = LunaSettings.getInt("aitweaks", "aitweaks_aim_bot_keybind")!!
}
