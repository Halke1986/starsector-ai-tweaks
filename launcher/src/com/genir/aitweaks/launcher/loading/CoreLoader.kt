package com.genir.aitweaks.launcher.loading

import com.fs.starfarer.api.Global
import com.genir.aitweaks.launcher.loading.Symbols.Companion.classPath
import java.net.URL
import java.net.URLClassLoader

class CoreLoader(urLs: Array<URL>) : URLClassLoader(urLs) {
    private val cache: MutableMap<String, Class<*>> = mutableMapOf()
    private val symbols = Symbols()
    private val obfuscator = Transformer(listOf(
        Transformer.newTransform("com/genir/aitweaks/core/Obfuscated\$ApproachManeuver", symbols.approachManeuver.classPath),
        Transformer.newTransform("com/genir/aitweaks/core/Obfuscated\$AutofireManager", symbols.autofireManager.classPath),
        Transformer.newTransform("com/genir/aitweaks/core/Obfuscated\$BasicShipAI", "com/fs/starfarer/combat/ai/BasicShipAI"),
        Transformer.newTransform("com/genir/aitweaks/core/Obfuscated\$Maneuver", symbols.maneuver.classPath),
        Transformer.newTransform("com/genir/aitweaks/core/Obfuscated\$ShipCommandWrapper", symbols.shipCommandWrapper.classPath),
        Transformer.newTransform("com/genir/aitweaks/core/Obfuscated\$ShipCommand", symbols.shipCommand.classPath),
        Transformer.newTransform("com/genir/aitweaks/core/Obfuscated\$Ship", "com/fs/starfarer/combat/entities/Ship"),
        Transformer.newTransform("com/genir/aitweaks/core/Obfuscated\$ThreatEvalAI", symbols.threatEvalAI.classPath),

        Transformer.newTransform("advance_AutofireManager", symbols.advanceAutofireManager),
        Transformer.newTransform("command_ShipCommandWrapper", symbols.commandShipCommandWrapper),
    ))

    override fun loadClass(name: String): Class<*> {
        when {
            !name.startsWith("com.genir.aitweaks.core") -> return super.loadClass(name)

            // Don't modify Obfuscated classes - they will not be used anyway.
            name.startsWith("com.genir.aitweaks.core.Obfuscated") -> return super.loadClass(name)
        }

        cache[name]?.let { return it }

        val classBuffer = Transformer.readClassBuffer(this, name)
        val obfuscated = obfuscator.apply(classBuffer)

        val c = defineClass(name, obfuscated, 0, obfuscated.size)
        cache[name] = c

        Global.getLogger(this::class.java).info(name)

        return c
    }
}
