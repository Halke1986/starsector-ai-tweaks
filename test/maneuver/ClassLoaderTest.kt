package maneuver

import org.junit.jupiter.api.Test

class CustomClassLoader {
    @Test
    fun testLoadClass() {
        val cl = this.javaClass.classLoader

        val basicShipAI = cl.loadClass("com.fs.starfarer.combat.ai.BasicShipAI")
        val basicShipAIStream = cl.getResourceAsStream("com/fs/starfarer/combat/ai/BasicShipAI.class")

//        print(basicShipAIStream.readBytes().size)

        println(basicShipAI.isInstance(null))

    }
}

