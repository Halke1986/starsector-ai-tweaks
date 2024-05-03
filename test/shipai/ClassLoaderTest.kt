package shipai

import com.genir.aitweaks.features.shipai.loading.ObfTable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CustomClassLoader {
    @Test
    fun testObfTable() {
        // Values for Starsector 0.97a-RC11 Windows
        val obf = ObfTable()

        assertEquals(obf.orderResponseModule, "I")
        assertEquals(obf.flockingAI, "oOOO")
        assertEquals(obf.maneuver, "oO0O")
        assertEquals(obf.shipAI, "oO0O\$o")
        assertEquals(obf.combatEntity, "B")
        assertEquals(obf.combatEntityPackage, "o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO")
        assertEquals(obf.approach, "B")
        assertEquals(obf.move, "U")

        assertEquals(obf.advance, "o00000")
        assertEquals(obf.getTarget, "o00000")
        assertEquals(obf.isDirectControl, "Õ00000")
        assertEquals(obf.doManeuver, "Object")
        assertEquals(obf.getDesiredHeading, "Ô00000")
        assertEquals(obf.getDesiredFacing, "Ò00000")
        assertEquals(obf.getDesiredStrafeHeading, "Object")
    }
}
