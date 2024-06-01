package com.genir.aitweaks.features.shipai.loading

class Loader(scriptLoader: ClassLoader, private val classes: Map<String, ByteArray>) : ClassLoader(scriptLoader) {
    private val cache: MutableMap<String, Class<*>> = mutableMapOf()

    override fun loadClass(name: String): Class<*> {
        cache[name]?.let { return it }

        classes[name]?.let { classData ->
            val c = defineClass(name, classData, 0, classData.size)
            cache[name] = c
            return c
        }

        return super.loadClass(name)
    }
}