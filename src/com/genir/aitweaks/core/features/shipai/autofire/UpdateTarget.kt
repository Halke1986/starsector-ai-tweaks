package com.genir.aitweaks.core.features.shipai.autofire

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.*
import com.genir.aitweaks.core.utils.asteroidGrid
import com.genir.aitweaks.core.utils.closestEntityFinder
import com.genir.aitweaks.core.utils.extensions.*
import com.genir.aitweaks.core.utils.missileGrid
import com.genir.aitweaks.core.utils.shipGrid
import lunalib.lunaSettings.LunaSettings
import org.lwjgl.util.vector.Vector2f

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
        // Estimate priority target.
        val priorityTarget = when {
            // Don't attack allies.
            attackTarget?.owner == weapon.ship.owner -> null

            // Try to follow ship attack target.
            else -> attackTarget
        }

        return when {
            // Hardpoint weapons track ships target even when it's outside their firing arc.
            priorityTarget != null && weapon.slot.isHardpoint -> priorityTarget

            // Select priority target, if it can be tracked.
            priorityTarget != null && canTrack(weapon, BallisticTarget.entity(priorityTarget), params) -> priorityTarget

            // Select alternative target.
            else -> selectEntity<ShipAPI>(shipGrid()) { !it.isFighter || alsoFighter }
        }
    }

    private inline fun <reified T : CombatEntityAPI> selectEntity(
        grid: CollisionGridAPI, crossinline isAcceptableTarget: (T) -> Boolean
    ): CombatEntityAPI? {
        // Try tracking current target.
        if (current is T && isAcceptableTarget(current) && canTrack(weapon, BallisticTarget.entity(current), params)) return current

        // Find the closest enemy entity that can be tracked by the weapon.
        return closestEntityFinder(weapon.location, weapon.totalRange, grid) {
            val target = BallisticTarget.entity(it)
            when {
                it !is T -> null
                it.owner == weapon.ship.owner -> null
                !it.isValidTarget -> null
                !isAcceptableTarget(it) -> null
                !canTrack(weapon, target, params) -> null
                else -> Hit(it, closestHitRange(weapon, target, params)!!, false)
            }
        }?.target
    }
}
