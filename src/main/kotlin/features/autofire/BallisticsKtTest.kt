package features.autofire

import com.genir.aitweaks.features.autofire.willHitBounds
import com.genir.aitweaks.utils.mocks.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.lwjgl.util.vector.Vector2f

class BallisticsKtTest {
    @Test
    fun testWillHitBounds2() {
        val bounds = MockBoundsAPI(
            "getOrigSegments" to listOf(
                MockSegmentAPI(Vector2f(4.0f, 13.0f), Vector2f(3.333332f, -5.666666f)),
                MockSegmentAPI(Vector2f(3.333332f, -5.666666f), Vector2f(0.6940994f, -3.4000683f)),
                MockSegmentAPI(Vector2f(0.6940994f, -3.4000683f), Vector2f(2.5799294f, 2.8077145f)),
                MockSegmentAPI(Vector2f(2.5799294f, 2.8077145f), Vector2f(-1.5446529f, 3.7806206f)),
                MockSegmentAPI(Vector2f(-1.5446529f, 3.7806206f), Vector2f(-2.002161f, 7.9474297f)),
                MockSegmentAPI(Vector2f(-2.002161f, 7.9474297f), Vector2f(-7.5947266f, 7.6417694f)),
                MockSegmentAPI(Vector2f(-7.5947266f, 7.6417694f), Vector2f(-12.8621025f, 7.619339f)),
                MockSegmentAPI(Vector2f(-12.8621025f, 7.619339f), Vector2f(-12.500004f, 12.5f)),
                MockSegmentAPI(Vector2f(-12.500004f, 12.5f), Vector2f(4.0f, 13.0f)),
            ), "getSegments" to listOf(
                MockSegmentAPI(Vector2f(1664.6791f, -462.06982f), Vector2f(1653.2975f, -447.25946f)),
                MockSegmentAPI(Vector2f(1653.2975f, -447.25946f), Vector2f(1656.7758f, -447.32437f)),
                MockSegmentAPI(Vector2f(1656.7758f, -447.32437f), Vector2f(1659.2784f, -453.31018f)),
                MockSegmentAPI(Vector2f(1659.2784f, -453.31018f), Vector2f(1663.077f, -451.43152f)),
                MockSegmentAPI(Vector2f(1663.077f, -451.43152f), Vector2f(1666.085f, -454.35114f)),
                MockSegmentAPI(Vector2f(1666.085f, -454.35114f), Vector2f(1670.2001f, -450.5517f)),
                MockSegmentAPI(Vector2f(1670.2001f, -450.5517f), Vector2f(1674.2451f, -447.17773f)),
                MockSegmentAPI(Vector2f(1674.2451f, -447.17773f), Vector2f(1677.0763f, -451.16977f)), // THIS
                MockSegmentAPI(Vector2f(1677.0763f, -451.16977f), Vector2f(1664.6791f, -462.06982f)),
            )
        )

        val weapon = MockWeaponAPI(
            "getLocation" to Vector2f(2011.343f, -1750.4396f),
            "getCurrAngle" to 104.474915f,
            "getProjectileSpeed" to 3.4028236E36f,
            "getShip" to MockShipAPI("getVelocity" to Vector2f(2.2171297f, 65.96275f))
        )

        val target = MockCombatEntityAPI(
            "getLocation" to Vector2f(1659.4774f, -449.50232f),
            "getVelocity" to Vector2f(52.902084f, -105.472f),
            "getCollisionRadius" to 35.755074f,
            "getFacing" to 219.58746f,
            "getExactBounds" to bounds,
        )

        val actual = willHitBounds(weapon, target)
        Assertions.assertEquals(1341.0204f, actual)
    }
}