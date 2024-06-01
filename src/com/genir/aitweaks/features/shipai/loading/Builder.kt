package com.genir.aitweaks.features.shipai.loading

import com.fs.starfarer.api.Global
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.net.URLClassLoader

private var customShipAIClass: Class<*>? = null

class Builder {
    companion object {
        fun getCustomShipAI(): Class<*> {
            customShipAIClass?.let { return it }

            // Compile custom AI classes.
            val scriptURLLoader = Global.getSettings().scriptClassLoader.parent
            val obf = Deobfuscator(scriptURLLoader).getDeobfuscatedSymbols()
            val classes = Compiler(scriptURLLoader, obf).compileClasses()

            try {
                // First, try a custom class loader. Should work when -noverify
                // option is defined in vmparams or when obfuscator didn't generate
                // illegal method names.
                val customLoader = Loader(scriptURLLoader, classes)

                classes.forEach { customLoader.loadClass(it.key) } // Test
                customShipAIClass = customLoader.loadClass("com.genir.aitweaks.asm.shipai.CustomShipAI")
            } catch (e: Throwable) {
                // Inject the classes into system class loader. That way methods with
                // illegal names can be loaded. Injector class contains reflect calls,
                // so it cannot be loaded with the default script loader.
                val unlockedLoader = URLClassLoader((scriptURLLoader as URLClassLoader).urLs)
                val injectorClass = unlockedLoader.loadClass("com.genir.aitweaks.features.shipai.loading.Injector")
                val injectClassesType = MethodType.methodType(Void.TYPE, ClassLoader::class.java, Map::class.java)
                val injectClasses = MethodHandles.lookup().findVirtual(injectorClass, "injectClasses", injectClassesType)
                injectClasses.invoke(injectorClass.newInstance(), scriptURLLoader, classes)

                classes.forEach { scriptURLLoader.loadClass(it.key) } // Test
                customShipAIClass = scriptURLLoader.loadClass("com.genir.aitweaks.asm.shipai.CustomShipAI")
            }

            return customShipAIClass!!
        }
    }
}