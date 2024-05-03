package com.genir.aitweaks.features.shipai.loading

import com.genir.aitweaks.utils.CCT
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes

class ObfTable {
    var basicShipAINested1 = ""
    var basicShipAINested2 = ""

    var orderResponseModule = ""
    var orderResponseModuleNested = ""

    init {
        analyzeBasicShipAI()
        analyzeOrderResponseModule()
    }

    private fun analyzeBasicShipAI() {
        val className = "com.fs.starfarer.combat.ai.BasicShipAI"
        val reader = getReader(className)
        val innerClasses: MutableSet<String> = mutableSetOf()

        reader.accept(object : ClassVisitor(Opcodes.ASM4) {
            override fun visitField(access: Int, name: String?, desc: String?, signature: String?, value: Any?): FieldVisitor? {
                if (name == "orderResponseModule") orderResponseModule = getSimpleName(desc!!)
                return null
            }

            override fun visitInnerClass(name: String?, outerName: String?, innerName: String?, access: Int) {
                if (name?.startsWith(className.replace(".", "/")) == true)
                    innerClasses.add(getSimpleName(name))
            }
        }, 0)

        basicShipAINested1 = innerClasses.toList()[0]
        basicShipAINested2 = innerClasses.toList()[1]
    }

    private fun analyzeOrderResponseModule() {
        val className = "com.fs.starfarer.combat.ai.$orderResponseModule"
        val reader = getReader(className)

        reader.accept(object : ClassVisitor(Opcodes.ASM4) {
            override fun visitInnerClass(name: String?, outerName: String?, innerName: String?, access: Int) {
                if (name?.startsWith(className.replace(".", "/")) == true)
                    orderResponseModuleNested = getSimpleName(name)
            }
        }, 0)

        return
    }

    private fun getSimpleName(descriptor: String) = descriptor.split("/").last().split("$").last().removeSuffix(";")

    private fun getReader(name: String): ClassReader {
        val classData = CCT.readClassBuffer(this.javaClass.classLoader, name)
        return ClassReader(classData)
    }
}