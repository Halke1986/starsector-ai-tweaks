package features.autofire.temp

import com.genir.aitweaks.features.autofire.temp.FiringSolution
import org.junit.jupiter.api.Test
import com.genir.aitweaks.mocks.MockCombatEntityAPI
import com.genir.aitweaks.mocks.MockShipAPI
import com.genir.aitweaks.mocks.MockWeaponAPI
import org.lwjgl.util.vector.Vector2f

internal class MathKtTest {


    @Test
    fun test1() {
        val ship = MockShipAPI(
            "getVelocity" to Vector2f(),
        )

        val weapon = MockWeaponAPI(
            "getLocation" to Vector2f(),
            "getShip" to ship,
            "getProjectileSpeed" to 0f,
        )

        val target = MockCombatEntityAPI(
            "getLocation" to Vector2f(),
            "getVelocity" to Vector2f(),
        )

        FiringSolution(weapon, target)
    }
}