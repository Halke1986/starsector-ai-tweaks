package com.genir.aitweaks.core.extensions

import com.fs.starfarer.api.combat.ShieldAPI
import com.genir.aitweaks.core.utils.Rotation

val ShieldAPI.Facing: Rotation
    get() = Rotation(facing)
