package deobfuscate

import com.genir.aitweaks.core.utils.extensions.classPath
import com.genir.aitweaks.launcher.loading.Symbols
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SymbolsTest {
    @Test
    fun symbols() {
        val s = Symbols()

        assertEquals("com/fs/starfarer/combat/ai/movement/maneuvers/B", s.approachManeuver.classPath)
        assertEquals("com/fs/starfarer/combat/ai/attack/D", s.autofireManager.classPath)
        assertEquals("com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO", s.threatEvalAI.classPath)
        assertEquals("com/fs/starfarer/combat/entities/Ship\$Oo", s.shipCommandWrapper.classPath)
        assertEquals("com/fs/starfarer/combat/entities/Ship\$oo", s.shipCommand.classPath)
        assertEquals("com/fs/starfarer/combat/ai/movement/maneuvers/oO0O", s.maneuver.classPath)

        assertEquals("o00000", s.advanceAutofireManager)
        assertEquals("new", s.commandShipCommandWrapper)
    }
}