package utils

import com.genir.aitweaks.utils.RADIANS_TO_DEGREES
import com.genir.aitweaks.utils.quad
import com.genir.aitweaks.utils.vectorProjection
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.lwjgl.util.vector.Vector2f
import kotlin.math.PI
import kotlin.math.abs

class Math {
    @Test
    fun testVectorProjection() {
        Assertions.assertEquals(Vector2f(0f, 1f), vectorProjection(Vector2f(0f, 1f), Vector2f(0f, 2f)))
        Assertions.assertEquals(Vector2f(0f, 0f), vectorProjection(Vector2f(1f, 0f), Vector2f(0f, 2f)))
        Assertions.assertEquals(Vector2f(0f, 0f), vectorProjection(Vector2f(0f, 0f), Vector2f(0f, 2f)))
        Assertions.assertEquals(Vector2f(0f, 1f), vectorProjection(Vector2f(0f, 1f), Vector2f(0f, 0.5f)))
        Assertions.assertEquals(Vector2f(0.5f, 0.5f), vectorProjection(Vector2f(0f, 1f), Vector2f(1f, 1f)))
        Assertions.assertEquals(Vector2f(0f, 1f), vectorProjection(Vector2f(1f, 1f), Vector2f(0f, 1f)))
    }

    @Test
    fun testSQRT() {
        val e = 140f / 60f
        val v = 40f / 60f

        facingOffset(e, v)

//        val a = 1f
//        val b = -5f * (e / v)
//        val c = 5f
//
//        val t = quad(a, b, c)!!.second
//
//        val x = t - PI / 2f
//
//        val facingOffset = x * RADIANS_TO_DEGREES

        println(facingOffset(e, v))

    }

    @Test
    fun testRet(): Unit {
        val l = listOf(1, 2, 3, 4, 5)

        l.forEach {
            if (it > 3) return@forEach


            println(it)
        }
    }

    fun facingOffset(expected: Float, max: Float): Float {
        val a = 1f
        val b = -5f * (expected / max)
        val c = 5f

        val t = quad(a, b, c)!!.second
        return abs(t - PI / 2f).toFloat() * RADIANS_TO_DEGREES
    }
}