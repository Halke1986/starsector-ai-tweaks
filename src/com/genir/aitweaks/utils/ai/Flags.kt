package com.genir.aitweaks.utils.ai

import com.fs.starfarer.api.combat.ShipAPI
import com.genir.aitweaks.features.maneuver.Maneuver
import com.genir.aitweaks.utils.frameTracker
import org.lwjgl.util.vector.Vector2f

private const val flagsKey = "aitweaks_aiflags"

const val maxAge = 0.5f

class Flags {
    /** Enemy target the ship is attacking. */
    var attackTarget: ShipAPI? = null
        get() = if (frameTracker - attackTargetT < maxAge) field else null
        set(value) {
            field = value; attackTargetT = frameTracker
        }

    var maneuverAI: Maneuver? = null
        get() = if (frameTracker - maneuverAIT < maxAge) field else null
        set(value) {
            field = value; maneuverAIT = frameTracker
        }

    /** Enemy target the ship is maneuvering relative to.
     * Duplicate of vanilla MANEUVER_TARGET. */
    var maneuverTarget: ShipAPI? = null
        get() = if (frameTracker - maneuverTargetT < maxAge) field else null
        set(value) {
            field = value; maneuverTargetT = frameTracker
        }

    /** Point at which the ship is aiming hardpoint weapons to properly lead its target. */
    var aimPoint: Vector2f? = null
        get() = if (frameTracker - aimPointT < maxAge) field else null
        set(value) {
            field = value; aimPointT = frameTracker
        }

    private var attackTargetT = 0f
    private var maneuverAIT = 0f
    private var maneuverTargetT = 0f
    private var aimPointT = 0f
}

val ShipAPI.AITFlags: Flags
    get() {
        if (!this.customData.containsKey(flagsKey))
            this.setCustomData(flagsKey, Flags())
        return this.customData[flagsKey] as Flags
    }
