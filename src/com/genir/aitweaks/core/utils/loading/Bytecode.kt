package com.genir.aitweaks.core.utils.loading

import org.objectweb.asm.*

/** Tools for obfuscated bytecode analysis. */
object Bytecode {
    data class Method(val name: String, val desc: String)

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

    fun transformClass(c: Class<*>, transformerFactory: (ClassVisitor) -> ClassVisitor): ByteArray {
        val reader = ClassReader(readClassBuffer(c))
        val writer = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        val transformer = transformerFactory(writer)

        reader.accept(transformer, 0)
        val bytes = writer.toByteArray()

        return bytes
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
