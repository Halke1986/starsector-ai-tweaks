package com.genir.aitweaks.core.extensions

import com.fs.starfarer.api.loading.WeaponSlotAPI
import com.genir.aitweaks.core.utils.Solution
import com.genir.aitweaks.core.utils.solve
import com.genir.aitweaks.core.utils.types.Direction

/** Weapon slot range from the center of the ship, along the attack facing.
 * attackFacing is in ship frame of reference*/
fun WeaponSlotAPI.rangeFromShipCenter(attackFacing: Direction, weaponRange: Float): Float {
    val p = -location
    val v = attackFacing.unitVector
    val range = solve(p, v, weaponRange, Solution.SMALLER_NON_NEGATIVE)
    if (range.isNaN()) {
        return 0f
    }

    return range
}
