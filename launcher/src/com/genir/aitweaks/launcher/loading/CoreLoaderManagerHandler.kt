package com.genir.aitweaks.launcher.loading

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.net.URL
import java.net.URLClassLoader

/** CoreLoaderManager uses reflection and file access, both of which are restricted by Starsector.
 * CoreLoaderManagerHandler bypasses those restrictions by loading CoreLoaderManager using a custom
 * class loader and invoking it through method handles. */
class CoreLoaderManagerHandler {
    private val manager: Any
    private val getCoreLoader: MethodHandle

    init {
        val urLs: Array<URL> = (this::class.java.classLoader as URLClassLoader).urLs
        val managerLoader: ClassLoader = URLClassLoader(urLs)

        val managerClass = managerLoader.loadClass("com.genir.aitweaks.launcher.loading.CoreLoaderManager")
        val getCoreLoaderType = MethodType.methodType(ClassLoader::class.java)

        getCoreLoader = MethodHandles.lookup().findVirtual(managerClass, "getCoreLoader", getCoreLoaderType)
        manager = managerClass.newInstance()
    }

    fun getCoreLoader(): ClassLoader {
        return getCoreLoader.invoke(manager) as ClassLoader
    }
}
