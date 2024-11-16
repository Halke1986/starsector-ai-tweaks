package com.genir.aitweaks.core.state

import com.genir.aitweaks.core.Obfuscated

object VanillaKeymap {
    enum class Action(var prev: Boolean = KEY_UP, var current: Boolean = KEY_UP) {
        SHIP_STRAFE_KEY,
        SHIP_FIRE,
        SHIP_TURN_LEFT,
        SHIP_TURN_RIGHT,
        SHIP_STRAFE_LEFT_NOTURN,
        SHIP_STRAFE_RIGHT_NOTURN,
        SHIP_ACCELERATE,
        SHIP_ACCELERATE_BACKWARDS,
        SHIP_SHIELDS,
    }

    fun advance() {
        Action.values().forEach { action ->
            action.prev = action.current
            val obfAction = Obfuscated.PlayerAction.valueOf(action.name)
            action.current = Obfuscated.Keymap.keymap_isKeyDown(obfAction)
        }
    }

    fun isKeyUp(action: Action) = action.current == KEY_UP

    fun isKeyDown(action: Action) = action.current == KEY_DOWN

    fun isKeyUpEvent(action: Action) = action.prev == KEY_DOWN && action.current == KEY_UP

    fun isKeyDownEvent(action: Action) = action.prev == KEY_UP && action.current == KEY_DOWN

    private const val KEY_DOWN = true
    private const val KEY_UP = !KEY_DOWN
}
