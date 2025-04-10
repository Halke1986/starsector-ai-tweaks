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
    val ship = Global.getCombatEngine().playerShip ?: return
    val ships = Global.getCombatEngine().ships

//    if (!Global.getCombatEngine().isPaused){
//        log("***********************************************")
//    }

    ships.forEach {
//        Debug.drawCollisionRadius(it, Color.CYAN)
//        Debug.drawAccelerationLines(it)

//        Debug.drawCircle(it.location, it.collisionRadius * 1.4f, CYAN)
    }

//    removeAsteroids()
}
