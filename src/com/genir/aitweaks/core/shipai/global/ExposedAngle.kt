package com.genir.aitweaks.core.shipai.global

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.genir.aitweaks.core.extensions.*
import com.genir.aitweaks.core.shipai.Preset
import com.genir.aitweaks.core.utils.angularSize
import com.genir.aitweaks.core.utils.types.Arc
import com.genir.aitweaks.core.utils.types.Direction.Companion.toDirection
import org.lwjgl.util.vector.Vector2f

class ExposedAngle(private val side: Int) : BaseEveryFrameCombatPlugin() {
    var angles: Map<ShipAPI, Arc> = mapOf()
        private set

    override fun advance(dt: Float, events: MutableList<InputEventAPI>?) {
        val enemies: List<ShipAPI> = Global.getCombatEngine().ships.filter {
            !it.isFighter && it.owner == side xor 1 && it.isValidTarget
        }

        angles = enemies.associateWith { enemy ->
            exposedAngle(enemy, enemies)
        }
    }

    private fun exposedAngle(target: ShipAPI, enemies: List<ShipAPI>): Arc {
        val arcs: MutableList<Arc> = borderArcs(target)
        for (other in enemies) {
            if (other.root == target.root) {
                continue
            }

            val toOther: Vector2f = other.location - target.location
            if (toOther.length - (other.collisionRadius + target.collisionRadius) > 1700f) {
                continue
            }

            arcs.add(Arc(
                angle = angularSize(toOther.lengthSquared, other.collisionRadius).coerceAtMost(180f),
                facing = toOther.facing
            ))
        }

        val exposedArcs: List<Arc> = Arc.arcSetComplement(arcs)
        return exposedArcs.maxWithOrNull(compareBy { it.angle }) ?: Arc(0f, 0f.toDirection)
    }

    private fun borderArcs(target: ShipAPI): MutableList<Arc> {
        val engine: CombatEngineAPI = Global.getCombatEngine()
        val limitX: Float = engine.mapWidth / 2f - Preset.borderHardNoGoZone
        val limitY: Float = engine.mapHeight / 2f - Preset.borderHardNoGoZone

        val arcs: MutableList<Arc> = mutableListOf()

        if (target.location.x > limitX) {
            val overLimit = (target.location.x - limitX) / Preset.borderHardNoGoZone
            arcs.add(Arc(angle = 180f * overLimit, facing = 0f.toDirection))
        }

        if (target.location.x < -limitX) {
            val overLimit = (limitX + target.location.x) / Preset.borderHardNoGoZone
            arcs.add(Arc(angle = 180f * overLimit, facing = 180f.toDirection))
        }

        if (target.location.y > limitY) {
            val overLimit = (target.location.y - limitY) / Preset.borderHardNoGoZone
            arcs.add(Arc(angle = 180f * overLimit, facing = 90f.toDirection))
        }

        if (target.location.y < -limitY) {
            val overLimit = (limitY + target.location.y) / Preset.borderHardNoGoZone
            arcs.add(Arc(angle = 180f * overLimit, facing = 270f.toDirection))
        }

        return arcs
    }
}
