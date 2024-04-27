package com.genir.aitweaks.features.maneuver

import com.genir.aitweaks.features.maneuver.raw.*

class AIClassLoader : ClassLoader() {
    private val raws: Map<String, String> = mapOf(
        "com.genir.aitweaks.asm.combat.ai.AssemblyShipAI" to assemblyShipAI,
        "com.genir.aitweaks.asm.combat.ai.AssemblyShipAI\$o" to `assemblyShipAI$o`,
        "com.genir.aitweaks.asm.combat.ai.AssemblyShipAI\$1" to `assemblyShipAI$1`,
        "com.genir.aitweaks.asm.combat.ai.OrderResponseModule" to orderResponseModule,
        "com.genir.aitweaks.asm.combat.ai.OrderResponseModule\$o" to `orderResponseModule$o`,
    )

    private var cache: MutableMap<String, Class<*>> = mutableMapOf()

    override fun loadClass(name: String): Class<*> {
        return when {
            // Load class from cache.
            cache.containsKey(name) -> cache[name]!!

            // Load class from raw bytes and store in cache.
            raws.containsKey(name) -> {
                val c = findClass(name)
                cache[name] = c
                c
            }

            // Delegate loading to vanilla class loader.
            else -> this.javaClass.classLoader.loadClass(name)
        }
    }

    override fun findClass(name: String): Class<*> {
        val classData = raws[name]!!.decodeHex()
        return defineClass(name, classData, 0, classData.size);
    }
}

fun String.decodeHex(): ByteArray {
    return filter { !it.isWhitespace() }.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}