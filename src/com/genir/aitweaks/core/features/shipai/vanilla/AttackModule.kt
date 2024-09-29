package com.genir.aitweaks.core.features.shipai.vanilla

import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.ai.attack.AttackAIModule
import com.genir.aitweaks.core.features.shipai.vanilla.stubs.StubThreatEvalAI
import com.genir.aitweaks.core.utils.loading.ByteClassLoader
import com.genir.aitweaks.core.utils.loading.Bytecode
import com.genir.aitweaks.core.utils.loading.Bytecode.classPath
import org.lwjgl.util.vector.Vector2f
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Field

class AttackModule(basicShipAI: BasicShipAI, threatEvalAI: ThreatEvalAI) {
    private val attackModule: AttackAIModule = basicShipAI.attackAI
    private val advance: MethodHandle

    init {
        // Find advance method.
        val methods = attackModule::class.java.methods
        val advanceParams = arrayOf(Float::class.java, threatEvalAI.threatEvalAI::class.java, Vector2f::class.java)
        val advance = methods.first { it.parameterTypes.contentEquals(advanceParams) }
        this.advance = MethodHandles.lookup().unreflect(advance)

        // Find autofire manager field.
        val fields: Array<Field> = attackModule::class.java.declaredFields
        val autofireManagerField: Field = fields.first { it.type.isInterface && it.type.methods.size == 1 }

        // Build non-functional autofire manager.
        val autofireManagerBytes = Bytecode.transformClass(AutofireManager::class.java) {
            object : ClassVisitor(Opcodes.ASM4, it) {

                // Replace implemented interface
                override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
                    val obfInterface = arrayOf(autofireManagerField.type.canonicalName.replace(".", "/"))

                    super.visit(version, access, name, signature, superName, obfInterface)
                }

                // Replace advance method name and description.
                override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
                    if (name != "advance")
                        return super.visitMethod(access, name, desc, signature, exceptions)

                    val obfName = autofireManagerField.type.methods.first().name
                    val obfDesc = desc.replace(StubThreatEvalAI::class.java.classPath, threatEvalAI.threatEvalAI::class.java.classPath)

                    return super.visitMethod(access, obfName, obfDesc, signature, exceptions)
                }
            }
        }

        val cl = ByteClassLoader(this::class.java.classLoader)
        cl.addClass(AutofireManager::class.java.canonicalName, autofireManagerBytes)
        val autofireManagerClass = cl.loadClass(AutofireManager::class.java.canonicalName)

        autofireManagerField.setAccessible(true)
        autofireManagerField.set(attackModule, autofireManagerClass.newInstance())
    }

    fun advance(dt: Float, threatEvalAI: ThreatEvalAI, missileDangerDir: Vector2f?) {
        advance.invoke(attackModule, dt, threatEvalAI.threatEvalAI, missileDangerDir)
    }
}
