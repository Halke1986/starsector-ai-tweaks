package com.genir.aitweaks.core.state

import com.genir.starfarer.title.input.Keymap

object VanillaKeymap {
    enum class PlayerAction(var prev: Boolean = KEY_UP, var current: Boolean = KEY_UP) {
        SHIP_STRAFE_KEY,
        SHIP_FIRE,
        SHIP_TURN_LEFT,
        SHIP_TURN_RIGHT,
        SHIP_STRAFE_LEFT_NOTURN,
        SHIP_STRAFE_RIGHT_NOTURN,
        SHIP_ACCELERATE,
        SHIP_ACCELERATE_BACKWARDS,
        SHIP_SHIELDS;

        val obfuscated = Keymap.PlayerAction.valueOf(name)
    }

    fun advance() {
        PlayerAction.entries.forEach { action ->
            action.prev = action.current
            action.current = Keymap.keymap_isKeyDown(action.obfuscated)
        }
    }

    fun isKeyUp(action: PlayerAction) = action.current == KEY_UP

    fun isKeyDown(action: PlayerAction) = action.current == KEY_DOWN

    fun isKeyUpEvent(action: PlayerAction) = action.prev == KEY_DOWN && action.current == KEY_UP

    fun isKeyDownEvent(action: PlayerAction) = action.prev == KEY_UP && action.current == KEY_DOWN

    private const val KEY_DOWN = true
    private const val KEY_UP = !KEY_DOWN
}
