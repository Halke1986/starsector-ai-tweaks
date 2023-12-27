package com.genir.aitweaks.features.autofire

import com.genir.aitweaks.utils.mocks.Mock
import com.genir.aitweaks.utils.mocks.MockCombatEntityAPI
import com.genir.aitweaks.utils.mocks.MockShipAPI
import com.genir.aitweaks.utils.mocks.MockWeaponAPI
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.lwjgl.util.vector.Vector2f

internal class FiringSolutionTest {
    @Test
    fun validSolution() {
        val weapon = MockWeaponAPI(
            "getLocation" to Vector2f(1000f, 1000f),
            "getShip" to MockShipAPI(
                "getVelocity" to Vector2f(0f, 200f),
                "getFacing" to 20f,
            ),
            "getProjectileSpeed" to 750f,
            "getRange" to 1000f,
            "getArcFacing" to 180f,
            "getArc" to 45f,
        )

        val target = MockCombatEntityAPI(
            "getLocation" to Vector2f(-500f, 1500f),
            "getVelocity" to Vector2f(-100f, -100f),
            "getCollisionRadius" to 200f,
        )

        // Projectile case
        val actualProjectile = FiringSolution(weapon, target)
        assertTrue(actualProjectile.valid)
        assertEquals(Vector2f(-732.4953f, 802.51416f), actualProjectile.intercept)

        // Beam case
        (weapon as Mock).values["getProjectileSpeed"] = Float.MAX_VALUE

        val actualBeam = FiringSolution(weapon, target)
        assertTrue(actualBeam.valid)
        assertEquals(target.location, actualBeam.intercept)
    }

    @Test
    fun targetIsOverTheWeapon() {
        val weapon = MockWeaponAPI(
            "getLocation" to Vector2f(0f, 0f),
            "getShip" to MockShipAPI(
                "getVelocity" to Vector2f(0f, 0f),
                "getFacing" to 20f,
            ),
            "getProjectileSpeed" to Float.MAX_VALUE,
            "getRange" to 1000f,
            "getArcFacing" to 180f,
            "getArc" to 30f,
        )

        val target = MockCombatEntityAPI(
            "getLocation" to Vector2f(-0.001f, 0.001f),
            "getVelocity" to Vector2f(-100f, -100f),
            "getCollisionRadius" to 200f,
        )

        val actual = FiringSolution(weapon, target)

        assertTrue(actual.valid)
        assertTrue(actual.canTrack)
        assertEquals(target.location, actual.intercept)
    }

    @Test
    fun projectileSlowerThanTarget() {
        val weapon = MockWeaponAPI(
            "getLocation" to Vector2f(1000f, 1000f),
            "getShip" to MockShipAPI("getVelocity" to Vector2f(0f, 200f)),
            "getProjectileSpeed" to 20f,
            "getRange" to 1000f,
        )

        val target = MockCombatEntityAPI(
            "getLocation" to Vector2f(-500f, 1500f),
            "getVelocity" to Vector2f(-100f, -100f),
            "getCollisionRadius" to 150f,
        )

        val actual = FiringSolution(weapon, target)

        assertFalse(actual.valid)
    }

    @Test
    fun projectileHitsTargetInThePast() {
        val weapon = MockWeaponAPI(
            "getLocation" to Vector2f(),
            "getShip" to MockShipAPI("getVelocity" to Vector2f()),
            "getProjectileSpeed" to 1f,
            "getRange" to 1000f,
            "getArcFacing" to 180f,
        )

        val target = MockCombatEntityAPI(
            "getLocation" to Vector2f(0f, 10f),
            "getVelocity" to Vector2f(0f, 2f),
            "getCollisionRadius" to 1f,
        )

        val actual = FiringSolution(weapon, target)

        assertFalse(actual.valid)
    }

    @Test
    fun targetTrackableButBehindWeapon() {
        val weapon = MockWeaponAPI(
            "getLocation" to Vector2f(4321.4805f, -191.35764f),
            "getShip" to MockShipAPI(
                "getVelocity" to Vector2f(16.294046f, -78.323074f),
                "getFacing" to 213.7486f
            ),
            "getProjectileSpeed" to 1000.0f,
            "getRange" to 1000f,
            "getArcFacing" to 0.0f,
            "getArc" to 250.0f,
            "getCurrAngle" to 143.3941f,
        )

        val target = MockCombatEntityAPI(
            "getLocation" to Vector2f(4961.056f, -813.4499f),
            "getVelocity" to Vector2f(-256.60562f, 66.17056f),
            "getCollisionRadius" to 70.0f,
        )

        val solution = FiringSolution(weapon, target)
        assertTrue(solution.valid)

        val range = HitSolver(weapon).hitRange(target)
        assertNull(range)
    }
}
