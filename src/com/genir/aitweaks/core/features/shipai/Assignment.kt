package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.CombatAssignmentType
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.utils.extensions.assignment

val ShipAPI.eliminateAssignment: ShipAPI?
    get() {
        if (assignment?.type != CombatAssignmentType.INTERCEPT) return null
        return (assignment?.target as? DeployedFleetMemberAPI)?.ship
    }
