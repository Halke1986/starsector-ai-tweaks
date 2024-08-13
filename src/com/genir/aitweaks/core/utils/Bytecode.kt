package com.genir.aitweaks.core.utils

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/** Tools for obfuscated bytecode analysis. */
class Bytecode() {
    data class Method(val name: String, val desc: String)

    companion object {

        /** Read method names in same order as defined in class bytecode. */
        fun getMethodsInOrder(c: Class<*>): List<Method> {
            val methods: MutableList<Method> = mutableListOf()

            ClassReader(readClassBuffer(c)).accept(object : ClassVisitor(Opcodes.ASM4) {
                override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
                    methods.add(Method(name, desc))
                    return null
                }
            }, 0)

            return methods
        }

        private fun readClassBuffer(c: Class<*>): ByteArray {
            val classPath = c.canonicalName.replace('.', '/') + ".class"
            val stream = c.classLoader.getResourceAsStream(classPath)

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
}
