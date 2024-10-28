package utils

import com.genir.aitweaks.core.utils.Arc
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class Arc {
    @Test
    fun testMergeTrivial() {
        val actual = Arc.merge(
            Arc(0f, 0f),
            Arc(0f, 0f),
        )
        Assertions.assertEquals(0f, actual.angle)
        Assertions.assertEquals(0f, actual.facing)
    }

    @Test
    fun testMergeFullArcs() {
        var actual = Arc.merge(
            Arc(360f, 0f),
            Arc(360f, 180f),
        )
        Assertions.assertEquals(360f, actual.angle)
        Assertions.assertEquals(0f, actual.facing)

        actual = Arc.merge(
            Arc(360f, 1f),
            Arc(360f, 0f),
        )
        Assertions.assertEquals(360f, actual.angle)
        Assertions.assertEquals(1f, actual.facing)
    }

    @Test
    fun testMergeLargeArcs() {
        var actual = Arc.merge(
            Arc(200f, 90f),
            Arc(200f, 270f),
        )
        Assertions.assertEquals(360f, actual.angle)
        Assertions.assertEquals(90f, actual.facing)

        actual = Arc.merge(
            Arc(300f, 0f),
            Arc(70f, 180f),
        )
        Assertions.assertEquals(360f, actual.angle)
        Assertions.assertEquals(0f, actual.facing)
    }

    @Test
    fun testMergeContainedArc() {
        var actual = Arc.merge(
            Arc(90f, 77f),
            Arc(80f, 77f),
        )
        Assertions.assertEquals(90f, actual.angle)
        Assertions.assertEquals(77f, actual.facing)

        actual = Arc.merge(
            Arc(10f, 90f),
            Arc(300f, 10f),
        )
        Assertions.assertEquals(300f, actual.angle)
        Assertions.assertEquals(10f, actual.facing)

        actual = Arc.merge(
            Arc(0f, 90f),
            Arc(360f, 20f),
        )
        Assertions.assertEquals(360f, actual.angle)
        Assertions.assertEquals(20f, actual.facing)
    }

    @Test
    fun testMergeNonOverlappingAngles() {
        var actual = Arc.merge(
            Arc(0f, 10f),
            Arc(0f, 20f),
        )
        Assertions.assertEquals(10f, actual.angle)
        Assertions.assertEquals(15f, actual.facing)

        actual = Arc.merge(
            Arc(0f, 10f),
            Arc(0f, 350f),
        )
        Assertions.assertEquals(20f, actual.angle)
        Assertions.assertEquals(0f, actual.facing)

        actual = Arc.merge(
            Arc(0f, 0f),
            Arc(0f, 180f),
        )
        Assertions.assertEquals(180f, actual.angle)
        Assertions.assertEquals(90f, actual.facing)
    }

    @Test
    fun testMergeNonOverlapping() {
        var actual = Arc.merge(
            Arc(10f, 0f),
            Arc(10f, 90f),
        )
        Assertions.assertEquals(100f, actual.angle)
        Assertions.assertEquals(45f, actual.facing)

        actual = Arc.merge(
            Arc(90f, 45f),
            Arc(20f, 280f),
        )
        Assertions.assertEquals(180f, actual.angle)
        Assertions.assertEquals(0f, actual.facing)

        actual = Arc.merge(
            Arc(180f, 90f),
            Arc(10f, 225f),
        )
        Assertions.assertEquals(230f, actual.angle)
        Assertions.assertEquals(115f, actual.facing)
    }

    @Test
    fun testMergeOverlapping() {
        var actual = Arc.merge(
            Arc(180f, 45f),
            Arc(180f, 135f),
        )
        Assertions.assertEquals(270f, actual.angle)
        Assertions.assertEquals(90f, actual.facing)

        actual = Arc.merge(
            Arc(90f, 315f),
            Arc(10f, 0f),
        )
        Assertions.assertEquals(95f, actual.angle)
        Assertions.assertEquals(317.5f, actual.facing)
    }

    @Test
    fun testMergeReal() {
        val arc1 = Arc(21.203f, 347.71f)
        val arc2 = Arc(16.140f, 3.5998f)

        val actual = Arc.merge(arc1, arc2)

        Assertions.assertEquals(34.561302f, actual.angle)
    }
}
