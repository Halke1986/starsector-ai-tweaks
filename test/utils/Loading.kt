package utils

import com.genir.aitweaks.core.features.shipai.vanilla.Deobfuscator
import com.genir.aitweaks.core.features.shipai.vanilla.Vanilla
import org.junit.jupiter.api.Test

class Loading {
    @Test
    fun testLoading() {

//        println(Vanilla::class.java.`package`.name)
//        println(Vanilla::class.java.name)

        Deobfuscator.getReader(Vanilla::class.java)

    }
}