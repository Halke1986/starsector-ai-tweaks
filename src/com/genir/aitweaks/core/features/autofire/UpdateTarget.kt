package com.genir.aitweaks.core.features.autofire

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.*
import com.genir.aitweaks.core.utils.*
import com.genir.aitweaks.core.utils.attack.*
import com.genir.aitweaks.core.utils.extensions.*
import lunalib.lunaSettings.LunaSettings
import org.lazywizard.lazylib.ext.minus
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
        weapon.hasAIHint(PD) -> selectFighter() ?: selectMissile() ?: selectShip()

        // Main weapons
        weapon.hasAIHint(ANTI_FTR) -> selectShip(alsoTargetFighters)
        weapon.hasAIHint(STRIKE) -> selectShip()
        weapon.ship.hullSpec.hullId.startsWith("guardian") -> selectShip()

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

    private fun selectMissile() = selectEntity<MissileAPI>(missileGrid()) { !it.isFlare || !weapon.ignoresFlares }

    private fun selectFighter(): CombatEntityAPI? = selectEntity<ShipAPI>(shipGrid()) { it.isFighter }

    private fun selectShip(alsoFighter: Boolean = false): CombatEntityAPI? {
        // Estimate priority target.
        val priorityTarget = when {
            // When ship is on vanilla escort assignment, the priority target needs to be estimated.
            attackTarget == null && weapon.isFrontFacing && weapon.ship.hasEscortAssignment -> estimateShipTarget(weapon)

            // Don't attack allies.
            attackTarget?.owner == weapon.ship.owner -> null

            // Try to follow ship attack target.
            else -> attackTarget
        }

        return when {
            // Hardpoint weapons track ships target even when it's outside their firing arc.
            priorityTarget != null && weapon.slot.isHardpoint -> priorityTarget

            // Select priority target, if it can be tracked.
            priorityTarget != null && canTrack(weapon, AttackTarget(priorityTarget), params) -> priorityTarget

            // Select alternative target.
            else -> selectEntity<ShipAPI>(shipGrid()) { !it.isFighter || alsoFighter }
        }
    }

    private inline fun <reified T : CombatEntityAPI> selectEntity(
        grid: CollisionGridAPI, crossinline isAcceptableTarget: (T) -> Boolean
    ): CombatEntityAPI? {
        // Try tracking current target.
        if (current is T && isAcceptableTarget(current) && canTrack(weapon, AttackTarget(current), params)) return current

        // Find the closest enemy entity that can be tracked by the weapon.
        return closestEntityFinder(weapon.location, weapon.totalRange, grid) {
            when {
                it !is T -> null
                it.owner == weapon.ship.owner -> null
                !it.isValidTarget -> null
                !isAcceptableTarget(it) -> null
                !canTrack(weapon, AttackTarget(it), params) -> null
                else -> Hit(it, closestHitRange(weapon, AttackTarget(it), params)!!, false)
            }
        }?.target
    }

    /** Fallback method of acquiring ship maneuver target.
     * Returns closest enemy ship "hit" by the ships heading vector. */
    private fun estimateShipTarget(weapon: WeaponAPI): ShipAPI? {
        val facing = unitVector(weapon.ship.facing)
        val isTarget = fun(entity: ShipAPI): Boolean {
            val dist = distanceToOrigin(weapon.ship.location - entity.location, facing) ?: return false
            return dist < entity.collisionRadius
        }

        return closestEntityFinder(weapon.ship.location, weapon.totalRange, shipGrid()) {
            when {
                it !is ShipAPI -> null
                !it.isValidTarget -> null
                it.isFighter -> null
                it.owner == weapon.ship.owner -> null
                !isTarget(it) -> null
                else -> Hit(it, (weapon.ship.location - it.location).length(), false)
            }
        }?.target as? ShipAPI
    }
}
