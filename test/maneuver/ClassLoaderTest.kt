package maneuver

import com.genir.aitweaks.utils.CCT
import com.genir.aitweaks.utils.ClassConstantTransformer
import com.genir.aitweaks.utils.ClassConstantTransformer.Transform
import com.genir.aitweaks.utils.ClassConstantTransformer.newTransform
import org.junit.jupiter.api.Test

interface I {
    fun f(): Any
}

class C : I {
    override fun f(): Float = 1f
}

class CustomClassLoader {
    @Test
    fun testLoadClass() {

        (C() as I).f()

//        val cl = this.javaClass.classLoader
//
//        val vanillaPath = "com/fs/starfarer"
//        val asmPath = "com/genir/aitweaks"
//
//        val transformer1 = ClassConstantTransformer(listOf<Transform>(
//            newTransform("$vanillaPath/combat/ai/BasicShipAI", "$asmPath/asm/combat/ai/AssemblyShipAI"),
//            newTransform("$vanillaPath/combat/ai/I", "$asmPath/asm/combat/ai/OrderResponseModule"),
//
//            newTransform("$vanillaPath/combat/ai/movement/BasicEngineAI", "$asmPath/features/maneuver/OverrideEngineAI"),
//            newTransform("$vanillaPath/combat/ai/movement/maneuvers/StrafeTargetManeuverV2", "$asmPath/features/maneuver/Strafe"),
//            newTransform("$vanillaPath/combat/ai/movement/maneuvers/B", "$asmPath/features/maneuver/Approach"),
//            newTransform("$vanillaPath/combat/ai/movement/maneuvers/U", "$asmPath/features/maneuver/Move"),
//        ))
//
//        val transformer2 = CCT(listOf(
//            CCT.newTransform("$vanillaPath/combat/ai/BasicShipAI", "$asmPath/asm/combat/ai/AssemblyShipAI"),
//            CCT.newTransform("$vanillaPath/combat/ai/I", "$asmPath/asm/combat/ai/OrderResponseModule"),
//
//            CCT.newTransform("$vanillaPath/combat/ai/movement/BasicEngineAI", "$asmPath/features/maneuver/OverrideEngineAI"),
//            CCT.newTransform("$vanillaPath/combat/ai/movement/maneuvers/StrafeTargetManeuverV2", "$asmPath/features/maneuver/Strafe"),
//            CCT.newTransform("$vanillaPath/combat/ai/movement/maneuvers/B", "$asmPath/features/maneuver/Approach"),
//            CCT.newTransform("$vanillaPath/combat/ai/movement/maneuvers/U", "$asmPath/features/maneuver/Move"),
//        ))
//
//        val stream = cl.getResourceAsStream("com/fs/starfarer/combat/ai/BasicShipAI.class")!!
//
//        var size = 0
//        var buffer = ByteArray(1024)
//        while (stream.available() > 0) {
//            size += stream.read(buffer, size, buffer.size - size)
//            if (size == buffer.size) {
//                buffer += ByteArray(buffer.size)
//            }
//        }
//
//
//        val vanillaClassData = buffer.sliceArray(IntRange(0, size - 1))
//
//        val c1 = transformer1.apply(vanillaClassData)
//        val c2 = transformer2.apply(vanillaClassData)
//
//        println(c1.size)
//        println(c2.size)


//        stream.readBytes()

//        transformer.apply(basicShipAIStream.readBytes())


        //find $asm_root -type f -exec sed -i "s^$vanilla_path/BasicShipAI^$asm_path/AssemblyShipAI^g" {} +
        //find $asm_root -type f -exec sed -i "s^$vanilla_path/I^$asm_path/OrderResponseModule^g" {} +
        //
        //# Override Engine Controller
        //find $asm_root -type f -exec sed -i "s^$vanilla_path/movement/BasicEngineAI^$aitweaks_path/OverrideEngineAI^g" {} +
        //
        //# Override Maneuvers
        //find $asm_root -type f -exec sed -i "s^$vanilla_path/movement/maneuvers/StrafeTargetManeuverV2^$aitweaks_path/Strafe^g" {} +
        //find $asm_root -type f -exec sed -i "s^$vanilla_path/movement/maneuvers/void^$aitweaks_path/Intercept^g" {} +
        //find $asm_root -type f -exec sed -i "s^$vanilla_path/movement/maneuvers/B^$aitweaks_path/Approach^g" {} +
        //find $asm_root -type f -exec sed -i "s^$vanilla_path/movement/maneuvers/U^$aitweaks_path/Move^g" {} +


//        val basicShipAI = cl.loadClass("com.fs.starfarer.combat.ai.BasicShipAI")


//        print(basicShipAIStream.readBytes().size)

//        println(basicShipAI.isInstance(null))

    }
}

