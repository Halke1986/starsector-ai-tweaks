package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.AssignmentTargetAPI
import com.fs.starfarer.api.combat.CombatAssignmentType.*
import com.fs.starfarer.api.combat.CombatFleetManagerAPI
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.assignment
import com.genir.aitweaks.core.extensions.length
import com.genir.aitweaks.core.extensions.minus
import com.genir.aitweaks.core.shipai.Assignment.Type.*
import com.genir.aitweaks.core.shipai.coordinators.NavigationCoordinator
import com.genir.aitweaks.core.state.State
import org.lwjgl.util.vector.Vector2f

class Assignment(private val ai: CustomShipAI) {
    private val ship: ShipAPI = ai.ship

    var navigateTo: Vector2f? = null
    var arrivedAt: Boolean = false
    var eliminate: ShipAPI? = null
    var type: Type = NONE

    enum class Type {
        NAVIGATE_TO,
        TAKE_CONTROL,
        ELIMINATE,
        EXPLORE, // non-vanilla assignment
        NONE,
    }

    fun advance() {
        // Cleanup.
        navigateTo = null
        arrivedAt = false
        eliminate = null
        type = NONE

        // Handle the assignment, if any.
        ship.assignment?.let { assignment ->
            when (assignment.type) {
                DEFEND,
                RALLY_TASK_FORCE,
                RALLY_CARRIER,
                RALLY_CIVILIAN,
                RALLY_STRIKE_FORCE,
                HARASS,
                LIGHT_ESCORT,
                MEDIUM_ESCORT,
                HEAVY_ESCORT,
                CONTROL,
                ENGAGE -> navigate(assignment)

                CAPTURE,
                ASSAULT -> takeControl(assignment)

                INTERCEPT,
                STRIKE -> eliminate(assignment) // Combat carriers should treat fighter strike assignment as eliminate.

                // Ignored and unimplemented assignments.
                RALLY_FIGHTERS,
                RECON,
                AVOID,
                RETREAT,
                REPAIR_AND_REFIT,
                SEARCH_AND_DESTROY -> Unit

                else -> Unit
            }
        }

        if (type == NONE && ai.isExploring) {
            explore()
        }
    }

    /** Navigate to the assignment location and stay close to it. */
    private fun navigate(assignment: CombatFleetManagerAPI.AssignmentInfo) {
        val navigationTarget: AssignmentTargetAPI = assignment.target ?: return
        val navigationCoordinator: NavigationCoordinator = State.state.navigateCoordinator
        val (coordinatedWaypoint, _) = navigationCoordinator.coordinateNavigation(ai, navigationTarget)

        navigateTo = coordinatedWaypoint
        arrivedAt = (coordinatedWaypoint - ship.location).length < Preset.arrivedAtLocationRadius
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

    /** Move in the direction of enemy spawn location. */
    private fun explore() {
        val height = Global.getCombatEngine().mapHeight / 4
        val sign = if (ship.owner == 0) 1 else -1

        navigateTo = Vector2f(ship.location.x, height * sign)
        arrivedAt = (navigateTo!! - ship.location).length < Preset.arrivedAtLocationRadius
        type = EXPLORE
    }
}
