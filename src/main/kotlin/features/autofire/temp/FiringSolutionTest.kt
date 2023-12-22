package features.autofire.temp

import com.genir.aitweaks.features.autofire.temp.FiringSolution
import org.junit.jupiter.api.Test
import com.genir.aitweaks.mocks.MockCombatEntityAPI
import com.genir.aitweaks.mocks.MockShipAPI
import com.genir.aitweaks.mocks.MockWeaponAPI
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.lwjgl.util.vector.Vector2f

internal class FiringSolutionTest {

    @Test
    fun validProjectile() {
        val weapon = MockWeaponAPI(
            "getLocation" to Vector2f(1000f, 1000f),
            "getShip" to MockShipAPI("getVelocity" to Vector2f(0f, 200f)),
            "getProjectileSpeed" to 750f,
        )

        val target = MockCombatEntityAPI(
            "getLocation" to Vector2f(-500f, 1500f),
            "getVelocity" to Vector2f(-100f, -100f),
            "getCollisionRadius" to 200f,
        )

        val actual = FiringSolution(weapon, target)

        assertEquals(2.3249526f, actual.interceptTime)
        assertEquals(Vector2f(-732.49524f, 802.5142f), actual.intercept)
        assertEquals(12.831601f, actual.interceptArc)
        assertEquals(186.33452f, actual.interceptFacing)
    }

    @Test
    fun validBeam() {
        val weapon = MockWeaponAPI(
            "getLocation" to Vector2f(1000f, 1000f),
            "getShip" to MockShipAPI("getVelocity" to Vector2f(0f, 200f)),
            "getProjectileSpeed" to Float.MAX_VALUE,
        )

        val target = MockCombatEntityAPI(
            "getLocation" to Vector2f(-500f, 1500f),
            "getVelocity" to Vector2f(-100f, -100f),
            "getCollisionRadius" to 200f,
        )

        val actual = FiringSolution(weapon, target)

        assertEquals(2.3249526f, actual.interceptTime)
        assertEquals(Vector2f(-732.49524f, 802.5142f), actual.intercept)
        assertEquals(12.831601f, actual.interceptArc)
        assertEquals(186.33452f, actual.interceptFacing)
    }
}

// Target is directly over the weapon.