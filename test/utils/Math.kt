package utils

import com.genir.aitweaks.core.utils.Direction.Companion.direction
import com.genir.aitweaks.core.utils.PI
import com.genir.aitweaks.core.utils.atan
import com.genir.aitweaks.core.utils.vectorProjection
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.lwjgl.util.vector.Vector2f
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
    fun testDirection() {
        Assertions.assertEquals(0f, 0f.direction.degrees)
        Assertions.assertEquals(0f, (-0f).direction.degrees)
        Assertions.assertEquals(180f, 180f.direction.degrees)
        Assertions.assertEquals(-180f, (-180f).direction.degrees)
        Assertions.assertEquals(10f, 10f.direction.degrees)
        Assertions.assertEquals(-10f, (-10f).direction.degrees)
        Assertions.assertEquals(-90f, 270f.direction.degrees)
        Assertions.assertEquals(90f, (-270f).direction.degrees)
        Assertions.assertEquals(-10f, 350f.direction.degrees)
        Assertions.assertEquals(10f, (-350f).direction.degrees)
        Assertions.assertEquals(0f, 360f.direction.degrees)
        Assertions.assertEquals(0f, (-360f).direction.degrees)
        Assertions.assertEquals(-80f, 1000f.direction.degrees)
        Assertions.assertEquals(80f, (-1000f).direction.degrees)
    }

    @Test
    fun testAtan() {
        for (i in -1000..1000) {
            if (i == 0) continue

            val z = i / 500.0f
            val err = atan(z) - kotlin.math.atan(z)

            Assertions.assertTrue(abs(err) < 1E-4)
        }
    }

    fun benchAtan() {
        val iterations = 100000000
        var value: Float = PI / 4

        // Warm-up
        for (i in 0 until iterations) {
            value += 0.001f
            atan(value)
        }

        value = PI / 4
        var sum = 0.0
        val start = System.nanoTime()
        for (i in 0 until iterations) {
            value += 0.001f
            sum += atan(value)
        }
        val elapsedTime = System.nanoTime() - start
        println("atan() took: " + (elapsedTime / iterations) + " ns per operation")
    }
}
