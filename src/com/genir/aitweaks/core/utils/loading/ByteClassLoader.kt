package com.genir.aitweaks.core.utils.loading

class ByteClassLoader(parent: ClassLoader) : ClassLoader(parent) {
    private val classes: MutableMap<String, Class<*>> = mutableMapOf()

    override fun loadClass(name: String): Class<*> {
        classes[name]?.let { return it }

        return super.loadClass(name)
    }

    fun addClass(name: String, classData: ByteArray) {
        classes[name] = defineClass(name, classData, 0, classData.size)
    }
}
