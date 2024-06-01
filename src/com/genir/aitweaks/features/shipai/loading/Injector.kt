package com.genir.aitweaks.features.shipai.loading

import java.io.FileOutputStream
import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Paths
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

/**
 * Injector writes the runtime-built AI Tweaks ship AI into jar file and
 * injects the jar into Starsector system class loader. This is required
 * because AI Tweaks ship AI is built on top on vanilla ship AI and contains
 * calls to obfuscated methods which have illegal names, e.g. 'String.super'.
 * Starsector Java 7 system class loader can load those methods, but class
 * loaders inheriting from it cannot.
 */
class Injector {
    fun injectClasses(scriptLoader: ClassLoader, classes: Map<String, ByteArray>) {
        val urls = (scriptLoader as URLClassLoader).urLs

        val baseJarUrl = urls.first { it.path.contains("aitweaks.jar") }
        val jarUrlStr = baseJarUrl.toExternalForm().replace("aitweaks.jar", "aitweaks-shipai.jar").replace(" ", "%20")
        val jarPath = Paths.get(URL(jarUrlStr).toURI()).normalize().toString()

        writeJar(jarPath, classes)
        injectJar(URL(jarUrlStr))
    }

    private fun writeJar(jarPath: String, classes: Map<String, ByteArray>) {
        FileOutputStream(jarPath).use { fos ->
            JarOutputStream(fos).use { jos ->

                classes.forEach {
                    val className = it.key.replace('.', '/') + ".class"
                    val classData = it.value

                    // Create a new jar entry for the class
                    val jarEntry = JarEntry(className)
                    jos.putNextEntry(jarEntry)

                    // Write the class data to the jar entry
                    jos.write(classData)

                    // Close the entry
                    jos.closeEntry()
                }
            }
        }
    }

    private fun injectJar(outputJarUrl: URL) {
        // Get the system class loader
        val systemClassLoader = ClassLoader.getSystemClassLoader() as URLClassLoader

        // Get the addURL method of URLClassLoader
        val addURLMethod: Method = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)

        // Make the method accessible
        addURLMethod.setAccessible(true)

        // Invoke the method to add the new JAR to the classpath
        addURLMethod.invoke(systemClassLoader, outputJarUrl)
    }
}