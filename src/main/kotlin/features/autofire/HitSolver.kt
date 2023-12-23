package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.utils.distanceToOriginSqr
import com.genir.aitweaks.utils.div
import com.genir.aitweaks.utils.extensions.radius
import com.genir.aitweaks.utils.solve
import com.genir.aitweaks.utils.unitVector
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f

class HitSolver(val weapon: WeaponAPI) {
    private val weaponUnitVector = unitVector(weapon.currAngle)

    private fun pv(target: CombatEntityAPI): Pair<Vector2f, Vector2f> = Pair(
        weapon.location - target.location,
        weaponUnitVector + (weapon.ship.velocity - target.velocity) / weapon.projectileSpeed,
    )

    fun willHit(target: CombatEntityAPI): Boolean {
        val (p, v) = pv(target)
        return distanceToOriginSqr(p, v) <= target.radius * target.radius
    }

    fun hitRange(target: CombatEntityAPI): Float? {
        val (p, v) = pv(target)
        val range = solve(p, v, target.radius, 0f)
        return if (range <= weapon.range) range else null
    }
}
