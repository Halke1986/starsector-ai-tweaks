package com.genir.aitweaks.features.shipai.loading

import com.fs.starfarer.api.Global
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.net.URLClassLoader

class Loader {
    companion object {
        fun loadCustomShipAI(): Class<*> {
            try {
                return load()
            } catch (e: Exception) {
                buildCustomShipAI()
                return load()
            }
        }

        private fun load(): Class<*> {
            return Global.getSettings().scriptClassLoader.loadClass("com.genir.aitweaks.asm.shipai.CustomShipAI")
        }

        private fun buildCustomShipAI() {
            val scriptURLLoader = Global.getSettings().scriptClassLoader.parent

            // Build custom AI classes.
            val obf = Deobfuscator(scriptURLLoader).getDeobfuscatedSymbols()
            val classes = Builder(scriptURLLoader, obf).buildClasses()

            // Inject the classes into system class loader. Injector class contains
            // reflect calls, so it cannot be loaded with the default script loader.
            val unlockedLoader = URLClassLoader((scriptURLLoader as URLClassLoader).urLs)
            val injectorClass = unlockedLoader.loadClass("com.genir.aitweaks.features.shipai.loading.Injector")
            val injectClassesType = MethodType.methodType(Void.TYPE, ClassLoader::class.java, Map::class.java)
            val injectClasses = MethodHandles.lookup().findVirtual(injectorClass, "injectClasses", injectClassesType)
            injectClasses.invoke(injectorClass.newInstance(), scriptURLLoader, classes)

            // Try to load all classes to ensure build succeeded.
            classes.forEach {
                scriptURLLoader.loadClass(it.key)
            }
        }
    }
}