package com.genir.aitweaks.core.features.shipai.autofire.ballistics

/** Weapon attack parameters: accuracy and delay until attack. */
data class BallisticParams(val accuracy: Float, val delay: Float) {
    companion object {
        val default = BallisticParams(1f, 0f)
    }
}
