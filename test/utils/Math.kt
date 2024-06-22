package utils

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
}