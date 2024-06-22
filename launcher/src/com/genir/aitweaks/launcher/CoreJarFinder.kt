package com.genir.aitweaks.launcher

import com.fs.starfarer.api.Global
import java.net.URL
import java.net.URLClassLoader
import kotlin.io.path.toPath

/** Find the URL of latest aitweaks-core-dev-*.jar. This is useful for
 * replacing classes without restarting the game or reloading a save,
 * with more flexibility than the usual Java debugger hot reload. */
class CoreJarFinder {
    fun findLatestCoreJar(): URL {
        // Find AI Tweaks jars directory.
        val urLs = (Global.getSettings().scriptClassLoader.parent as URLClassLoader).urLs
        val launcherURL = urLs.first { it.path.contains("aitweaks-launcher.jar") }
        val encodedURL = URL(launcherURL.toExternalForm().replace(" ", "%20"))
        val jarsPath = encodedURL.toURI().toPath().normalize().parent
        val jarsDir = jarsPath.toFile()

        // Find latest dev aitweaks-core jar, if any.
        val devJars = jarsDir.list().filter { it.contains("aitweaks-core-dev") }
        val coreJar = if (devJars.isEmpty()) "aitweaks-core.jar"
        else devJars.fold(devJars[0]) { latest, it -> if (it > latest) it else latest }
        val coreURL = URL(encodedURL.toExternalForm().replace("aitweaks-launcher.jar", coreJar))

        Global.getLogger(this::class.java).info(coreURL.toExternalForm())
        return coreURL
    }
}
