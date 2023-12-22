package utils

import com.genir.aitweaks.utils.solve
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.Float.Companion.NaN

internal class MathKtTest {

    @Test
    fun testSolve() {
        assertEquals(NaN, solve(Vector2f(0f, 0f), Vector2f(0f, 0f), 0f, 0f))
        assertEquals(
            1.2075576f, solve(
                Vector2f(0.41844496f, -0.53832567f), Vector2f(0.7005532f, -0.25651577f), 0.21049917f, 1.086479f
            )
        )

//
//        val p = Vector2f(0.41844496f, -0.53832567f)
//        val dp = Vector2f(0.7005532f, -0.25651577f)
//        val r = 0.21049917f
//        val dr = 1.086479f
//
//        val actual = intersectionTime(p, dp, r, dr)
//
//        val left = r + dr * actual!!
//        val right = (p + dp.times_(actual!!)).length()
//
//        assertEquals(1.2075576f, actual)
//        assertEquals(left, right)
    }

//    @Test
//    fun testIntersection2() {
//        val p = Vector2f(0.41844496f, -0.53832567f)
//        val dp = Vector2f(0.7005532f, -0.25651577f)
//        val r = 0.21049917f
//        val dr = 1.086479f
//
//        val actual = intersectionTime(p, dp, r, dr)
//
//        val left = r + dr * actual!!
//        val right = (p + dp.times_(actual!!)).length()
//
//        assertEquals(1.2075576f, actual)
//        assertEquals(left, right)
//    }
}