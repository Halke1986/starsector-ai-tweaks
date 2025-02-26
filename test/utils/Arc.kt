package utils

import com.genir.aitweaks.core.utils.Arc
import com.genir.aitweaks.core.utils.Direction.Companion.direction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class Arc {
    @Test
    fun testMergeTrivial() {
        val actual = Arc.union(
            Arc(0f, 0f.direction),
            Arc(0f, 0f.direction),
        )
        Assertions.assertEquals(0f, actual.angle)
        Assertions.assertEquals(0f, actual.facing.degrees)
    }

    @Test
    fun testMergeFullArcs() {
        var actual = Arc.union(
            Arc(360f, 0f.direction),
            Arc(360f, 180f.direction),
        )
        Assertions.assertEquals(360f, actual.angle)
        Assertions.assertEquals(0f, actual.facing.degrees)

        actual = Arc.union(
            Arc(360f, 1f.direction),
            Arc(360f, 0f.direction),
        )
        Assertions.assertEquals(360f, actual.angle)
        Assertions.assertEquals(1f, actual.facing.degrees)
    }

    @Test
    fun testMergeLargeArcs() {
        var actual = Arc.union(
            Arc(200f, 90f.direction),
            Arc(200f, 270f.direction),
        )
        Assertions.assertEquals(360f, actual.angle)
        Assertions.assertEquals(90f, actual.facing.degrees)

        actual = Arc.union(
            Arc(300f, 0f.direction),
            Arc(70f, 180f.direction),
        )
        Assertions.assertEquals(360f, actual.angle)
        Assertions.assertEquals(0f, actual.facing.degrees)
    }

    @Test
    fun testMergeContainedArc() {
        var actual = Arc.union(
            Arc(90f, 77f.direction),
            Arc(80f, 77f.direction),
        )
        Assertions.assertEquals(90f, actual.angle)
        Assertions.assertEquals(77f, actual.facing.degrees)

        actual = Arc.union(
            Arc(10f, 90f.direction),
            Arc(300f, 10f.direction),
        )
        Assertions.assertEquals(300f, actual.angle)
        Assertions.assertEquals(10f, actual.facing.degrees)

        actual = Arc.union(
            Arc(0f, 90f.direction),
            Arc(360f, 20f.direction),
        )
        Assertions.assertEquals(360f, actual.angle)
        Assertions.assertEquals(20f, actual.facing.degrees)
    }

    @Test
    fun testMergeNonOverlappingAngles() {
        var actual = Arc.union(
            Arc(0f, 10f.direction),
            Arc(0f, 20f.direction),
        )
        Assertions.assertEquals(10f, actual.angle)
        Assertions.assertEquals(15f, actual.facing.degrees)

        actual = Arc.union(
            Arc(0f, 10f.direction),
            Arc(0f, 350f.direction),
        )
        Assertions.assertEquals(20f, actual.angle)
        Assertions.assertEquals(0f, actual.facing.degrees)

        actual = Arc.union(
            Arc(0f, 0f.direction),
            Arc(0f, 180f.direction),
        )
        Assertions.assertEquals(180f, actual.angle)
        Assertions.assertEquals(90f, actual.facing.degrees)
    }

    @Test
    fun testMergeNonOverlapping() {
        var actual = Arc.union(
            Arc(10f, 0f.direction),
            Arc(10f, 90f.direction),
        )
        Assertions.assertEquals(100f, actual.angle)
        Assertions.assertEquals(45f, actual.facing.degrees)

        actual = Arc.union(
            Arc(90f, 45f.direction),
            Arc(20f, 280f.direction),
        )
        Assertions.assertEquals(180f, actual.angle)
        Assertions.assertEquals(0f, actual.facing.degrees)

        actual = Arc.union(
            Arc(180f, 90f.direction),
            Arc(10f, 225f.direction),
        )
        Assertions.assertEquals(230f, actual.angle)
        Assertions.assertEquals(115f, actual.facing.degrees)
    }

    @Test
    fun testMergeOverlapping() {
        var actual = Arc.union(
            Arc(180f, 45f.direction),
            Arc(180f, 135f.direction),
        )
        Assertions.assertEquals(270f, actual.angle)
        Assertions.assertEquals(90f, actual.facing.degrees)

        actual = Arc.union(
            Arc(90f, 315f.direction),
            Arc(10f, 0f.direction),
        )
        Assertions.assertEquals(95f, actual.angle)
        Assertions.assertEquals(-42.5f, actual.facing.degrees)
    }

    @Test
    fun testMergeReal() {
        val arc1 = Arc(21.203f, 347.71f.direction)
        val arc2 = Arc(16.140f, 3.5998f.direction)

        val actual = Arc.union(arc1, arc2)

        Assertions.assertEquals(34.56131f, actual.angle)
    }
}
