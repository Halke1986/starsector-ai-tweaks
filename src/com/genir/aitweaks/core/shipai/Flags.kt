package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags

class Flags(val vanillaFlags: ShipwideAIFlags) {
    /** Set of AI flags respected by Custom ship AI. */
    enum class Flag(val vanillaFlag: AIFlags) {
        // Input flags.
        DO_NOT_BACK_OFF(vanillaFlag = AIFlags.DO_NOT_BACK_OFF),
        MANEUVER_RANGE_FROM_TARGET(vanillaFlag = AIFlags.MANEUVER_RANGE_FROM_TARGET),

        // Output flags.
        BACKING_OFF(vanillaFlag = AIFlags.BACKING_OFF),
        MANEUVER_TARGET(vanillaFlag = AIFlags.MANEUVER_TARGET),
    }

    inline fun <reified T> get(flag: Flag): T? {
        return vanillaFlags.getCustom(flag.vanillaFlag) as? T
    }

    fun has(flag: Flag): Boolean {
        return vanillaFlags.hasFlag(flag.vanillaFlag)
    }

    fun set(flag: Flag) {
        vanillaFlags.setFlag(flag.vanillaFlag)
    }

    fun set(flag: Flag, value: Any?) {
        vanillaFlags.setFlag(flag.vanillaFlag, ShipwideAIFlags.FLAG_DURATION, value)
    }

    fun unset(flag: Flag) {
        vanillaFlags.unsetFlag(flag.vanillaFlag)
    }
}
