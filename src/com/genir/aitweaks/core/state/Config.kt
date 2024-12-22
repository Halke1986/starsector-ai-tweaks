package com.genir.aitweaks.core.state

import lunalib.lunaSettings.LunaSettings

/**
 * AI Tweaks configuration.
 * Values are read only during combat, so there is no need to listen for changes in the campaign layer.
 * However, the configuration must be refreshed at the beginning of each combat.
 */
class Config {
    val devMode: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_devmode")!!
    val highlightCustomAI: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_highlight_custom_ai")!!
    val enableTitleScreenFire: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_title_screen_fire")!!
    val enableCustomAI: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_custom_ship_ai")!!
    val enableFleetCohesion: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_fleet_cohesion_ai")!!
    val enabledStaggeredFire: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_staggered_fire")!!
    val aimAssistRotateShip: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_aim_bot_rotate_ship")!!
    val removeCombatMapGrid: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_remove_combat_map_grid")!!
    val enableNeedlerFix: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_needler_fix")!!

    val aiPersonality: String? = LunaSettings.getString("aitweaks", "aitweaks_ai_core_personality")

    val omniShieldKeybind: Int = LunaSettings.getInt("aitweaks", "aitweaks_omni_shield_keybind")!!
    val aimAssistKeybind: Int = LunaSettings.getInt("aitweaks", "aitweaks_aim_bot_keybind")!!
}
