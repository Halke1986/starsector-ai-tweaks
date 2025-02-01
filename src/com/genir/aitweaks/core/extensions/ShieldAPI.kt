package com.genir.aitweaks.core.extensions

import com.fs.starfarer.api.combat.ShieldAPI
import com.genir.aitweaks.core.utils.Direction

val ShieldAPI.Facing: Direction
    get() = Direction(facing)
