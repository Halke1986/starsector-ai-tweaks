package com.genir.aitweaks.features.maneuver

import com.genir.aitweaks.utils.CCT

private const val vanillaPath = "com/fs/starfarer"
private const val asmPath = "com/genir/aitweaks"

class AIClassLoader : ClassLoader() {
//    private val raws: Map<String, String> = mapOf(
//        "com.genir.aitweaks.asm.combat.ai.AssemblyShipAI" to assemblyShipAI,
//        "com.genir.aitweaks.asm.combat.ai.AssemblyShipAI\$o" to `assemblyShipAI$o`,
//        "com.genir.aitweaks.asm.combat.ai.AssemblyShipAI\$1" to `assemblyShipAI$1`,
//        "com.genir.aitweaks.asm.combat.ai.OrderResponseModule" to orderResponseModule,
//        "com.genir.aitweaks.asm.combat.ai.OrderResponseModule\$o" to `orderResponseModule$o`,
//    )

    private val sources: Map<String, String> = mapOf(
        "com.genir.aitweaks.asm.combat.ai.AssemblyShipAI" to "com.fs.starfarer.combat.ai.BasicShipAI",
        "com.genir.aitweaks.asm.combat.ai.AssemblyShipAI\$o" to "com.fs.starfarer.combat.ai.BasicShipAI\$o",
        "com.genir.aitweaks.asm.combat.ai.AssemblyShipAI\$1" to "com.fs.starfarer.combat.ai.BasicShipAI\$1",
        "com.genir.aitweaks.asm.combat.ai.OrderResponseModule" to "com.fs.starfarer.combat.ai.I",
        "com.genir.aitweaks.asm.combat.ai.OrderResponseModule\$o" to "com.fs.starfarer.combat.ai.I\$o",
    )

    private val transformer = CCT(listOf(
        CCT.newTransform("$vanillaPath/combat/ai/BasicShipAI", "$asmPath/asm/combat/ai/AssemblyShipAI"),
        CCT.newTransform("$vanillaPath/combat/ai/I", "$asmPath/asm/combat/ai/OrderResponseModule"),
        CCT.newTransform("$vanillaPath/combat/ai/movement/BasicEngineAI", "$asmPath/features/maneuver/OverrideEngineAI"),
        CCT.newTransform("$vanillaPath/combat/ai/movement/maneuvers/StrafeTargetManeuverV2", "$asmPath/features/maneuver/Strafe"),
        CCT.newTransform("$vanillaPath/combat/ai/movement/maneuvers/B", "$asmPath/features/maneuver/Approach"),
        CCT.newTransform("$vanillaPath/combat/ai/movement/maneuvers/U", "$asmPath/features/maneuver/Move"),
    ))

    private var cache: MutableMap<String, Class<*>> = mutableMapOf()

    override fun loadClass(name: String): Class<*> {
        return when {
            // Load class from cache.
            cache.containsKey(name) -> cache[name]!!

            // Load class from raw bytes and store in cache.
            sources.containsKey(name) -> {
                val c = findClass(name)
                cache[name] = c
                c
            }

            // Delegate loading to vanilla class loader.
            else -> this.javaClass.classLoader.loadClass(name)
        }
    }

    override fun findClass(name: String): Class<*> {
        val vanillaClassData = CCT.readClassBuffer(this.javaClass.classLoader, sources[name]!!)
        val classData = transformer.apply(vanillaClassData)
        return defineClass(name, classData, 0, classData.size);
    }
}

fun String.decodeHex(): ByteArray {
    return filter { !it.isWhitespace() }.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}