package com.genir.aitweaks.utils.extensions

import com.fs.starfarer.api.combat.FluxTrackerAPI

val FluxTrackerAPI.hardFluxLevel: Float
    get() = this.hardFlux / this.maxFlux