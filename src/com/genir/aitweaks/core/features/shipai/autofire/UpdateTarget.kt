package com.genir.aitweaks.core.features.shipai.autofire

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.*
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.extensions.*
import lunalib.lunaSettings.LunaSettings
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs

private const val alsoTargetFighters = true

class UpdateTarget(
    private val weapon: WeaponAPI,
    private val current: CombatEntityAPI?,
    private val attackTarget: ShipAPI?,
    private val params: BallisticParams,
) {
    val target: CombatEntityAPI? = when {
        Global.getCurrentState() == GameState.TITLE && titleScreenFireIsOn() -> selectAsteroid()

        // Obligatory PD
        weapon.hasAIHint(PD_ONLY) && weapon.hasAIHint(ANTI_FTR) -> selectFighter() ?: selectMissile()
        weapon.hasAIHint(PD_ONLY) -> selectMissile() ?: selectFighter()

        // PD
        weapon.hasAIHint(PD) && weapon.hasAIHint(ANTI_FTR) -> selectFighter() ?: selectMissile() ?: selectShip()
        weapon.hasAIHint(PD) -> selectMissile() ?: selectFighter() ?: selectShip()

        // Main weapons
        weapon.hasAIHint(ANTI_FTR) -> selectShip(alsoTargetFighters)
        weapon.hasAIHint(STRIKE) -> selectShip()
        weapon.ship.hullSpec.hullId.startsWith("guardian") -> selectShip()
        weapon.ship.customShipAI != null -> selectShip() // Custom AI doesn't attack fighters with main weapons, for now.

        else -> selectShip() ?: selectFighter()?.let { if (!it.isSupportFighter) it else null }
    }

    private fun titleScreenFireIsOn() = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_title_screen_fire") == true

    /** Target asteroid selection. Selects asteroid only when the weapon and asteroid
     * are both in viewport. Otherwise, it looks weird on the title screen. */
    private fun selectAsteroid(): CombatEntityAPI? {
        val inViewport = { location: Vector2f -> Global.getCombatEngine().viewport.isNearViewport(location, 0f) }
        if (!inViewport(weapon.location)) return null
        return selectEntity<CombatAsteroidAPI>(asteroidGrid()) { inViewport(it.location) }
    }

    private fun selectMissile(): CombatEntityAPI? {
        return selectEntity<MissileAPI>(missileGrid()) { !it.isFlare || !weapon.ignoresFlares }
    }

    private fun selectFighter(): CombatEntityAPI? {
        return selectEntity<ShipAPI>(shipGrid()) { it.isFighter }
    }

    private fun selectShip(alsoFighter: Boolean = false): CombatEntityAPI? {
        val priorityTarget: ShipAPI? = attackTarget

        // Try to follow ship attack target.
        val selectPriorityTarget = when {
            priorityTarget == null -> false

            !priorityTarget.isValidTarget -> false

            // Don't attack allies.
            priorityTarget.owner == weapon.ship.owner -> false

            // Hardpoint weapons select ship target even when it's outside their firing arc.
            weapon.slot.isHardpoint -> true

            // Turreted weapons select ship target if it can be tracked.
            canTrack(weapon, BallisticTarget.entity(priorityTarget), params) -> true

            else -> false
        }

        if (selectPriorityTarget) return priorityTarget

        // Select alternative target.
        return selectEntity<ShipAPI>(shipGrid()) { !it.isFighter || alsoFighter }
    }

    private inline fun <reified T : CombatEntityAPI> selectEntity(
        grid: CollisionGridAPI, crossinline isAcceptableTarget: (T) -> Boolean
    ): CombatEntityAPI? {
        // Try tracking the current target.
        when {
            current !is T -> Unit

            !current.isValidTarget -> Unit

            !isAcceptableTarget(current) -> Unit

            !canTrack(weapon, BallisticTarget.entity(current), params) -> Unit

            else -> return current
        }

        // Find the closest enemy entity that can be tracked by the weapon.
        return closestEntityFinder(weapon.location, weapon.totalRange, grid) {
            val target = BallisticTarget.entity(it)
            when {
                it !is T -> null
                it.owner == weapon.ship.owner -> null
                !it.isValidTarget -> null
                !isAcceptableTarget(it) -> null
                !canTrack(weapon, target, params) -> null

                // Evaluate the target based on angle and distance.
                else -> {
                    val angle = shortestRotation((target.location - weapon.location).facing, weapon.currAngle) * DEGREES_TO_RADIANS
                    val angleWeight = 0.75f
                    val evalAngle = abs(angle) * angleWeight

                    // Prioritize closer targets. Avoid attacking targets out of effective weapons range.
                    val dist = closestHitRange(weapon, target, params)!!
                    val evalDist = (dist / weapon.totalRange)

                    evalAngle + evalDist
                }
            }
        }
    }
}
