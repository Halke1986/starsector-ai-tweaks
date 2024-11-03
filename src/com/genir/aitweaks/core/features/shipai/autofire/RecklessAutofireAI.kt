package com.genir.aitweaks.core.features.shipai.autofire

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.features.shipai.autofire.Hit.Type.HULL
import com.genir.aitweaks.core.features.shipai.autofire.Hit.Type.SHIELD
import com.genir.aitweaks.core.utils.extensions.isValidTarget
import com.genir.aitweaks.core.utils.extensions.length
import com.genir.aitweaks.core.utils.extensions.totalRange
import com.genir.aitweaks.core.utils.firstShipAlongLineOfFire
import org.lazywizard.lazylib.ext.minus

/** Specialized AutofireAI implementation for weapons that are most
 * effective when very trigger-happy. The AI will fire even if it
 * anticipates a miss or is out of range. */
class RecklessAutofireAI(weapon: WeaponAPI) : AutofireAI(weapon) {
    override fun calculateShouldFire(): HoldFire? {
        predictedHit = null

        val target: CombatEntityAPI = target ?: return HoldFire.NO_TARGET
        if (!target.isValidTarget) return HoldFire.NO_TARGET

        holdFireIfOverfluxed()?.let { return it }

        // Fire only when the selected target can be hit. That way the weapon doesn't fire
        // on targets that are only briefly in the line of sight, when the weapon is turning.
        val ballisticParams = currentParams()
        val expectedHit = analyzeHit(weapon, target, ballisticParams)
            ?: Hit(target, (target.location - weapon.location).length, if (target.shield?.isOn == true) SHIELD else HULL)

        predictedHit = expectedHit

        // Check what actually will get hit, and hold fire if it's an ally or hulk.
        val actualHit = firstShipAlongLineOfFire(weapon, ballisticParams)
        val hit: Hit = when {
            actualHit == null -> expectedHit
            actualHit.range > expectedHit.range -> expectedHit
            else -> actualHit
        }
        predictedHit = hit

        avoidFriendlyFire(weapon, expectedHit, actualHit)?.let { return it }

        if ((target.location - weapon.location).length > weapon.totalRange * 1.7f) return HoldFire.OUT_OF_RANGE

        return null
    }
}
