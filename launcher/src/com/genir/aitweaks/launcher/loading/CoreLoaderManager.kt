package com.genir.aitweaks.launcher.loading

import lunalib.lunaSettings.LunaSettings
import java.net.URL
import java.net.URLClassLoader

/** CoreLoaderManager contains the class loader for latest AI Tweaks core jar. */
object CoreLoaderManager {
    private val coreLoaderClass = run {
        // The bootstrap loader is used to lift reflection and file access restrictions
        // and to load the ASM library dynamically. The ASM library could be declared
        // statically in mod_info.json, but doing so may cause conflicts with other mods
        // that depend on different ASM versions.
        val urLs: Array<URL> = (this::class.java.classLoader as URLClassLoader).urLs
        val launcherURL = urLs.first { it.path.contains("aitweaks-launcher.jar") }
        val asmURL = URL(launcherURL.toString().replace("aitweaks-launcher.jar", "asm-9.1.jar"))
        val bootstrapLoader = URLClassLoader(urLs + asmURL)

        bootstrapLoader.loadClass("com.genir.aitweaks.launcher.loading.CoreLoader")
    }

    var coreLoader: URLClassLoader = coreLoaderClass.instantiate()

    /** Update AI Tweaks core loader to point at the latest core jar. */
    fun updateLoader() {
        if (LunaSettings.getBoolean("aitweaks", "aitweaks_enable_devmode") != true) {
            return
        }

        val newLoader: URLClassLoader = coreLoaderClass.instantiate()
        if (!coreLoader.urLs.contentEquals(newLoader.urLs)) {
            coreLoader = newLoader
        }
    }

    fun <T> Class<*>.instantiate(): T {
        return newInstance() as T
    }
}
