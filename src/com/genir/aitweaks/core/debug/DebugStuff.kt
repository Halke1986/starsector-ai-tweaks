package com.genir.aitweaks.core.debug

import com.fs.starfarer.api.Global

/**
 *
 * FRAME UPDATE ORDER (for AI controlled ship)
 *
 * ship movement
 * AI
 * ship advance:
 *   engine controller process commands
 *   weapons:
 *      fire projectile
 *      update aim
 * (ship movement, AI, ship advance LOOP for fast time ships)
 *
 * EFSs
 *
 */

internal fun debug(dt: Float) {
    try {
        val playerShip = Global.getCombatEngine().playerShip ?: return
        val ships = Global.getCombatEngine().ships

//        Global.getCombatEngine().missiles.forEach { missile ->
//            Debug.drawCollisionRadius(missile)
//        }

//    if (!Global.getCombatEngine().isPaused){
//        log("***********************************************")
//    }

        //  Debug.drawCircle(playerShip.location, playerShip.maxSpeed, Color.GRAY)

        ships.forEach { ship ->
//            if (it != ship) {
//                installAI(it) { MirrorTargetAI(it, ship) }
//            Debug.drawEngineLines(ship)
//            }
//            Debug.drawCollisionRadius(ship, Color.CYAN)
//            Debug.drawCircle(ship.location, ship.collisionRadius * 1.4f, Color.CYAN)

//            Debug.print[ship] = "${ship.Id} ${ship.name}"
        }

//    removeAsteroids()
    } catch (e: Exception) {
        Debug.print["crash"] = e
    }
}
