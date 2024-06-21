package com.genir.aitweaks.features.shipai.ai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.debug.drawCircle
import com.genir.aitweaks.debug.drawLine
import com.genir.aitweaks.features.shipai.CustomAIManager
import com.genir.aitweaks.utils.aitStash
import com.genir.aitweaks.utils.extensions.hasAIType
import org.lazywizard.lazylib.ext.minus
import java.awt.Color

class AttackCoord : BaseEveryFrameCombatPlugin() {
    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        val customAI = CustomAIManager().getCustomAIClass() ?: return
        val ships = Global.getCombatEngine().ships.asSequence()
        val ais = ships.filter { it.hasAIType(customAI) }.mapNotNull { it.aitStash.maneuverAI }

        // Divide attacking AIs into squads attacking the same target.
        val squads: MutableMap<ShipAPI, MutableList<Maneuver>> = mutableMapOf()
        ais.forEach { attackerAI ->
            val target = attackerAI.maneuverTarget ?: return@forEach
            val squad = squads[target]

            if (squad == null) squads[target] = mutableListOf(attackerAI)
            else squad.add(attackerAI)
        }

        squads.forEach { coordinateSquad(it.key, it.value) }
    }

    private fun coordinateSquad(target: ShipAPI, squad: List<Maneuver>) {
        squad.forEach { attackerAI ->
            val headingPoint = attackerAI.headingPoint ?: attackerAI.ship.location
//            drawLine(attackerAI.ship.location, headingPoint, Color.YELLOW)
//            drawCircle(target.location, (headingPoint - target.location).length())
        }
    }
}