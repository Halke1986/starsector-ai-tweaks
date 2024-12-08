package com.genir.aitweaks.core.debug

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.features.shipai.EngineController
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f

object TargetTest {
    private val projectilesCache: MutableSet<DamagingProjectileAPI> = mutableSetOf()

    fun advance(dt: Float) {
        removeAsteroids()
        installAI()

        val engine = Global.getCombatEngine()
        val ship: ShipAPI = engine.playerShip ?: return
        val ally: ShipAPI = engine.ships.firstOrNull { it.isShip && it.owner == 0 && it != ship } ?: return
        val target: ShipAPI? = engine.ships.firstOrNull { it.owner == 1 }

        val projectiles = engine.projectiles

//        projectiles.forEach { proj ->
//            if (!projectilesCache.contains(proj)) {
//                projectilesCache.add(proj)
//
//                val p = proj.location - ally.shieldCenterEvenIfNoShield
//                val v = proj.velocity - ally.velocity
//
//                val d = distanceToOrigin(p, v) ?: 0f
//                val e = d - ally.shieldRadiusEvenIfNoShield
//                debugPrint["estimate"] = "e $e"
//
//                log("e ${proj.location} ${proj.velocity} ${ally.velocity} ${ally.shieldCenterEvenIfNoShield} ${ally.shieldRadiusEvenIfNoShield} $e")
//
//
////                log("e ${getFacingStrict(proj.velocity)} $e")
//                log("------------------")
//            }
//
////            val l = (ally.shieldCenterEvenIfNoShield - proj.location).length
////            val d = l - ally.shieldRadiusEvenIfNoShield
////            if (d < 0)
////                debugPrint["d"] = d

        val p = ally.location - ship.location
        val r = ally.shieldRadiusEvenIfNoShield

        Debug.drawCircle(ally.location, r)

//        val ts = tangents(p, r)

//        drawLine(ship.location, ship.location + ts!!.first, RED)
//        drawLine(ship.location, ship.location + ts!!.second, RED)

//        }
    }

    class ShipAI(val ship: ShipAPI) : BaseEngineControllerAI() {
        private val controller: EngineController = EngineController(ship)

        override fun advance(dt: Float) {
//            ship.shield.toggleOn()
            ship.weaponGroupsCopy.forEach { it.toggleOn() }
            (ship as Ship).setNoWeaponSelected()

            Debug.print["hold"] = ship.allGroupedWeapons.firstOrNull()!!.customAI!!.shouldHoldFire ?: ""

            controller.heading(dt, Vector2f(0f, -1000f))
            controller.facing(dt, 90f)

            Debug.drawEngineLines(ship)
        }
    }

    class TargetAI(val ship: ShipAPI) : BaseEngineControllerAI() {
        private val controller: EngineController = EngineController(ship)

        val x = 2000f
        private val y = 1000f

        val location = Vector2f(x, y)

        override fun advance(dt: Float) {
            ship.weaponGroupsCopy.forEach { it.toggleOff() }
            (ship as Ship).setNoWeaponSelected()

            val toLocation = (location - ship.location)
            if (toLocation.length < 10f) {
                location.x = -location.x
            }

            controller.heading(dt, location)
            controller.facing(dt, toLocation.facing)
        }
    }

    class AllyAI(val ship: ShipAPI) : BaseEngineControllerAI() {
        private val controller: EngineController = EngineController(ship)

        val x = 1500f
        private val y = 0f

        val location = Vector2f(x, y)

        override fun advance(dt: Float) {
            ship.weaponGroupsCopy.forEach { it.toggleOff() }
            (ship as Ship).setNoWeaponSelected()

            val toLocation = (location - ship.location)
            if (toLocation.length < 10f) {
                location.x = -location.x
            }

//            debugPrint["ally speed"] = "ally ${ship.velocity.length}"

            controller.heading(dt, location)
            controller.facing(dt, toLocation.facing)
        }
    }

    private fun installAI() {
        val engine = Global.getCombatEngine()

        val ship: ShipAPI? = engine.playerShip
        if (ship != null && !ship.isUnderManualControl) {
            installAI(ship) { ShipAI(ship) }
        }

        val target: ShipAPI? = engine.ships.firstOrNull { it.owner == 1 }
        if (target != null) {
            installAI(target) { TargetAI(target) }
        }

        val ally: ShipAPI? = engine.ships.firstOrNull { it.isShip && it.owner == 0 && it != ship }
        if (ally != null) {
            installAI(ally) { AllyAI(ally) }
        }
    }
}
