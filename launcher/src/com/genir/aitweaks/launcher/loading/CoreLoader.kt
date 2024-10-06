package com.genir.aitweaks.launcher.loading

import com.fs.starfarer.api.Global
import java.net.URL
import java.net.URLClassLoader

class CoreLoader(urLs: Array<URL>) : URLClassLoader(urLs) {
    private val cache: MutableMap<String, Class<*>> = mutableMapOf()

    private val obfuscator = Transformer(listOf(
        Transformer.newTransform("com/genir/aitweaks/core/Obfuscated\$AutofireManager", "com/fs/starfarer/combat/ai/attack/D"),
        Transformer.newTransform("com/genir/aitweaks/core/Obfuscated\$ThreatEvalAI", "com/fs/starfarer/combat/ai/O0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO"),

        Transformer.newTransform("advance_AutofireManager", "o00000"),
        ))

    override fun loadClass(name: String): Class<*> {
        when {
            !name.startsWith("com.genir.aitweaks.core") -> return super.loadClass(name)

            name.startsWith("com.genir.aitweaks.core.Obfuscated") -> return super.loadClass(name)
        }

        cache[name]?.let { return it }

        val classBuffer = Transformer.readClassBuffer(this, name)
        val obfuscated = obfuscator.apply(classBuffer)

        val c = defineClass(name, obfuscated, 0, obfuscated.size)
        cache[name] = c

        Global.getLogger(this::class.java).info(name)

        return c
    }
}


//        private fun getObfuscatedClass(): Class<*> {
//            obfuscated?.let { return it }
//
//            val cl = ByteClassLoader(this::class.java.classLoader)
//            cl.addClass(AutofireManagerAdapter::class.java.name, obfuscate())
//            obfuscated = cl.loadClass(AutofireManagerAdapter::class.java.name)
//
//            return obfuscated!!
//        }
//
//        private fun obfuscate(): ByteArray {
//            // Find vanilla types.
//            val obfAutofireManager: Class<*> = findAutofireManagerField().type
//            val obfThreatEvalAI: Class<*> = BasicShipAI::class.java.getMethod("getThreatEvaluator").returnType
//
//            // Build obfuscated autofire manager.
//            return Bytecode.transformClass(AutofireManagerAdapter::class.java) {
//                object : ClassVisitor(Opcodes.ASM4, it) {
//
//                    // Replace implemented interface.
//                    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
//                        val obfInterface = arrayOf(obfAutofireManager.classPath)
//
//                        super.visit(version, access, name, signature, superName, obfInterface)
//                    }
//
//                    // Replace advance method name and description.
//                    override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
//                        if (name != "advance") return super.visitMethod(access, name, desc, signature, exceptions)
//
//                        val obfName = obfAutofireManager.methods.first().name
//                        val obfDesc = desc.replace(Obfuscated.ThreatEvalAI::class.java.classPath, obfThreatEvalAI.classPath)
//
//                        return super.visitMethod(access, obfName, obfDesc, signature, exceptions)
//                    }
//                }
//            }
//        }