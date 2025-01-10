package com.genir.aitweaks.launcher.loading

import lunalib.lunaSettings.LunaSettings
import java.net.URLClassLoader

/** CoreLoaderManager contains the class loader for latest AI Tweaks core jar. */
object CoreLoaderManager {
    private val bootstrapLoader = URLClassLoader((this::class.java.classLoader as URLClassLoader).urLs)
    private val coreLoaderClass = bootstrapLoader.loadClass("com.genir.aitweaks.launcher.loading.CoreLoader")
    var coreLoader = coreLoaderClass.newInstance() as URLClassLoader

    /** Update AI Tweaks core loader to point at the latest core jar. */
    fun updateLoader() {
        if (LunaSettings.getBoolean("aitweaks", "aitweaks_enable_devmode") != true) {
            return
        }

        val newLoader = coreLoaderClass.newInstance() as URLClassLoader
        if (!coreLoader.urLs.contentEquals(newLoader.urLs)) {
            coreLoader = newLoader
        }
    }

    fun <T> Class<*>.instantiate(): T {
        return newInstance() as T
    }
}
