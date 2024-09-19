package utils

import com.genir.aitweaks.core.utils.Arc
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class Arc {
    @Test
    fun testMergeArc() {
        val arc1 = Arc(21.203f, 347.71f)
        val arc2 = Arc(16.140f, 3.5998f)

        arc1.append(arc2)

        Assertions.assertEquals(34.561287f, arc1.arc)
    }
}