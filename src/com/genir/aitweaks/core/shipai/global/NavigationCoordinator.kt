package com.genir.aitweaks.core.shipai.global

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.AssignmentTargetAPI
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.extensions.sumOf
import com.genir.aitweaks.core.extensions.totalCollisionRadius
import com.genir.aitweaks.core.shipai.CustomShipAI
import org.lwjgl.util.vector.Vector2f

class NavigationCoordinator : BaseEveryFrameCombatPlugin() {
    private val requests: MutableMap<CustomShipAI, Unit> = mutableMapOf()
    private val responses: MutableMap<CustomShipAI, Response> = mutableMapOf()

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        // Running when paused would lead to de-synchronization
        // with ship AI. In the first frame after un-pausing,
        // ship AI would observe null reviewedHeadingPoint.
        if (Global.getCombatEngine().isPaused) {
            return
        }

        responses.clear()

        val formations: Collection<MutableList<Unit>> = buildFormations()

        formations.forEach {
            coordinateFormations(it)
        }

        requests.clear()
    }

    fun coordinateNavigation(ai: CustomShipAI, navigationTarget: AssignmentTargetAPI): Response {
        requests[ai] = Unit(ai, navigationTarget)

        return responses[ai] ?: Response(navigationTarget.location, 1)
    }

    private fun buildFormations(): Collection<MutableList<Unit>> {
        val formations: MutableMap<AssignmentTargetAPI, MutableList<Unit>> = mutableMapOf()

        requests.forEach { (_, unit) ->
            val taskForce = formations[unit.navigationTarget]

            if (taskForce == null) {
                formations[unit.navigationTarget] = mutableListOf(unit)
            } else {
                taskForce.add(unit)
            }
        }

        return formations.values
    }


    private fun coordinateFormations(formation: MutableList<Unit>) {
        // Task forces with only one unit do not require coordination.
        if (formation.size == 1) {
            return
        }

        val formationWidth = formation.sumOf { it.width }

        formation.sortBy { unit -> unit.ai.ship.location.x }

        var locationOffset: Float = -formationWidth / 2

        formation.forEach { unit ->
            val unitLocation = Vector2f(
                unit.navigationTarget.location.x + locationOffset + unit.width / 2,
                unit.navigationTarget.location.y,
            )

            locationOffset += unit.width

            responses[unit.ai] = Response(unitLocation, formation.size)
        }
    }

    data class Response(
        val reviewedHeadingPoint: Vector2f,
        val formationSize: Int,
    )

    private class Unit(
        val ai: CustomShipAI,
        val navigationTarget: AssignmentTargetAPI,
    ) {
        val width: Float = ai.ship.totalCollisionRadius * 2 * 1.6f
    }
}
