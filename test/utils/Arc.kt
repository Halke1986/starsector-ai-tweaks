package utils

import com.genir.aitweaks.core.utils.Arc
import com.genir.aitweaks.core.utils.Rotation
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class Arc {
    @Test
    fun testMergeTrivial() {
        val actual = Arc.union(
            Arc(0f, Rotation(0f)),
            Arc(0f, Rotation(0f)),
        )
        Assertions.assertEquals(0f, actual.angle)
        Assertions.assertEquals(0f, actual.facing)
    }

    @Test
    fun testMergeFullArcs() {
        var actual = Arc.union(
            Arc(360f, Rotation(0f)),
            Arc(360f, Rotation(180f)),
        )
        Assertions.assertEquals(360f, actual.angle)
        Assertions.assertEquals(0f, actual.facing)

        actual = Arc.union(
            Arc(360f, Rotation(1f)),
            Arc(360f, Rotation(0f)),
        )
        Assertions.assertEquals(360f, actual.angle)
        Assertions.assertEquals(1f, actual.facing)
    }

    @Test
    fun testMergeLargeArcs() {
        var actual = Arc.union(
            Arc(200f, Rotation(90f)),
            Arc(200f, Rotation(270f)),
        )
        Assertions.assertEquals(360f, actual.angle)
        Assertions.assertEquals(90f, actual.facing)

        actual = Arc.union(
            Arc(300f, Rotation(0f)),
            Arc(70f, Rotation(180f)),
        )
        Assertions.assertEquals(360f, actual.angle)
        Assertions.assertEquals(0f, actual.facing)
    }

    @Test
    fun testMergeContainedArc() {
        var actual = Arc.union(
            Arc(90f, Rotation(77f)),
            Arc(80f, Rotation(77f)),
        )
        Assertions.assertEquals(90f, actual.angle)
        Assertions.assertEquals(77f, actual.facing)

        actual = Arc.union(
            Arc(10f, Rotation(90f)),
            Arc(300f, Rotation(10f)),
        )
        Assertions.assertEquals(300f, actual.angle)
        Assertions.assertEquals(10f, actual.facing)

        actual = Arc.union(
            Arc(0f, Rotation(90f)),
            Arc(360f, Rotation(20f)),
        )
        Assertions.assertEquals(360f, actual.angle)
        Assertions.assertEquals(20f, actual.facing)
    }

    @Test
    fun testMergeNonOverlappingAngles() {
        var actual = Arc.union(
            Arc(0f, Rotation(10f)),
            Arc(0f, Rotation(20f)),
        )
        Assertions.assertEquals(10f, actual.angle)
        Assertions.assertEquals(15f, actual.facing)

        actual = Arc.union(
            Arc(0f, Rotation(10f)),
            Arc(0f, Rotation(350f)),
        )
        Assertions.assertEquals(20f, actual.angle)
        Assertions.assertEquals(0f, actual.facing)

        actual = Arc.union(
            Arc(0f, Rotation(0f)),
            Arc(0f, Rotation(180f)),
        )
        Assertions.assertEquals(180f, actual.angle)
        Assertions.assertEquals(90f, actual.facing)
    }

    @Test
    fun testMergeNonOverlapping() {
        var actual = Arc.union(
            Arc(10f, Rotation(0f)),
            Arc(10f, Rotation(90f)),
        )
        Assertions.assertEquals(100f, actual.angle)
        Assertions.assertEquals(45f, actual.facing)

        actual = Arc.union(
            Arc(90f, Rotation(45f)),
            Arc(20f, Rotation(280f)),
        )
        Assertions.assertEquals(180f, actual.angle)
        Assertions.assertEquals(0f, actual.facing)

        actual = Arc.union(
            Arc(180f, Rotation(90f)),
            Arc(10f, Rotation(225f)),
        )
        Assertions.assertEquals(230f, actual.angle)
        Assertions.assertEquals(115f, actual.facing)
    }

    @Test
    fun testMergeOverlapping() {
        var actual = Arc.union(
            Arc(180f, Rotation(45f)),
            Arc(180f, Rotation(135f)),
        )
        Assertions.assertEquals(270f, actual.angle)
        Assertions.assertEquals(90f, actual.facing)

        actual = Arc.union(
            Arc(90f, Rotation(315f)),
            Arc(10f, Rotation(0f)),
        )
        Assertions.assertEquals(95f, actual.angle)
        Assertions.assertEquals(317.5f, actual.facing)
    }

    @Test
    fun testMergeReal() {
        val arc1 = Arc(21.203f, Rotation(347.71f))
        val arc2 = Arc(16.140f, Rotation(3.5998f))

        val actual = Arc.union(arc1, arc2)

        Assertions.assertEquals(34.561302f, actual.angle)
    }
}
