package com.genir.aitweaks.core.debug

import com.fs.starfarer.api.Global
import java.awt.Color.CYAN

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
//    val ship = Global.getCombatEngine().playerShip ?: return
    val ships = Global.getCombatEngine().ships

    ships.forEach{
        Debug.drawCircle(it.location, it.collisionRadius + 15f, CYAN)

//        Debug.drawAccelerationLines(it)
    }
}
