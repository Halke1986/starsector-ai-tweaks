package com.genir.aitweaks.core.features.shipai.autofire

import com.fs.starfarer.api.combat.WeaponAPI
import com.genir.aitweaks.core.utils.extensions.isValidTarget
import com.genir.aitweaks.core.utils.extensions.length
import com.genir.aitweaks.core.utils.extensions.totalRange
import com.genir.aitweaks.core.utils.firstShipAlongLineOfFire
import org.lazywizard.lazylib.ext.minus

/** Specialized AutofireAI implementation for Torchship plasma flamer.
 * Plasma flamer is made very trigger-happy, firing even when it will
 * miss or is out of range. */
class TadaPlasmaAI(weapon: WeaponAPI) : AutofireAI(weapon) {
    override fun calculateShouldFire(): HoldFire? {
        predictedHit = null

        if (target?.isValidTarget != true) return HoldFire.NO_TARGET
        if (intercept == null) return HoldFire.NO_HIT_EXPECTED

        holdFireIfOverfluxed()?.let { return it }

        // Fire only when the selected target can be hit. That way the weapon doesn't fire
        // on targets that are only briefly in the line of sight, when the weapon is turning.
        val ballisticParams = currentParams()
        val expectedHit = target?.let { analyzeHit(weapon, target!!, ballisticParams) }

        predictedHit = expectedHit

        // Check what actually will get hit, and hold fire if it's an ally or hulk.
        val actualHit = firstShipAlongLineOfFire(weapon, ballisticParams)
        val hit: Hit? = when {
            actualHit == null -> expectedHit
            expectedHit == null -> actualHit
            actualHit.range > expectedHit.range -> expectedHit
            else -> actualHit
        }
        predictedHit = hit

        avoidFriendlyFire(weapon, expectedHit, actualHit)?.let { return it }

        if ((target!!.location - weapon.location).length > weapon.totalRange * 1.7f) return HoldFire.OUT_OF_RANGE

        return null
    }
}
