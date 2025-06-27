package com.genir.aitweaks.core.shipai.global

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.utils.FrameTracker

class GlobalAI : BaseEveryFrameCombatPlugin() {
    val fleetSegmentation: Array<FleetSegmentation> = arrayOf(FleetSegmentation(0), FleetSegmentation(1))
    val maneuverCoordinator: AttackCoordinator = AttackCoordinator()
    val navigateCoordinator: NavigationCoordinator = NavigationCoordinator()
    val projectileTracker: ProjectileTracker = ProjectileTracker()

    private val fleetCohesion: Array<FleetCohesionAI> = arrayOf(FleetCohesionAI(0, this), FleetCohesionAI(1, this))
    private val searchAndDestroy: SearchAndDestroyManager = SearchAndDestroyManager(this)

    val frameTracker: FrameTracker = FrameTracker()

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        fleetSegmentation[0].advance(dt, events)
        fleetSegmentation[1].advance(dt, events)
        fleetCohesion[0].advance(dt, events)
        fleetCohesion[1].advance(dt, events)
        searchAndDestroy.advance(dt, events)
        maneuverCoordinator.advance(dt, events)
        navigateCoordinator.advance(dt, events)
        projectileTracker.advance(dt, events)

        frameTracker.advance()
    }
}
