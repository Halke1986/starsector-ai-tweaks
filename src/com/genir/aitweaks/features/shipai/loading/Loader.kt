package com.genir.aitweaks.features.shipai.loading

import com.fs.starfarer.api.Global
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.net.URLClassLoader

class Loader {
    fun loadCustomShipAI(): Class<*> {
        val scriptLoader = Global.getSettings().scriptClassLoader

        // Build custom AI classes.
        val obf = Deobfuscator(scriptLoader).getDeobfuscatedSymbols()
        val classes = Builder(scriptLoader, obf).buildClasses()

        // Inject the classes into system class loader. Injector class contains
        // reflect calls, so it cannot be loaded with the default script loader.
        val unlockedLoader = URLClassLoader((scriptLoader as URLClassLoader).urLs)
        val injectorClass = unlockedLoader.loadClass("com.genir.aitweaks.features.shipai.loading.Injector")
        val injectClassesType = MethodType.methodType(Void.TYPE, ClassLoader::class.java, Map::class.java)
        val injectClasses = MethodHandles.lookup().findVirtual(injectorClass, "injectClasses", injectClassesType)
        injectClasses.invoke(injectorClass.newInstance(), scriptLoader, classes)

        // Try to load all classes to ensure build succeeded.
        classes.forEach {
            scriptLoader.loadClass(it.key)
        }

        return scriptLoader.loadClass("com.genir.aitweaks.asm.shipai.CustomShipAI")
    }
}