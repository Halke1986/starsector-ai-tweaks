package com.genir.aitweaks.core.extensions

import com.fs.starfarer.api.loading.WeaponSlotAPI
import com.genir.aitweaks.core.utils.solve
import com.genir.aitweaks.core.utils.types.Direction

/** Weapon slot range from the center of the ship, along the attack facing.
 * attackFacing is in ship frame of reference*/
fun WeaponSlotAPI.rangeFromShipCenter(attackFacing: Direction, weaponRange: Float): Float {
    val p = -location
    val v = attackFacing.unitVector
    return solve(p, v, weaponRange)?.smallerNonNegative ?: 0f
}
