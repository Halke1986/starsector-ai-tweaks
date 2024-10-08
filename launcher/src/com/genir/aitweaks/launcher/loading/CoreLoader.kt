package com.genir.aitweaks.launcher.loading

import com.fs.starfarer.api.Global
import com.genir.aitweaks.launcher.loading.Symbols.Companion.classPath
import java.net.URL
import java.net.URLClassLoader

class CoreLoader(coreURL: URL) : URLClassLoader(arrayOf(coreURL)) {
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
        cache[name]?.let { return it }

        var c: Class<*>?

        try {
            // Try to load the class using default Starsector script loader.
            // This ensures that AI Tweaks' core logic uses the same class
            // definitions as the rest of the application, including AI Tweaks
            // launcher. Using the same class definitions is important, when
            // sharing state through static fields, as is the case in LunaLib
            // settings.
            c = Global.getSettings().scriptClassLoader.loadClass(name)
            Global.getLogger(this::class.java).info("default    $name")
        } catch (_: SecurityException) {
            // Load classes restricted by Starsector reflect/IO ban.
            c = ClassLoader.getSystemClassLoader().loadClass(name)
            Global.getLogger(this::class.java).info("restricted $name")
        } catch (_: ClassNotFoundException) {
            // Load and transform AI Tweaks core logic classes.
            val classBuffer = Transformer.readClassBuffer(this, name)
            val obfuscated = obfuscator.apply(classBuffer)
            c = defineClass(name, obfuscated, 0, obfuscated.size)
            Global.getLogger(this::class.java).info("aitweaks   $name")
        }

        cache[name] = c!!
        return c
    }
}
