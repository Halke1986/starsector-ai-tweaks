package maneuver

import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.combat.entities.Ship
import com.genir.aitweaks.features.maneuver.AIClassLoader
import com.genir.aitweaks.features.maneuver.decodeHex
import com.genir.aitweaks.features.maneuver.raw.`assemblyShipAI$o`
import org.junit.jupiter.api.Test
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

class CustomClassLoader {
    @Test
    fun testLoadClass() {
        val loader = AIClassLoader()

        `assemblyShipAI$o`.decodeHex()

//        val klas = loader.loadClass("com.genir.aitweaks.asm.combat.ai.AssemblyShipAI\$o")
//        val type = MethodType.methodType(Void.TYPE, Ship::class.java, ShipAIConfig::class.java)

//        val ctor = MethodHandles.lookup().findConstructor(klas, type)
//
//        println(exampleClass.fx(35f))
    }


}

