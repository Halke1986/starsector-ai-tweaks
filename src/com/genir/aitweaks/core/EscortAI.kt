package com.genir.aitweaks.core

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType.NONE
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType.PHASE
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand.*
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.utils.log
import org.lazywizard.lazylib.ext.combat.getNearbyAllies
import org.lazywizard.lazylib.ext.combat.getNearbyEnemies
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import java.lang.Float.min
import kotlin.math.roundToInt

/* Goal: stick together and display nether cowardice nor reckless behavior */
class EscortAI : BaseEveryFrameCombatPlugin() {
    override fun advance(dt: Float, events: MutableList<InputEventAPI>?): Unit = with(Global.getCombatEngine()) {
        if (!isPaused) ships.filter {
            it.isShip && it.hasShield/* && it.owner == 0*/ /*player*/
        }.sortedWith(compareBy({ it.owner }, { it.isTank }, { it.deploymentPoints }, { it.variant.displayName }, { it.name })).onEach { ship ->
            val mainGuns = ship.usableWeapons.filter { !it.isMissile && !it.isPDSpec } //.apply { log("weapons ${map { "${it.displayName}:${it.range}" }}") }
            val maxRange = mainGuns.maxOfOrNull { it.range }
                ?: if (ship.isTank) 1000f else return@onEach ship.report("unsupported ${ship.variant.hullMods} weapons ${ship.usableWeapons.map { it.displayName }.toSet()}")
            val minRange = mainGuns.minOfOrNull { it.range } ?: (maxRange * .9f)

            val sightRange = 3000f
            val enemyShips = ship.getNearbyEnemies(sightRange).filter { it.isShip }.sortedBy { ship.distanceTo(it) }
            val enemy = enemyShips.firstOrNull() ?: return@onEach
            val alliedShips = (ship.getNearbyAllies(sightRange).filter { it.isShip } - ship).sortedBy { it.distanceTo(enemy) }
            if (alliedShips.sumOf { it.deploymentPoints } / enemyShips.sumOf { it.deploymentPoints } > 2)
                return@onEach// ship.report("insignificant enemies ${enemyShips.sumOf { it.deploymentPoints }}")

            val dist = ship.distanceTo(enemy)
            val supportingShips = alliedShips.filter { it.distanceTo(enemy) / dist < 1.2 }
            val supportingShipsDist = supportingShips.map { (it.distanceTo(enemy) / dist * 100).roundToInt() / 100f }
            val tankingAlly = supportingShips.firstOrNull()?.takeIf { it.distanceTo(enemy) < dist }

            /*listOf(ship.ai).filterIsInstance<BasicShipAI>()*/
            if (ship.run { isCapital || isCruiser }) alliedShips.filter {
                it.variant.hasHullMod("escort_package")
                    && it.distanceTo(ship) > 900 // escort_package effectiveness drop off from 1000
                    && it.assignment?.target?.location == ship.location
            }.maxByOrNull { it.distanceTo(ship) }?.let {
                ship.command(if (ship.vectorTo(it).isLeftOf(ship.vectorTo(enemy))) STRAFE_LEFT else STRAFE_RIGHT) // .also { ship.report("sticking to ${escort.name}") }
            }

            val shieldDmgCapacity = ship.shieldDmgCapacity
            val canTank3Sabots = shieldDmgCapacity > 6000
            if (tankingAlly != null && ship.fluxLevel < 0.3 && canTank3Sabots)
                ship.command(ACCELERATE).also { ship.report("supporting ${tankingAlly.identity} supportingShipsDist: $supportingShipsDist") }
            else if (ship.isTank && ship.fluxLevel < min(0.8f, 0.01f + 0.2f * supportingShips.size))
                ship.command(ACCELERATE).also { ship.report("tanking ${ship.fluxLevel} dist $dist supportingShipsDist: $supportingShipsDist") }
            else if (ship.isTank && supportingShips.isEmpty())
                ship.command(ACCELERATE_BACKWARDS).also { ship.report("waiting for support dist $dist supportingShipsDist: $supportingShipsDist") }
            else if (dist < minRange)
                ship.command(ACCELERATE_BACKWARDS).also { ship.report("maintaining $minRange dist $dist shieldDmgCapacity: $shieldDmgCapacity") }
            else if (dist <= maxRange || tankingAlly == null && !canTank3Sabots)
                ship.command(ACCELERATE_BACKWARDS).also { ship.report("backing off $maxRange dist $dist shieldDmgCapacity: $shieldDmgCapacity") }
            else if (ship.isTank)
                ship.report("? ${supportingShips.size}")
        }
    }
}

val ShipAPI.shieldDmgCapacity get() = fluxLeft / shield.fluxPerPointOfDamage * (if (system.displayName == "Fortress Shield") 10 else 1)
val ShipAPI.isTank get() = (listOf("fluxshunt").any(variant::hasHullMod) || listOf("Fortress Shield").contains(system.displayName))
val ShipAPI.isPhase get() = hullSpec.shieldSpec.type == PHASE
val ShipAPI.hasShield get() = !isPhase && hullSpec.shieldSpec.type != NONE
val ShipAPI.identity get() = listOf(owner, variant.displayName.padEnd(10), hullSpec.hullName.padEnd(15)/* if (isTank) "(tank)" else "", */).joinToString(" ")
fun ShipAPI.report(action: String): Unit = if (owner != 0) Unit else listOf(identity,
//    if (this is com.fs.starfarer.combat.entities.Ship) commands.joinToString { it.new.name }.padEnd(21) else "",
    action).joinToString(" ").let(::log)

fun ShipAPI.distanceTo(s: ShipAPI) = distanceTo(s.location) - s.shieldRadiusEvenIfNoShield - shieldRadiusEvenIfNoShield
fun ShipAPI.distanceTo(v: Vector2f) = vectorTo(v).length
fun ShipAPI.vectorTo(s: ShipAPI) = vectorTo(s.location)
fun ShipAPI.vectorTo(v: Vector2f) = v - location

fun Vector2f.isLeftOf(ref: Vector2f) = (ref.x * this.y) - (ref.y * this.x) > 0
