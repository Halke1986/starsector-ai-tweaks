package utils

import com.genir.aitweaks.core.utils.atan
import com.genir.aitweaks.core.utils.clampAngle
import com.genir.aitweaks.core.utils.vectorProjection
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.lwjgl.util.vector.Vector2f

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
    fun testAtan() {
        for (i in -1000..1000) {
            if (i == 0) continue

            val z = i / 1000.0
            val err = atan(z) / kotlin.math.atan(z)

            Assertions.assertTrue(err > 0.9978)
            Assertions.assertTrue(err < 1.0018)
        }
    }

    @Test
    fun testClampAngle() {
        Assertions.assertEquals(0f, clampAngle(0f))
        Assertions.assertEquals(10f, clampAngle(10f))
        Assertions.assertEquals(350f, clampAngle(-10f))
        Assertions.assertEquals(270f, clampAngle(270f))
        Assertions.assertEquals(90f, clampAngle(-270f))
        Assertions.assertEquals(350f, clampAngle(350f))
        Assertions.assertEquals(10f, clampAngle(-350f))
        Assertions.assertEquals(0f, clampAngle(360f))
        Assertions.assertEquals(0f, clampAngle(-360f))
        Assertions.assertEquals(280f, clampAngle(1000f))
        Assertions.assertEquals(80f, clampAngle(-1000f))
    }
}
