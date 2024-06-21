package com.genir.aitweaks.features.shipai.loading

class Builder {
    fun buildCustomShipAI(): Class<*> {
        // Compile custom AI classes.
        val aitLoader = Builder::class.java.classLoader
        val obf = Deobfuscator(aitLoader).getDeobfuscatedSymbols()
        val classes = Compiler(aitLoader, obf).compileClasses()

        try {
            // First, try a custom class loader. Should work when -noverify
            // option is defined in vmparams or when obfuscator didn't generate
            // illegal method names.
            val customLoader = Loader(aitLoader, classes)

            classes.forEach { customLoader.loadClass(it.key) } // Test
            return customLoader.loadClass("com.genir.aitweaks.asm.shipai.CustomShipAI")
        } catch (e: Throwable) {
            Injector().injectClasses(aitLoader, classes)

            classes.forEach { aitLoader.loadClass(it.key) } // Test
            return aitLoader.loadClass("com.genir.aitweaks.asm.shipai.CustomShipAI")
        }
    }
}