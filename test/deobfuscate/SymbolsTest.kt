package deobfuscate

import com.genir.aitweaks.launcher.loading.Symbols
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SymbolsTest {
    @Test
    fun symbols() {
        val s = Symbols()

        assertEquals("com.fs.starfarer.combat.ai.movement.oOOO", s.flockingAI.name)
        assertEquals("com.fs.starfarer.combat.ai.movement.maneuvers.B", s.approachManeuver.name)
        assertEquals("com.fs.starfarer.combat.ai.attack.D", s.autofireManager.name)
        assertEquals("com.fs.starfarer.combat.ai.movement.maneuvers.oO0O", s.maneuver.name)
        assertEquals("com.fs.starfarer.combat.entities.Ship\$Oo", s.shipCommandWrapper.name)
        assertEquals("com.fs.starfarer.combat.entities.Ship\$oo", s.shipCommand.name)
        assertEquals("com.fs.starfarer.combat.ai.O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO", s.threatEvalAI.name)
        assertEquals("com.fs.starfarer.combat.o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO.B", s.combatEntity.name)
        assertEquals("com.fs.starfarer.combat.systems.thissuper", s.weapon.name)
        assertEquals("com.fs.starfarer.combat.entities.ship.trackers.super", s.aimTracker.name)
        assertEquals("com.fs.starfarer.ui.o0O0", s.button.name)
        assertEquals("com.fs.starfarer.title.super.new\$oo", s.playerAction.name)
        assertEquals("com.fs.starfarer.title.super.new", s.keymap.name)

        assertEquals("o00000", s.autofireManager_advance.name)
        assertEquals("new", s.shipCommandWrapper_getCommand.name)
        assertEquals("o00000", s.maneuver_getTarget.name)
        assertEquals("new", s.aimTracker_setTargetOverride.name)
        assertEquals("Ø00000", s.keymap_isKeyDown.name)
    }
}