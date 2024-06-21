package com.genir.aitweaks.launcher

import com.fs.starfarer.api.Global
import java.net.URL
import java.net.URLClassLoader

var reloader = newReloader()

fun newReloader(): ClassLoader {
    val parentScriptLoader = Global.getSettings().scriptClassLoader.parent as URLClassLoader
    val urLs = parentScriptLoader.urLs

    val launcherURL = urLs.first { it.path.contains("aitweaks-launcher.jar") }
    val contentURL = launcherURL.toExternalForm().replace("aitweaks-launcher.jar", "aitweaks.jar").replace(" ", "%20")

    val allURLs = urLs.clone().toMutableList()
    allURLs.add(URL(contentURL))

    return URLClassLoader(allURLs.toTypedArray())
}

class AitLoaderGetter {
    fun getAitLoader(): ClassLoader = reloader
}
