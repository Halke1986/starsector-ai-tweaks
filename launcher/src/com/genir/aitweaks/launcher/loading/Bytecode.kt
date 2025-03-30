package com.genir.aitweaks.launcher.loading

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/** Tools for obfuscated bytecode analysis. */
object Bytecode {
    data class Method(val name: String, val desc: String)

    /** Read method names in same order as defined in class bytecode. */
    fun getMethodsInOrder(c: Class<*>): List<Method> {
        val methods: MutableList<Method> = mutableListOf()

        ClassReader(readClassBuffer(c.classLoader, c.name)).accept(object : ClassVisitor(Opcodes.ASM7) {
            override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
                methods.add(Method(name, desc))
                return null
            }
        }, 0)

        return methods
    }

    val Class<*>.classPath: String
        get() = this.name.replace('.', '/')

    fun readClassBuffer(cl: ClassLoader, className: String): ByteArray {
        val classPath = className.replace('.', '/') + ".class"
        val stream = cl.getResourceAsStream(classPath)

        var size = 0
        var buffer = ByteArray(1024)
        while (stream.available() > 0) {
            size += stream.read(buffer, size, buffer.size - size)
            if (size == buffer.size) {
                buffer += ByteArray(buffer.size)
            }
        }

        val classData = buffer.sliceArray(IntRange(0, size - 1))

        return classData
    }
}
