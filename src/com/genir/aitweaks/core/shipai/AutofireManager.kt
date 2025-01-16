package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.core.Obfuscated
import com.genir.aitweaks.core.extensions.isMissile
import com.genir.aitweaks.core.extensions.isPD
import com.genir.aitweaks.core.shipai.autofire.SyncFire
import com.genir.aitweaks.core.utils.defaultAIInterval
import org.lwjgl.util.vector.Vector2f

class AutofireManager(val ship: ShipAPI) : Obfuscated.AutofireManager {
    private val updateInterval: IntervalUtil = defaultAIInterval()
    private var autofireCount = 0

    override fun autofireManager_advance(dt: Float, threatEvalAI: Obfuscated.ThreatEvaluator?, missileDangerDir: Vector2f?) {
        updateInterval.advance(dt)
        if (updateInterval.intervalElapsed()) {
            autofireCount = ensureAutofire()
            SyncFire.updateWeaponSync(ship)
        }

        // If all 7 weapon groups are auto-firing, then shipAI forcefully selects one of them
        // to control manually. Block SELECT_GROUP command to prevent that.
        if (autofireCount == 7) {
            ship.blockCommandForOneFrame(ShipCommand.SELECT_GROUP)
        }
    }

    private fun ensureAutofire(): Int {
        return ship.weaponGroupsCopy.count { group ->
            val weapons = group.weaponsCopy

            val shouldAutofire = when {
                weapons.any { it.isPD } -> true
                !ship.hullSpec.isBuiltInMod("missile_reload") && weapons.any { it.isMissile } -> false
                else -> true
            }

            if (shouldAutofire) {
                // Deselect group that should be auto-firing.
                if (ship.selectedGroupAPI == group) {
                    (ship as Ship).setNoWeaponSelected()
                }
                group.toggleOn()
            } else {
                group.toggleOff()
            }

            shouldAutofire
        }
    }
}
