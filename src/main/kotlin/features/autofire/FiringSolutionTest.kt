package com.genir.aitweaks.features.autofire

import org.junit.jupiter.api.Test
import com.genir.aitweaks.mocks.MockCombatEntityAPI
import com.genir.aitweaks.mocks.MockShipAPI
import com.genir.aitweaks.mocks.MockWeaponAPI
import org.junit.jupiter.api.Assertions.*
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

        assertTrue(actual.valid)
        assertEquals(Vector2f(-732.4953f, 802.51416f), actual.intercept)
        assertEquals(2.3249528f, actual.interceptTime)

        assertEquals(12.8316f, actual.interceptArc)
        assertEquals(186.33452f, actual.interceptFacing)
        assertEquals(1503.4849f, actual.closestPossibleHit)
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

        assertTrue(actual.valid)
        assertEquals(4.6465497E-36f, actual.interceptTime)
        assertEquals(target.location, actual.intercept)

        assertEquals(14.163194f, actual.interceptArc)
        assertEquals(161.83597f, actual.interceptFacing)
        assertEquals(1381.1388f, actual.closestPossibleHit)
    }

    @Test
    fun targetIsOverTheWeapon() {
        val weapon = MockWeaponAPI(
            "getLocation" to Vector2f(0f, 0f),
            "getShip" to MockShipAPI("getVelocity" to Vector2f(0f, 0f)),
            "getProjectileSpeed" to Float.MAX_VALUE,
        )

        val target = MockCombatEntityAPI(
            "getLocation" to Vector2f(-0.001f, 0.001f),
            "getVelocity" to Vector2f(-100f, -100f),
            "getCollisionRadius" to 200f,
        )

        val actual = FiringSolution(weapon, target)

        assertTrue(actual.valid)
        assertEquals(target.location, actual.intercept)
        assertEquals(4.156E-42f, actual.interceptTime)

        assertEquals(360f, actual.interceptArc)
        assertEquals(135.28372f, actual.interceptFacing)
        assertEquals(0f, actual.closestPossibleHit)
    }

    @Test
    fun projectileSlowerThanTarget() {
        val weapon = MockWeaponAPI(
            "getLocation" to Vector2f(1000f, 1000f),
            "getShip" to MockShipAPI("getVelocity" to Vector2f(0f, 200f)),
            "getProjectileSpeed" to 20f,
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
        )

        val target = MockCombatEntityAPI(
            "getLocation" to Vector2f(0f, 10f),
            "getVelocity" to Vector2f(0f, 2f),
            "getCollisionRadius" to 1f,
        )

        val actual = FiringSolution(weapon, target)

        assertFalse(actual.valid)
    }


}