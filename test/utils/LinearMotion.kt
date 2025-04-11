package utils

import com.genir.aitweaks.core.utils.types.LinearMotion
import com.genir.aitweaks.core.utils.types.LinearMotion.Companion.intersection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.lwjgl.util.vector.Vector2f

class TestLinearMotion {
    @Test
    fun testIntersection() {
        assertEquals(
            Pair(0.5f, 0.5f),
            intersection(
                LinearMotion(Vector2f(0f, -1f), Vector2f(0f, 2f)),
                LinearMotion(Vector2f(-1f, 0f), Vector2f(2f, 0f)),
            ),
        )

        assertEquals(
            Pair(-0.5f, 0.25f),
            intersection(
                LinearMotion(Vector2f(0f, 1f), Vector2f(0f, 2f)),
                LinearMotion(Vector2f(-1f, 0f), Vector2f(4f, 0f)),
            ),
        )

        assertEquals(
            Pair(1f, 1f),
            intersection(
                LinearMotion(Vector2f(0f, 1f), Vector2f(0f, -1f)),
                LinearMotion(Vector2f(1f, 1f), Vector2f(-1f, -1f)),
            ),
        )
    }
}
