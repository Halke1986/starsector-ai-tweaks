package features.autofire.temp

import com.genir.aitweaks.features.autofire.temp.FiringSolution
import org.junit.jupiter.api.Test
import com.genir.aitweaks.mock.MockCombatEntityAPI
import com.genir.aitweaks.mock.MockShipAPI
import com.genir.aitweaks.mock.MockWeaponAPI
import org.lwjgl.util.vector.Vector2f

//class MockShip(private val mockVelocity: Vector2f) : MockShipAPI() {
//    override fun getVelocity(): Vector2f = mockVelocity
//}
//
//class MockWeapon(
//    private val mockLocation: Vector2f, private val mockShip: MockShip, private val mockProjectileSpeed: Float
//) : MockWeaponAPI() {
//    override fun getLocation(): Vector2f = mockLocation
//    override fun getShip(): ShipAPI = mockShip
//    override fun getProjectileSpeed(): Float = mockProjectileSpeed
//}
//
//class MockCombatEntity(private val mockLocation: Vector2f, private val mockVelocity: Vector2f) : MockCombatEntityAPI() {
//    override fun getLocation(): Vector2f = mockLocation
//    override fun getVelocity(): Vector2f = mockVelocity
//}

internal class MathKtTest {


    @Test
    fun test1() {
        val ship = MockShipAPI()
        ship.getVelocityMock = Vector2f()

        val weapon = MockWeaponAPI()
        weapon.getLocationMock = Vector2f()
        weapon.getShipMock = ship
        weapon.getProjectileSpeedMock = 0f

        val target = MockCombatEntityAPI()
        target.getLocationMock = Vector2f()
        target.getVelocityMock = Vector2f()

        FiringSolution(weapon, target)
    }
}