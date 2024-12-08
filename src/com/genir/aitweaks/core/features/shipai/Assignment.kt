package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.api.combat.CombatAssignmentType.*
import com.fs.starfarer.api.combat.CombatFleetManagerAPI
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.features.shipai.Assignment.Type.*
import com.genir.aitweaks.core.utils.extensions.assignment
import com.genir.aitweaks.core.utils.extensions.length
import com.genir.aitweaks.core.utils.extensions.minus
import org.lwjgl.util.vector.Vector2f

class Assignment(private val ship: ShipAPI) {
    var navigateTo: Vector2f? = null
    var arrivedAt: Boolean = false
    var eliminate: ShipAPI? = null
    var type: Type = NONE

    enum class Type {
        NAVIGATE_TO,
        TAKE_CONTROL,
        ELIMINATE,
        NONE,
    }

    fun advance() {
        // Cleanup.
        navigateTo = null
        arrivedAt = false
        eliminate = null
        type = NONE

        val assignment = ship.assignment ?: return

        when (assignment.type) {
            DEFEND -> navigate(assignment)
            RALLY_TASK_FORCE -> navigate(assignment)
            RALLY_CARRIER -> navigate(assignment)
            RALLY_CIVILIAN -> navigate(assignment)
            RALLY_STRIKE_FORCE -> navigate(assignment)
            HARASS -> navigate(assignment)
            LIGHT_ESCORT -> navigate(assignment)
            MEDIUM_ESCORT -> navigate(assignment)
            HEAVY_ESCORT -> navigate(assignment)
            CONTROL -> navigate(assignment)
            ENGAGE -> navigate(assignment)

            CAPTURE -> takeControl(assignment)
            ASSAULT -> takeControl(assignment)

            INTERCEPT -> eliminate(assignment)
            STRIKE -> eliminate(assignment) // Combat carriers should treat fighter strike assignment as eliminate.

            // Ignored and unimplemented assignments.
            RALLY_FIGHTERS -> Unit
            RECON -> Unit
            AVOID -> Unit
            RETREAT -> Unit
            REPAIR_AND_REFIT -> Unit
            SEARCH_AND_DESTROY -> Unit

            else -> Unit
        }
    }

    /** Navigate to the assignment location and stay close to it. */
    private fun navigate(assignment: CombatFleetManagerAPI.AssignmentInfo) {
        navigateTo = assignment.target?.location
        arrivedAt = navigateTo != null && (navigateTo!! - ship.location).length < Preset.arrivedAtLocationRadius
        type = NAVIGATE_TO
    }

    /** Take control of battle objective by unconditionally moving on top of it. */
    private fun takeControl(assignment: CombatFleetManagerAPI.AssignmentInfo) {
        navigateTo = assignment.target?.location
        arrivedAt = false
        type = TAKE_CONTROL
    }

    private fun eliminate(assignment: CombatFleetManagerAPI.AssignmentInfo) {
        eliminate = (assignment.target as? DeployedFleetMemberAPI)?.ship
        type = ELIMINATE
    }
}
