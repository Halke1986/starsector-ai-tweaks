package com.genir.aitweaks.core.features.shipai

import com.fs.starfarer.combat.ai.BasicShipAI
import com.fs.starfarer.combat.ai.attack.AttackAIModule
import com.genir.aitweaks.core.features.shipai.vanilla.Obfuscated
import com.genir.aitweaks.core.utils.extensions.classPath
import com.genir.aitweaks.core.utils.loading.ByteClassLoader
import com.genir.aitweaks.core.utils.loading.Bytecode
import org.lwjgl.util.vector.Vector2f
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.lang.reflect.Field

class AutofireManager : Obfuscated.AutofireManagerInterface {
    override fun advance(p0: Float, p1: Obfuscated.ThreatEvalAI, p2: Vector2f?) {
    }

    companion object {
        private var obfuscated: Class<*>? = null

        /** Replace vanilla autofire manager with AI Tweaks implementation. */
        fun inject(attackModule: AttackAIModule) {
            val f = findAutofireManagerField().also { it.setAccessible(true) }
            if (f.get(attackModule)::class.java.isInstance(getObfuscatedClass())) return

            f.set(attackModule, getObfuscatedClass().newInstance())
        }

        /** Find obfuscated AttackAIModule.autofireManager field. */
        private fun findAutofireManagerField(): Field {
            val fields: Array<Field> = AttackAIModule::class.java.declaredFields
            return fields.first { it.type.isInterface && it.type.methods.size == 1 }
        }

        private fun getObfuscatedClass(): Class<*> {
            obfuscated?.let { return it }

            val cl = ByteClassLoader(this::class.java.classLoader)
            cl.addClass(AutofireManager::class.java.name, obfuscate())
            obfuscated = cl.loadClass(AutofireManager::class.java.name)

            return obfuscated!!
        }

        private fun obfuscate(): ByteArray {
            // Find vanilla types.
            val obfAutofireManager: Class<*> = findAutofireManagerField().type
            val obfThreatEvalAI: Class<*> = BasicShipAI::class.java.getMethod("getThreatEvaluator").returnType

            // Build obfuscated autofire manager.
            return Bytecode.transformClass(AutofireManager::class.java) {
                object : ClassVisitor(Opcodes.ASM4, it) {

                    // Replace implemented interface.
                    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
                        val obfInterface = arrayOf(obfAutofireManager.classPath)

                        super.visit(version, access, name, signature, superName, obfInterface)
                    }

                    // Replace advance method name and description.
                    override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
                        if (name != "advance") return super.visitMethod(access, name, desc, signature, exceptions)

                        val obfName = obfAutofireManager.methods.first().name
                        val obfDesc = desc.replace(Obfuscated.ThreatEvalAI::class.java.classPath, obfThreatEvalAI.classPath)

                        return super.visitMethod(access, obfName, obfDesc, signature, exceptions)
                    }
                }
            }
        }
    }
}
