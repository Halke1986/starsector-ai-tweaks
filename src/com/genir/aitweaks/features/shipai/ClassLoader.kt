package com.genir.aitweaks.features.shipai

import com.genir.aitweaks.utils.CCT

private const val vanillaPath = "com/fs/starfarer"
private const val aitPath = "com/genir/aitweaks"

class AIClassLoader : ClassLoader() {
    private val vanillaSources: Map<String, String> = mapOf(
        "com.genir.aitweaks.asm.shipai.AssemblyShipAI" to "com.fs.starfarer.combat.ai.BasicShipAI",
        "com.genir.aitweaks.asm.shipai.AssemblyShipAI\$o" to "com.fs.starfarer.combat.ai.BasicShipAI\$o",
        "com.genir.aitweaks.asm.shipai.AssemblyShipAI\$1" to "com.fs.starfarer.combat.ai.BasicShipAI\$1",
        "com.genir.aitweaks.asm.shipai.OrderResponseModule" to "com.fs.starfarer.combat.ai.I",
        "com.genir.aitweaks.asm.shipai.OrderResponseModule\$o" to "com.fs.starfarer.combat.ai.I\$o"
    )

    private val adapterSources: Map<String, String> = mapOf(
        "com.genir.aitweaks.asm.shipai.Strafe" to "com.genir.aitweaks.features.shipai.adapters.Strafe",
        "com.genir.aitweaks.asm.shipai.Approach" to "com.genir.aitweaks.features.shipai.adapters.Approach",
        "com.genir.aitweaks.asm.shipai.Move" to "com.genir.aitweaks.features.shipai.adapters.Move",
        "com.genir.aitweaks.asm.shipai.ManeuverAdapter" to "com.genir.aitweaks.features.shipai.adapters.ManeuverAdapter",
    )

    private val vanillaTransformer = CCT(listOf(
        // Rename vanilla classes.
        CCT.newTransform("$vanillaPath/combat/ai/BasicShipAI", "$aitPath/asm/shipai/AssemblyShipAI"),
        CCT.newTransform("$vanillaPath/combat/ai/I", "$aitPath/asm/shipai/OrderResponseModule"),

        CCT.newTransform("$vanillaPath/combat/ai/movement/BasicEngineAI", "$aitPath/features/shipai/adapters/EngineAIAdapter"),

        // Replace vanilla maneuvers.
        CCT.newTransform("$vanillaPath/combat/ai/movement/maneuvers/StrafeTargetManeuverV2", "$aitPath/asm/shipai/Strafe"),
        CCT.newTransform("$vanillaPath/combat/ai/movement/maneuvers/B", "$aitPath/asm/shipai/Approach"),
        CCT.newTransform("$vanillaPath/combat/ai/movement/maneuvers/U", "$aitPath/asm/shipai/Move"),
    ))

    private val adapterTransformer = CCT(listOf(
        // Rename aitweaks classes.
        CCT.newTransform("$aitPath/features/shipai/adapters/Strafe", "$aitPath/asm/shipai/Strafe"),
        CCT.newTransform("$aitPath/features/shipai/adapters/Approach", "$aitPath/asm/shipai/Approach"),
        CCT.newTransform("$aitPath/features/shipai/adapters/Move", "$aitPath/asm/shipai/Move"),
        CCT.newTransform("$aitPath/features/shipai/adapters/ManeuverAdapter", "$aitPath/asm/shipai/ManeuverAdapter"),

        // Replace stub types.
        CCT.newTransform("$aitPath/features/shipai/adapters/ManeuverInterface", "$vanillaPath/combat/ai/movement/maneuvers/oO0O"),
        CCT.newTransform("$aitPath/features/shipai/adapters/FlockingAI", "$vanillaPath/combat/ai/movement/oOOO"),
        CCT.newTransform("$aitPath/features/shipai/adapters/ShipAI", "$vanillaPath/combat/ai/movement/maneuvers/oO0O\$o"),
        CCT.newTransform("$vanillaPath/api/combat/CombatEntityAPI", "$vanillaPath/combat/o0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO/B"),

        // Replace method names.
        CCT.newTransform("advanceObf", "o00000"),
        CCT.newTransform("getTargetObf", "o00000"),
        CCT.newTransform("isDirectControlObf", "Õ00000"),
        CCT.newTransform("doManeuverObf", "Object"),
        CCT.newTransform("getDesiredHeadingObf", "Ô00000"),
        CCT.newTransform("getDesiredFacingObf", "Ò00000"),
        CCT.newTransform("getDesiredStrafeHeadingObf", "Object"),
    ))

    private var cache: MutableMap<String, Class<*>> = mutableMapOf()

    override fun loadClass(name: String): Class<*> {
        return when {
            // Load class from cache.
            cache.containsKey(name) -> cache[name]!!

            // Load class from raw bytes and store in cache.
            vanillaSources.containsKey(name) || adapterSources.containsKey(name) -> {
                val c = findClass(name)
                cache[name] = c
                c
            }

            // Delegate loading to vanilla class loader.
            else -> this.javaClass.classLoader.loadClass(name)
        }
    }

    override fun findClass(name: String): Class<*> {
        val (source, transformer) = if (vanillaSources.containsKey(name)) {
            Pair(vanillaSources[name]!!, vanillaTransformer)
        } else {
            Pair(adapterSources[name]!!, adapterTransformer)
        }

        // Do two transform passes to replace multiple types contained in the same constant.
        val classBuffer = CCT.readClassBuffer(this.javaClass.classLoader, source)
        val classData = transformer.apply(transformer.apply(classBuffer))
        return defineClass(name, classData, 0, classData.size);
    }
}
