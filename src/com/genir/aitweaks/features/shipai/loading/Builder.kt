package com.genir.aitweaks.features.shipai.loading

class Builder(private val scriptLoader: ClassLoader, private val obf: Deobfuscator.Symbols) {
    fun buildClasses(): Map<String, ByteArray> {
        return buildVanillaClasses() + buildAdapterClasses()
    }

    private fun buildVanillaClasses(): Map<String, ByteArray> {
        val inner = obf.basicShipAIInnerClasses
        val vanillaSources: Map<String, String> = mapOf(
            "com.genir.aitweaks.asm.shipai.CustomShipAI" to "com.fs.starfarer.combat.ai.BasicShipAI",
            "com.genir.aitweaks.asm.shipai.CustomShipAI$${inner[0]}" to "com.fs.starfarer.combat.ai.BasicShipAI$${inner[0]}",
            "com.genir.aitweaks.asm.shipai.CustomShipAI$${inner[1]}" to "com.fs.starfarer.combat.ai.BasicShipAI$${inner[1]}",
            "com.genir.aitweaks.asm.shipai.OrderResponseModule" to "com.fs.starfarer.combat.ai.${obf.orderResponseModule}",
            "com.genir.aitweaks.asm.shipai.OrderResponseModule$${obf.orderResponseModuleInner}" to "com.fs.starfarer.combat.ai.${obf.orderResponseModule}$${obf.orderResponseModuleInner}",
        )

        val vanillaTransformer = Transformer(listOf(
            // Rename vanilla classes.
            Transformer.newTransform("com/fs/starfarer/combat/ai/BasicShipAI", "com/genir/aitweaks/asm/shipai/CustomShipAI"),
            Transformer.newTransform("com/fs/starfarer/combat/ai/${obf.orderResponseModule}", "com/genir/aitweaks/asm/shipai/OrderResponseModule"),

            // Replace vanilla maneuvers.
            Transformer.newTransform("com/fs/starfarer/combat/ai/movement/BasicEngineAI", "com/genir/aitweaks/asm/shipai/EngineAIAdapter"),
            Transformer.newTransform("com/fs/starfarer/combat/ai/movement/maneuvers/StrafeTargetManeuverV2", "com/genir/aitweaks/asm/shipai/Strafe"),
            Transformer.newTransform("com/fs/starfarer/combat/ai/movement/maneuvers/${obf.approachManeuver}", "com/genir/aitweaks/asm/shipai/Approach"),
            Transformer.newTransform("com/fs/starfarer/combat/ai/movement/maneuvers/${obf.moveManeuver}", "com/genir/aitweaks/asm/shipai/Move"),
        ))

        return vanillaSources.mapValues { build(it.value, vanillaTransformer) }
    }

    private fun buildAdapterClasses(): Map<String, ByteArray> {
        val adapterSources: Map<String, String> = mapOf(
            "com.genir.aitweaks.asm.shipai.Strafe" to "com.genir.aitweaks.features.shipai.adapters.Strafe",
            "com.genir.aitweaks.asm.shipai.Approach" to "com.genir.aitweaks.features.shipai.adapters.Approach",
            "com.genir.aitweaks.asm.shipai.Move" to "com.genir.aitweaks.features.shipai.adapters.Move",
            "com.genir.aitweaks.asm.shipai.ManeuverAdapter" to "com.genir.aitweaks.features.shipai.adapters.ManeuverAdapter",
            "com.genir.aitweaks.asm.shipai.EngineAIAdapter" to "com.genir.aitweaks.features.shipai.adapters.EngineAIAdapter",
        )

        val adapterTransformer = Transformer(listOf(
            // Rename aitweaks classes.
            Transformer.newTransform("com/genir/aitweaks/features/shipai/adapters/Strafe", "com/genir/aitweaks/asm/shipai/Strafe"),
            Transformer.newTransform("com/genir/aitweaks/features/shipai/adapters/Approach", "com/genir/aitweaks/asm/shipai/Approach"),
            Transformer.newTransform("com/genir/aitweaks/features/shipai/adapters/Move", "com/genir/aitweaks/asm/shipai/Move"),
            Transformer.newTransform("com/genir/aitweaks/features/shipai/adapters/ManeuverAdapter", "com/genir/aitweaks/asm/shipai/ManeuverAdapter"),
            Transformer.newTransform("com/genir/aitweaks/features/shipai/adapters/EngineAIAdapter", "com/genir/aitweaks/asm/shipai/EngineAIAdapter"),

            // Replace stub types.
            Transformer.newTransform("com/genir/aitweaks/features/shipai/adapters/ManeuverInterface", "com/fs/starfarer/combat/ai/movement/maneuvers/${obf.maneuverInterface}"),
            Transformer.newTransform("com/genir/aitweaks/features/shipai/adapters/FlockingAI", "com/fs/starfarer/combat/ai/movement/${obf.flockingAIModule}"),
            Transformer.newTransform("com/genir/aitweaks/features/shipai/adapters/ShipAI", "com/fs/starfarer/combat/ai/movement/maneuvers/${obf.shipAIInterface}"),
            Transformer.newTransform("com/fs/starfarer/api/combat/CombatEntityAPI", "com/fs/starfarer/combat/${obf.combatEntityPackage}/${obf.combatEntityInterface}"),

            // Replace method names.
            Transformer.newTransform("advanceObf", obf.advance),
            Transformer.newTransform("getTargetObf", obf.getTarget),
            Transformer.newTransform("isDirectControlObf", obf.isDirectControl),
            Transformer.newTransform("doManeuverObf", obf.doManeuver),
            Transformer.newTransform("getDesiredHeadingObf", obf.getDesiredHeading),
            Transformer.newTransform("getDesiredFacingObf", obf.getDesiredFacing),
            Transformer.newTransform("getDesiredStrafeHeadingObf", obf.getDesiredStrafeHeading),
        ))

        return adapterSources.mapValues { build(it.value, adapterTransformer) }
    }

    private fun build(sourceName: String, transformer: Transformer): ByteArray {
        // Do two transform passes to replace multiple types contained in the same constant.
        val classBuffer = Transformer.readClassBuffer(scriptLoader, sourceName)
        return transformer.apply(transformer.apply(classBuffer))
    }
}