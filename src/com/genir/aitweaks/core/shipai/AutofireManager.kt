package com.genir.aitweaks.core.shipai

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponGroupAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.genir.aitweaks.core.extensions.weapons
import com.genir.aitweaks.core.shipai.autofire.SyncFire
import com.genir.aitweaks.core.utils.defaultAIInterval
import com.genir.starfarer.combat.ai.ThreatEvaluator
import com.genir.starfarer.combat.ai.attack.AttackAIModule
import com.genir.starfarer.combat.entities.Ship
import org.lwjgl.util.vector.Vector2f
import java.lang.reflect.Field

class AutofireManager(val ship: ShipAPI) : com.genir.starfarer.combat.ai.attack.AutofireManager {
    private val updateInterval: IntervalUtil = defaultAIInterval()
    private var autofireCount = 0

    override fun autofireManager_advance(dt: Float, threatEvalAI: ThreatEvaluator?, missileDangerDir: Vector2f?) {
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
        return ship.weaponGroupsCopy.count { group: WeaponGroupAPI ->
            val weapons = group.weapons

            val shouldAutofire = shouldAutofire(group)
            if (shouldAutofire) {
                // Deselect group that should be auto-firing.
                if (ship.selectedGroupAPI == group) {
                    (ship as Ship).setNoWeaponSelected()
                }

                group.toggleOn()
            } else {
                group.toggleOff()
            }

            return@count shouldAutofire
        }
    }

    private fun shouldAutofire(group: WeaponGroupAPI): Boolean {
        return when {
            // Preserve the vanilla behavior of PD weapons forcing
            // autofire for the entire group. As opposed to vanilla,
            // count ammo based PD weapons as well.
            group.weapons.any { it.isPD } -> {
                true
            }

            // Weapons designed to be autofire-only force the entire group into autofire.
            group.weapons.any { it.hasAIHint(WeaponAPI.AIHints.NO_MANUAL_FIRE) } -> {
                true
            }

            // Do not conserve missiles if ship can regenerate them.
            ship.hullSpec.isBuiltInMod("missile_reload") -> {
                true
            }

            // Do not autofire limited ammo missiles.
            group.weapons.any { it.isMissile && !it.hasAIHint(WeaponAPI.AIHints.DO_NOT_CONSERVE) } -> {
                false
            }

            else -> {
                true
            }
        }
    }

    companion object {
        /** Replace vanilla autofire manager with AI Tweaks adapter. */
        fun inject(ship: ShipAPI, attackModule: AttackAIModule) {
            // Find the obfuscated AttackAIModule.autofireManager field.
            val fields: Array<Field> = AttackAIModule::class.java.declaredFields
            val field = fields.first { it.type.isInterface && it.type.methods.size == 1 }
            field.setAccessible(true)

            // AutofireManager is already overridden.
            if (AutofireManager::class.java.isInstance(field.get(attackModule))) {
                return
            }

            field.set(attackModule, AutofireManager(ship))
        }
    }
}
