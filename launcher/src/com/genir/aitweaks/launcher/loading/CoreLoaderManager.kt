package com.genir.aitweaks.launcher.loading

import java.io.File
import java.net.URL
import java.net.URLClassLoader
import kotlin.io.path.Path

/** CoreLoaderManager finds AI Tweaks core logic jar
 * and returns ClassLoader that can load the logic. */
class CoreLoaderManager {
    private var coreURL: URL? = null
    private var coreLoader: ClassLoader? = null

    fun getCoreLoader(): ClassLoader {
        val currentCoreURL = latestCoreURL()

        if (coreURL?.sameFile(currentCoreURL) != true) {
            coreLoader = CoreLoader(currentCoreURL)
            coreURL = currentCoreURL
        }

        return coreLoader!!
    }

    /** AI Tweaks launcher jar URL. */
    private fun launcherURL(): URL {
        val urLs: Array<URL> = (this::class.java.classLoader as URLClassLoader).urLs
        return urLs.first { it.path.contains("aitweaks-launcher.jar") }
    }

    /** Find the URL of latest aitweaks-core-dev-*.jar. This is useful for
     * replacing classes without restarting the game or reloading a save,
     * with more flexibility than the usual Java debugger hot reload. */
    private fun latestCoreURL(): URL {
        // Find all aitweaks-core dev jars.
        val jarsDir: File = Path(launcherURL().path.removePrefix("/")).parent.toFile()
        val devJars: List<String>? = jarsDir.list()?.filter { it.contains("aitweaks-core-dev") }

        // Find the latest dev aitweaks-core jar, if any.
        val coreJar: String = if (devJars?.isNotEmpty() != true) "aitweaks-core.jar"
        else devJars.fold(devJars[0]) { latest, it -> if (it > latest) it else latest }

        // Return the URL of AI Tweaks core jar.
        return URL(launcherURL().toString().replace("aitweaks-launcher.jar", coreJar))
    }
}
