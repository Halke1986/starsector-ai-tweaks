package com.genir.aitweaks.launcher

import com.fs.starfarer.api.Global
import lunalib.lunaSettings.LunaSettings
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.net.URL
import java.net.URLClassLoader

var coreLoader: ClassLoader = Global.getSettings().scriptClassLoader
private var coreURL: URL? = null

class CoreLoaderManager {
    private val jarUrls = (this::class.java.classLoader as URLClassLoader).urLs

    fun reload() {
        val devmode = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_devmode") == true

        if (!devmode) {
            coreURL ?: run { setNewClassLoader(defaultJarURL()) }
        } else {
            // Class loader to circumvent Starsector file access restrictions.
            val cl = URLClassLoader(jarUrls)

            // Find latest AI Tweaks dev core jar.
            val finderClass = cl.loadClass("com.genir.aitweaks.launcher.CoreJarFinder")
            val findJarType = MethodType.methodType(URL::class.java)
            val findJar = MethodHandles.lookup().findVirtual(finderClass, "findLatestCoreJar", findJarType)
            val latestCoreURL = findJar.invoke(finderClass.newInstance()) as URL

            // Core jar was not replaced.
            if (coreURL?.sameFile(latestCoreURL) == true) return

            setNewClassLoader(latestCoreURL)
        }
    }

    private fun defaultJarURL(): URL {
        val baseJarUrl = jarUrls.first { it.path.contains("aitweaks-launcher.jar") }
        val jarUrlStr = baseJarUrl.toExternalForm().replace("aitweaks-launcher.jar", "aitweaks-core.jar")
        return URL(jarUrlStr.replace(" ", "%20"))
    }

    private fun setNewClassLoader(coreJarURL: URL) {
        Global.getLogger(this::class.java).info(coreJarURL.toExternalForm())
        coreLoader = URLClassLoader(arrayOf(*jarUrls, coreJarURL))
        coreURL = coreJarURL
    }

    fun get(): ClassLoader = coreLoader
}
