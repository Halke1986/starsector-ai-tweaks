package com.genir.aitweaks.launcher

import com.fs.starfarer.api.Global
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.net.URL
import java.net.URLClassLoader

var coreLoader: ClassLoader = Global.getSettings().scriptClassLoader
private var coreURL: URL? = null

class CoreLoaderManager {
    fun reload() {
        // Class loader to circumvent Starsector file access restrictions.
        val urLs = (Global.getSettings().scriptClassLoader.parent as URLClassLoader).urLs
        val cl = URLClassLoader(urLs)

        // Find latest AI Tweaks core jar.
        val finderClass = cl.loadClass("com.genir.aitweaks.launcher.CoreJarFinder")
        val findJarType = MethodType.methodType(URL::class.java)
        val findJar = MethodHandles.lookup().findVirtual(finderClass, "findLatestCoreJar", findJarType)
        val latestCoreURL = findJar.invoke(finderClass.newInstance()) as URL

        // Core jar was not replaced.
        if (coreURL?.sameFile(latestCoreURL) == true) return

        val allURLs = urLs.clone().toMutableList()
        allURLs.add(latestCoreURL)

        Global.getLogger(this::class.java).info(latestCoreURL.toExternalForm())
        coreLoader = URLClassLoader(allURLs.toTypedArray())
        coreURL = latestCoreURL
    }

    fun get(): ClassLoader = coreLoader
}
