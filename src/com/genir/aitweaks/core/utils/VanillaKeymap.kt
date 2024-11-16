package com.genir.aitweaks.core.utils

import com.genir.aitweaks.core.Obfuscated

object VanillaKeymap {
    fun isKeyDown(action: Action): Boolean {
        val obfAction = Obfuscated.PlayerAction.valueOf(action.name)
        return Obfuscated.Keymap.keymap_isKeyDown(obfAction)
    }

    enum class Action {
        SHIP_STRAFE_KEY,
        SHIP_FIRE,
        SHIP_TURN_LEFT,
        SHIP_TURN_RIGHT,
        SHIP_STRAFE_LEFT_NOTURN,
        SHIP_STRAFE_RIGHT_NOTURN,
        SHIP_ACCELERATE,
        SHIP_ACCELERATE_BACKWARDS,
    }
}
