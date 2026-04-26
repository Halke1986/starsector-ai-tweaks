package com.genir.aitweaks.core.shipai.global

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.extensions.isValidTarget

class TargetTracker : BaseEveryFrameCombatPlugin() {
    private var shipTargets: List<ShipAPI>? = null
    private var missileTargets: List<MissileAPI>? = null
    private var obstacles: List<ShipAPI>? = null

    // Ships and fighters that can be attacked and damaged.
    fun getShipTargets(): List<ShipAPI> {
        var targets = this.shipTargets
        if (targets == null) {
            targets = Global.getCombatEngine().ships.filter { ship ->
                ship.isValidTarget
            }

            this.shipTargets = targets
        }

        return targets
    }

    // Missiles that can be attacked and damaged.
    fun getMissileTargets(): List<MissileAPI> {
        var targets = this.missileTargets
        if (targets == null) {
            targets = Global.getCombatEngine().missiles.filter { missile ->
                missile.isValidTarget
            }

            this.missileTargets = targets
        }

        return targets
    }

    // Ships that can collide and block line of fire.
    fun getObstacles(): List<ShipAPI> {
        var obstacles = this.obstacles
        if (obstacles == null) {
            obstacles = Global.getCombatEngine().ships.filter { obstacle ->
                !obstacle.isExpired && obstacle.collisionClass == CollisionClass.SHIP
            }

            this.obstacles = obstacles
        }

        return obstacles
    }

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        shipTargets = null
        missileTargets = null
        obstacles = null
    }
}