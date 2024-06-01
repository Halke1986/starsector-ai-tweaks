package shipai

import com.genir.aitweaks.features.shipai.loading.Deobfuscator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CustomClassLoader {
    @Test
    fun testObfTable() {
        // Values for Starsector 0.97a-RC11 Windows
        val obf = Deobfuscator(this.javaClass.classLoader).getDeobfuscatedSymbols()

        assertEquals(obf.combatEntityPackage, "o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO")

        assertEquals(obf.basicShipAIInnerClasses, listOf("1", "o"))
        assertEquals(obf.orderResponseModule, "I")
        assertEquals(obf.orderResponseModuleInner, "o")
        assertEquals(obf.flockingAIModule, "oOOO")
        assertEquals(obf.maneuverInterface, "oO0O")
        assertEquals(obf.shipAIInterface, "oO0O\$o")
        assertEquals(obf.combatEntityInterface, "B")
        assertEquals(obf.approachManeuver, "B")
        assertEquals(obf.moveManeuver, "U")

        assertEquals(obf.advance, "o00000")
        assertEquals(obf.getTarget, "o00000")
        assertEquals(obf.isDirectControl, "Õ00000")
        assertEquals(obf.doManeuver, "Object")
        assertEquals(obf.getDesiredHeading, "Ô00000")
        assertEquals(obf.getDesiredFacing, "Ò00000")
        assertEquals(obf.getDesiredStrafeHeading, "Object")
    }
}
