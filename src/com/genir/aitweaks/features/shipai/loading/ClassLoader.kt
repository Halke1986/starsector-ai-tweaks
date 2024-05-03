package com.genir.aitweaks.features.shipai.loading

import com.genir.aitweaks.features.shipai.loading.ClassConstantTransformer as CCT

class AIClassLoader : ClassLoader() {
    private val obf = ObfTable()

    private val vanillaSources: Map<String, String> = mapOf(
        "com.genir.aitweaks.asm.shipai.AssemblyShipAI" to "com.fs.starfarer.combat.ai.BasicShipAI",
        "com.genir.aitweaks.asm.shipai.OrderResponseModule" to "com.fs.starfarer.combat.ai.${obf.orderResponseModule}",
    )

    private val adapterSources: Map<String, String> = mapOf(
        "com.genir.aitweaks.asm.shipai.Strafe" to "com.genir.aitweaks.features.shipai.adapters.Strafe",
        "com.genir.aitweaks.asm.shipai.Approach" to "com.genir.aitweaks.features.shipai.adapters.Approach",
        "com.genir.aitweaks.asm.shipai.Move" to "com.genir.aitweaks.features.shipai.adapters.Move",
        "com.genir.aitweaks.asm.shipai.ManeuverAdapter" to "com.genir.aitweaks.features.shipai.adapters.ManeuverAdapter",
    )

    private val vanillaTransformer = CCT(listOf(
        // Rename vanilla classes.
        CCT.newTransform("com/fs/starfarer/combat/ai/BasicShipAI", "com/genir/aitweaks/asm/shipai/AssemblyShipAI"),
        CCT.newTransform("com/fs/starfarer/combat/ai/${obf.orderResponseModule}", "com/genir/aitweaks/asm/shipai/OrderResponseModule"),

        // Replace vanilla maneuvers.
        CCT.newTransform("com/fs/starfarer/combat/ai/movement/BasicEngineAI", "com/genir/aitweaks/features/shipai/adapters/EngineAIAdapter"),
        CCT.newTransform("com/fs/starfarer/combat/ai/movement/maneuvers/StrafeTargetManeuverV2", "com/genir/aitweaks/asm/shipai/Strafe"),
        CCT.newTransform("com/fs/starfarer/combat/ai/movement/maneuvers/${obf.approach}", "com/genir/aitweaks/asm/shipai/Approach"),
        CCT.newTransform("com/fs/starfarer/combat/ai/movement/maneuvers/${obf.move}", "com/genir/aitweaks/asm/shipai/Move"),
    ))

    private val adapterTransformer = CCT(listOf(
        // Rename aitweaks classes.
        CCT.newTransform("com/genir/aitweaks/features/shipai/adapters/Strafe", "com/genir/aitweaks/asm/shipai/Strafe"),
        CCT.newTransform("com/genir/aitweaks/features/shipai/adapters/Approach", "com/genir/aitweaks/asm/shipai/Approach"),
        CCT.newTransform("com/genir/aitweaks/features/shipai/adapters/Move", "com/genir/aitweaks/asm/shipai/Move"),
        CCT.newTransform("com/genir/aitweaks/features/shipai/adapters/ManeuverAdapter", "com/genir/aitweaks/asm/shipai/ManeuverAdapter"),

        // Replace stub types.
        CCT.newTransform("com/genir/aitweaks/features/shipai/adapters/ManeuverInterface", "com/fs/starfarer/combat/ai/movement/maneuvers/${obf.maneuver}"),
        CCT.newTransform("com/genir/aitweaks/features/shipai/adapters/FlockingAI", "com/fs/starfarer/combat/ai/movement/${obf.flockingAI}"),
        CCT.newTransform("com/genir/aitweaks/features/shipai/adapters/ShipAI", "com/fs/starfarer/combat/ai/movement/maneuvers/${obf.shipAI}"),
        CCT.newTransform("com/fs/starfarer/api/combat/CombatEntityAPI", "com/fs/starfarer/combat/${obf.combatEntityPackage}/${obf.combatEntity}"),

        // Replace method names.
        CCT.newTransform("advanceObf", obf.advance),
        CCT.newTransform("getTargetObf", obf.getTarget),
        CCT.newTransform("isDirectControlObf", obf.isDirectControl),
        CCT.newTransform("doManeuverObf", obf.doManeuver),
        CCT.newTransform("getDesiredHeadingObf", obf.getDesiredHeading),
        CCT.newTransform("getDesiredFacingObf", obf.getDesiredFacing),
        CCT.newTransform("getDesiredStrafeHeadingObf", obf.getDesiredStrafeHeading),
    ))

    private var cache: MutableMap<String, Class<*>> = mutableMapOf()

    override fun loadClass(name: String): Class<*> {
        val outerName = name.split("$").first()

        return when {
            // Load class from cache.
            cache.containsKey(name) -> cache[name]!!

            // Load class from raw bytes and store in cache.
            vanillaSources.containsKey(outerName) || adapterSources.containsKey(outerName) -> {
                val c = findClass(name)
                cache[name] = c
                c
            }

            // Delegate loading to vanilla class loader.
            else -> this.javaClass.classLoader.loadClass(name)
        }
    }

    override fun findClass(name: String): Class<*> {
        val elems = name.split("$")
        val outerName = elems.first()
        val innerName = if (elems.size > 1) "$${elems[1]}" else ""

        val (source, transformer) = if (vanillaSources.containsKey(outerName)) {
            Pair(vanillaSources[outerName]!!, vanillaTransformer)
        } else {
            Pair(adapterSources[outerName]!!, adapterTransformer)
        }

        // Do two transform passes to replace multiple types contained in the same constant.
        val classBuffer = CCT.readClassBuffer(this.javaClass.classLoader, source + innerName)
        val classData = transformer.apply(transformer.apply(classBuffer))
        return defineClass(name, classData, 0, classData.size);
    }
}
