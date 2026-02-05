package com.genir.aitweaks.core.state

import lunalib.lunaSettings.LunaSettings

/**
 * AI Tweaks configuration.
 * Values are read only during combat, so there is no need to listen for changes in the campaign layer.
 * However, the configuration must be refreshed at the beginning of each combat.
 */
class Config {
    companion object {
        var config = Config()
    }

    // Ship AI
    val enableFleetCohesion: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_fleet_cohesion_ai")!!
    val aiPersonality: String? = LunaSettings.getString("aitweaks", "aitweaks_ai_core_personality")
    val fleetwideSearchAndDestroy: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_fleetwide_search_destroy")!!

    // Autofire AI
    val staggeredFireThreshold: Float = LunaSettings.getFloat("aitweaks", "aitweaks_staggered_fire_threshold")!!
    val enableNeedlerFix: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_needler_fix")!!
    val enableBeamSweep: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_beam_sweep")!!
    val strictUseLessVSShields: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_strict_use_less_vs_shields")!!
    val fireThroughShields: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_allow_beams_through_shields")!!

    // Player assist
    val useVanillaAI: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_use_vanilla_ai")!!
    val shieldAssistKeybind: Int = LunaSettings.getInt("aitweaks", "aitweaks_shield_assist_keybind")!!
    val aimAssistKeybind: Int = LunaSettings.getInt("aitweaks", "aitweaks_aim_bot_keybind")!!
    val aimAssistRotateShip: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_aim_bot_rotate_ship")!!
    val aimAssistTargetJunk: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_aim_bot_target_junk")!!
    val aimAssistPerfect: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_aim_bot_perfect")!!

    // Debug
    val devMode: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_devmode")!!
    val highlightCustomAI: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_highlight_custom_ai")!!
    val removeCombatMapGrid: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_remove_combat_map_grid")!!
    val enableSimulatorCustomAI: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_simulator_custom_ai")!!
    val enableAllCustomAI: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_all_custom_ai")!!
    val enableTitleScreenFire: Boolean = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_title_screen_fire")!!
}
