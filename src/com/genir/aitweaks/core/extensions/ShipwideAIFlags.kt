package com.genir.aitweaks.core.extensions

import com.fs.starfarer.api.combat.ShipwideAIFlags

inline fun <reified T> ShipwideAIFlags.get(flag: ShipwideAIFlags.AIFlags): T? {
    return getCustom(flag) as? T
}
