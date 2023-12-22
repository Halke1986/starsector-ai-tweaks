package com.genir.aitweaks.mock

import com.fs.starfarer.api.combat.BoundsAPI
import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShieldAPI
import org.lwjgl.util.vector.Vector2f

open class MockCombatEntityAPI : CombatEntityAPI {
    lateinit var getLocationMock: Vector2f
    override fun getLocation(): Vector2f = getLocationMock

    lateinit var getVelocityMock: Vector2f
    override fun getVelocity(): Vector2f = getVelocityMock

    override fun getFacing(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setFacing(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getAngularVelocity(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setAngularVelocity(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getOwner(): Int {
        TODO("Unexpected mock method call")
    }

    override fun setOwner(p0: Int) {
        TODO("Unexpected mock method call")
    }

    override fun getCollisionRadius(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getCollisionClass(): CollisionClass {
        TODO("Unexpected mock method call")
    }

    override fun setCollisionClass(p0: CollisionClass?) {
        TODO("Unexpected mock method call")
    }

    override fun getMass(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setMass(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getExactBounds(): BoundsAPI {
        TODO("Unexpected mock method call")
    }

    override fun getShield(): ShieldAPI {
        TODO("Unexpected mock method call")
    }

    override fun getHullLevel(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getHitpoints(): Float {
        TODO("Unexpected mock method call")
    }

    override fun getMaxHitpoints(): Float {
        TODO("Unexpected mock method call")
    }

    override fun setCollisionRadius(p0: Float) {
        TODO("Unexpected mock method call")
    }

    override fun getAI(): Any {
        TODO("Unexpected mock method call")
    }

    override fun isExpired(): Boolean {
        TODO("Unexpected mock method call")
    }

    override fun setCustomData(p0: String?, p1: Any?) {
        TODO("Unexpected mock method call")
    }

    override fun removeCustomData(p0: String?) {
        TODO("Unexpected mock method call")
    }

    override fun getCustomData(): MutableMap<String, Any> {
        TODO("Unexpected mock method call")
    }

    override fun setHitpoints(p0: Float) {
        TODO("Unexpected mock method call")
    }
}