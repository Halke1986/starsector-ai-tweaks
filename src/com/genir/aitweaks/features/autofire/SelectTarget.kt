package com.genir.aitweaks.features.autofire

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.WeaponAPI.AIHints.*
import com.genir.aitweaks.utils.*
import com.genir.aitweaks.utils.attack.*
import com.genir.aitweaks.utils.extensions.*
import lunalib.lunaSettings.LunaSettings
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f

private const val alsoTargetFighters = true

class SelectTarget(
    private val weapon: WeaponAPI,
    private val current: CombatEntityAPI?,
    private val shipTarget: ShipAPI?,
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
        else -> selectShip() ?: selectFighter()?.let { if (!it.isSupportFighter) it else null }
    }

    private fun titleScreenFireIsOn() = LunaSettings.getBoolean("aitweaks", "aitweaks_title_screen_fire") == true

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
        // Use ship target as a priority. When ship is on escort assignment
        // or flamed out, the priority target needs to be estimated instead.
        val priorityTarget = when {
            shipTarget == null && weapon.frontFacing && weapon.ship.hasEscortAssignment -> estimateShipTarget(weapon)
            weapon.frontFacing && weapon.ship.engineController.isFlamedOut -> estimateShipTarget(weapon)
            shipTarget?.isValidTarget == true && shipTarget.owner != weapon.ship.owner -> shipTarget
            else -> null
        }

        // Prioritize ship target. Hardpoint weapons track ships target even when it's outside their firing arc.
        return when {
            priorityTarget != null && weapon.slot.isHardpoint -> priorityTarget
            priorityTarget != null && canTrack(weapon, AttackTarget(priorityTarget), params) -> priorityTarget
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


