package maneuver

import com.genir.aitweaks.features.shipai.AIClassLoader
import com.genir.aitweaks.utils.CCT
import org.junit.jupiter.api.Test


class CustomClassLoader {
    @Test
    fun testLoadClass() {
        val cl = AIClassLoader()

//        cl.loadClass("com.fs.starfarer.combat.ai.BasicShipAI")
        cl.loadClass("com.genir.aitweaks.asm.shipai.AssemblyShipAI")
//        cl.loadClass("com.genir.aitweaks.asm.shipai.Strafe")
//        cl.loadClass("com.genir.aitweaks.asm.shipai.Move")
//        cl.loadClass("com.genir.aitweaks.asm.shipai.Approach")
//        val k = cl.loadClass("com.genir.aitweaks.asm.shipai.ManeuverAdapter")

//        val b = CCT.readClassBuffer(cl, "com.genir.aitweaks.asm.shipai.Approach")
//        CCT(listOf()).apply(b)
    }
}

