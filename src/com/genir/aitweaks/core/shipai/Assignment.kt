package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.AssignmentTargetAPI
import com.fs.starfarer.api.combat.CombatAssignmentType.*
import com.fs.starfarer.api.combat.CombatFleetManagerAPI
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.core.extensions.assignment
import com.genir.aitweaks.core.extensions.isNearMapCenterline
import com.genir.aitweaks.core.extensions.length
import com.genir.aitweaks.core.extensions.minus
import com.genir.aitweaks.core.shipai.Assignment.Type.*
import com.genir.aitweaks.core.shipai.global.NavigationCoordinator
import org.lwjgl.util.vector.Vector2f

class Assignment(private val ai: CustomShipAI) {
    private val ship: ShipAPI = ai.ship

    var navigateTo: Vector2f? = null
    var arrivedAt: Boolean = false
    var eliminate: ShipAPI? = null
    var type: Type = NONE

    enum class Type {
        NAVIGATE,
        NAVIGATE_IN_FORMATION,
        EXPLORE, // non-vanilla assignment
        ELIMINATE,
        NONE,
    }

    fun advance() {
        // Cleanup.
        navigateTo = null
        arrivedAt = false
        eliminate = null
        type = NONE

        val assignment: CombatFleetManagerAPI.AssignmentInfo? = ship.assignment
        val target: AssignmentTargetAPI? = assignment?.target

        // Handle the assignment, if any.
        if (assignment != null && target != null) {
            when (assignment.type) {
                DEFEND,
                HARASS,
                LIGHT_ESCORT,
                MEDIUM_ESCORT,
                HEAVY_ESCORT,
                CONTROL,
                ENGAGE -> navigate(target, formation = false)

                RALLY_TASK_FORCE,
                RALLY_CARRIER,
                RALLY_CIVILIAN,
                RALLY_STRIKE_FORCE -> navigate(target, formation = true)

                CAPTURE,
                ASSAULT -> takeControl(target)

                INTERCEPT,
                STRIKE -> eliminate(target) // Combat carriers should treat fighter strike assignment as eliminate.

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
    private fun navigate(target: AssignmentTargetAPI, formation: Boolean) {
        // For now, don't respect vanilla Admiral AI capture
        // assignments, as this leads to suicide charges.
        if (ship.owner != 0 || ship.isAlly) {
            return
        }

        // When the assignment is near the map centerline, the ships should form line abreast.
        type = if (formation && target.isNearMapCenterline) {
            NAVIGATE_IN_FORMATION
        } else {
            NAVIGATE
        }

        val coordinatedWaypoint = if (type == NAVIGATE_IN_FORMATION) {
            val navigationCoordinator: NavigationCoordinator = ai.globalAI.navigateCoordinator
            val (waypoint, _) = navigationCoordinator.coordinateNavigation(ai, target)
            waypoint
        } else {
            target.location
        }

        navigateTo = coordinatedWaypoint
        arrivedAt = (coordinatedWaypoint - ship.location).length < Preset.arrivedAtLocationRadius
    }

    /** Take control of battle objective by unconditionally moving on top of it. */
    private fun takeControl(target: AssignmentTargetAPI) {
        // For now, don't respect vanilla Admiral AI capture
        // assignments, as this leads to suicide charges.
        if (ship.owner != 0 || ship.isAlly) {
            return
        }

        navigateTo = target.location
        arrivedAt = false
        type = NAVIGATE
    }

    private fun eliminate(target: AssignmentTargetAPI) {
        if (target is DeployedFleetMemberAPI) {
            eliminate = target.ship
            type = ELIMINATE
        }
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
