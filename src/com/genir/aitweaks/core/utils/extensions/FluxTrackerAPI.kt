package com.genir.aitweaks.core.utils.extensions

import com.fs.starfarer.api.combat.FluxTrackerAPI

val FluxTrackerAPI.hardFluxLevel: Float
    get() = this.hardFlux / this.maxFlux