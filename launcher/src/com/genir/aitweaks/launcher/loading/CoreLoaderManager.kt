package com.genir.aitweaks.launcher.loading

import lunalib.lunaSettings.LunaSettings
import java.net.URL
import java.net.URLClassLoader

/** CoreLoaderManager contains the class loader for latest AI Tweaks core jar. */
object CoreLoaderManager {
    var coreLoader: URLClassLoader = BootstrapLoader().loadClass("com.genir.aitweaks.launcher.loading.CoreLoader").instantiate()

    /** Update AI Tweaks core loader to point at the latest core jar. */
    fun updateLoader() {
        if (LunaSettings.getBoolean("aitweaks", "aitweaks_enable_devmode") != true) {
            return
        }

        val newLoader: URLClassLoader = coreLoader::class.java.instantiate()
        if (!coreLoader.urLs.contentEquals(newLoader.urLs)) {
            coreLoader = newLoader
        }
    }

    fun <T> Class<*>.instantiate(): T {
        return newInstance() as T
    }

    /** The bootstrap loader is used to lift reflection and file access restrictions
     * and to load the ASM library dynamically. The ASM library could be declared
     * statically in mod_info.json, but doing so may cause conflicts with other mods
     * that depend on different ASM versions. */
    private class BootstrapLoader() : URLClassLoader((this::class.java.classLoader as URLClassLoader).urLs) {
        private val asmLoader = run {
            val launcherURL = super.getURLs().first { it.path.contains("aitweaks-launcher.jar") }
            val asmURL = URL(launcherURL.toString().replace("aitweaks-launcher.jar", "asm-9.1.jar"))

            return@run URLClassLoader(arrayOf(asmURL))
        }

        override fun loadClass(name: String?, resolve: Boolean): Class<*> {
            // Try loading AI Tweaks specific ASM version.
            // This avoids loading ASM added by other mods.
            try {
                return asmLoader.loadClass(name)
            } catch (_: ClassNotFoundException) {
            }

            return super.loadClass(name, resolve)
        }
    }
}
