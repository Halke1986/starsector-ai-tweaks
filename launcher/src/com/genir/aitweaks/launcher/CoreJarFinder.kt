package com.genir.aitweaks.launcher

import java.io.File
import java.net.URL
import java.net.URLClassLoader
import kotlin.io.path.Path

/** Find the URL of latest aitweaks-core-dev-*.jar. This is useful for
 * replacing classes without restarting the game or reloading a save,
 * with more flexibility than the usual Java debugger hot reload. */
class CoreJarFinder {
    fun findLatestCoreJar(): URL {
        // Find AI Tweaks jars directory.
        val urLs: Array<URL> = (this::class.java.classLoader as URLClassLoader).urLs
        val launcherURL: URL = urLs.first { it.path.contains("aitweaks-launcher.jar") }
        val jarsDir: File = Path(launcherURL.path.removePrefix("/")).parent.toFile()

        // Find latest dev aitweaks-core jar, if any.
        val devJars: List<String> = jarsDir.list().filter { it.contains("aitweaks-core-dev") }
        val coreJar: String = if (devJars.isEmpty()) "aitweaks-core.jar"
        else devJars.fold(devJars[0]) { latest, it -> if (it > latest) it else latest }

        // Return the URL of AI Tweaks core jar.
        return URL(launcherURL.toString().replace("aitweaks-launcher.jar", coreJar))
    }
}
