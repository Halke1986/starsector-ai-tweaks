package com.genir.aitweaks.utils.mocks

import com.fs.starfarer.api.combat.BoundsAPI
import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShieldAPI
import org.lwjgl.util.vector.Vector2f

open class MockCombatEntityAPI(vararg values: Pair<String, Any>) : CombatEntityAPI, Mock(*values) {
    override fun getLocation(): Vector2f = getMockValue(object {})

    override fun getVelocity(): Vector2f = getMockValue(object {})

    override fun getFacing(): Float = getMockValue(object {})

    override fun setFacing(p0: Float) = Unit

    override fun getAngularVelocity(): Float = getMockValue(object {})

    override fun setAngularVelocity(p0: Float) = Unit

    override fun getOwner(): Int = getMockValue(object {})

    override fun setOwner(p0: Int) = Unit

    override fun getCollisionRadius(): Float = getMockValue(object {})

    override fun getCollisionClass(): CollisionClass = getMockValue(object {})

    override fun setCollisionClass(p0: CollisionClass?) = Unit

    override fun getMass(): Float = getMockValue(object {})

    override fun setMass(p0: Float) = Unit

    override fun getExactBounds(): BoundsAPI = getMockValue(object {})

    override fun getShield(): ShieldAPI = getMockValue(object {})

    override fun getHullLevel(): Float = getMockValue(object {})

    override fun getHitpoints(): Float = getMockValue(object {})

    override fun getMaxHitpoints(): Float = getMockValue(object {})

    override fun setCollisionRadius(p0: Float) = Unit

    override fun getAI(): Any = getMockValue(object {})

    override fun isExpired(): Boolean = getMockValue(object {})

    override fun setCustomData(p0: String?, p1: Any?) = Unit

    override fun removeCustomData(p0: String?) = Unit

    override fun getCustomData(): MutableMap<String, Any> = getMockValue(object {})

    override fun setHitpoints(p0: Float) = Unit
}