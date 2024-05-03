package maneuver

import com.genir.aitweaks.features.shipai.loading.ObfTable
import org.junit.jupiter.api.Test
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class ConstVisitor : MethodVisitor(Opcodes.ASM4) {
    override fun visitLdcInsn(cst: Any?) {
        println("${cst?.toString()}")
        super.visitLdcInsn(cst)
    }


//    override fun visitField(access: Int, name: String?, desc: String?, signature: String?, value: Any?): FieldVisitor? {
//        log("$name  $desc")
//        println("$name  $desc")
//
//        return null//super.visitField(access, name, desc, signature, value)
//    }
}

class CustomClassLoader {
    @Test
    fun testLoadClass() {
        val obf = ObfTable()

    }
}

