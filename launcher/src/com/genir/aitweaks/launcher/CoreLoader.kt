package com.genir.aitweaks.launcher

import com.fs.starfarer.api.Global
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.net.URL
import java.net.URLClassLoader

var coreLoader: ClassLoader = Global.getSettings().scriptClassLoader

fun newCoreLoader(): ClassLoader {
    // Class loader to circumvent Starsector file access restrictions.
    val urLs = (Global.getSettings().scriptClassLoader.parent as URLClassLoader).urLs
    val cl = URLClassLoader(urLs)

    val finderClass = cl.loadClass("com.genir.aitweaks.launcher.CoreJarFinder")
    val findJarType = MethodType.methodType(URL::class.java)
    val findJar = MethodHandles.lookup().findVirtual(finderClass, "findLatestCoreJar", findJarType)
    val coreURL = findJar.invoke(finderClass.newInstance()) as URL

    val allURLs = urLs.clone().toMutableList()
    allURLs.add(coreURL)

    return URLClassLoader(allURLs.toTypedArray())
}

class AitLoaderGetter {
    fun getAitLoader(): ClassLoader = coreLoader
}
