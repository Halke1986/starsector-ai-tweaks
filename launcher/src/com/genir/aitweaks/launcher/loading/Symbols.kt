package com.genir.aitweaks.launcher.loading

import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.ai.attack.AttackAIModule
import com.fs.starfarer.combat.entities.Ship
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
import java.lang.reflect.Method

class Symbols {
    private val ship: Class<*> = Ship::class.java
    private val basicShipAI: Class<*> = BasicShipAI::class.java
    private val flockingAI: Class<*> = basicShipAI.getMethod("getFlockingAI").returnType

    val approachManeuver: Class<*> = findApproachManeuver()
    val autofireManager: Class<*> = AttackAIModule::class.java.declaredFields.first { it.type.isInterface && it.type.methods.size == 1 }.type
    val maneuver: Class<*> = basicShipAI.getMethod("getCurrentManeuver").returnType
    val shipCommandWrapper: Class<*> = ship.getMethod("getCommands").genericReturnTypeArgument(0)
    val shipCommand: Class<*> = ship.getMethod("getBlockedCommands").genericReturnTypeArgument(0)
    val threatEvalAI: Class<*> = basicShipAI.getMethod("getThreatEvaluator").returnType

    val advanceAutofireManager: String = autofireManager.methods.first { it.name != "<init>" }.name
    val commandShipCommandWrapper: String = shipCommandWrapper.fields.first { it.type.isEnum }.name

    companion object {
        val Class<*>.classPath: String
            get() = this.name.replace('.', '/')
    }

    private fun Method.genericReturnTypeArgument(idx: Int): Class<*> {
        return (genericReturnType as ParameterizedTypeImpl).actualTypeArguments[idx] as Class<*>
    }

    private fun findApproachManeuver(): Class<*> {
        val aiReader = ClassReader(Transformer.readClassBuffer(this::class.java.classLoader, "com.fs.starfarer.combat.ai.BasicShipAI"))
        val maneuvers = mutableListOf<String>()

        // Find all maneuver classes used by ship AI.
        aiReader.accept(object : ClassVisitor(Opcodes.ASM4) {
            override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
                return object : MethodVisitor(Opcodes.ASM4) {
                    override fun visitTypeInsn(opcode: Int, type: String?) {
                        if (type?.startsWith("com/fs/starfarer/combat/ai/movement/maneuvers/") == true) {
                            maneuvers.add(type)
                        }
                    }
                }
            }
        }, 0)

        // Gather all possible candidate classes for approach maneuver.
        val candidates = mutableSetOf<String>()
        maneuvers.forEach { className ->
            val reader = ClassReader(Transformer.readClassBuffer(this::class.java.classLoader, className.replace("/", ".")))

            reader.accept(object : ClassVisitor(Opcodes.ASM4) {
                override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
                    if (name == "<init>" && desc?.startsWith("(L${ship.classPath};L${ship.classPath};FL${flockingAI.classPath};") == true) {
                        candidates.add(className)
                    }

                    return null
                }
            }, 0)
        }

        // Identify approach maneuver by number of methods.
        // The expected maneuver has the higher number of methods
        val candidateClasses: List<Class<*>> = candidates.map { this::class.java.classLoader.loadClass(it.replace("/", ".")) }
        return candidateClasses.maxWithOrNull { a, b -> a.declaredMethods.size - b.declaredMethods.size }!!
    }
}
