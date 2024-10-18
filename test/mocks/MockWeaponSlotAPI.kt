package mocks

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.loading.WeaponSlotAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI
import org.lwjgl.util.vector.Vector2f

class MockWeaponSlotAPI(vararg values: Pair<String, Any?>) : WeaponSlotAPI, Mock(*values) {
    override fun isHardpoint(): Boolean = getMockValue(object {})!!

    override fun isTurret(): Boolean = getMockValue(object {})!!

    override fun isHidden(): Boolean = getMockValue(object {})!!

    override fun isSystemSlot(): Boolean = getMockValue(object {})!!

    override fun isBuiltIn(): Boolean = getMockValue(object {})!!

    override fun isDecorative(): Boolean = getMockValue(object {})!!

    override fun getId(): String = getMockValue(object {})!!

    override fun getWeaponType(): WeaponAPI.WeaponType = getMockValue(object {})!!

    override fun getSlotSize(): WeaponAPI.WeaponSize = getMockValue(object {})!!

    override fun getArc(): Float = getMockValue(object {})!!

    override fun setArc(arc: Float) = Unit

    override fun getAngle(): Float = getMockValue(object {})!!

    override fun computePosition(ship: CombatEntityAPI?): Vector2f = getMockValue(object {})!!

    override fun isStationModule(): Boolean = getMockValue(object {})!!

    override fun weaponFits(spec: WeaponSpecAPI?): Boolean = getMockValue(object {})!!

    override fun setAngle(angle: Float) = Unit

    override fun getLocation(): Vector2f = getMockValue(object {})!!

    override fun getRenderOrderMod(): Float = getMockValue(object {})!!

    override fun setRenderOrderMod(renderOrderMod: Float) = Unit

    override fun computeMidArcAngle(ship: ShipAPI?): Float = getMockValue(object {})!!

    override fun getLaunchPointOffsets(): MutableList<Vector2f> = getMockValue(object {})!!

    override fun isWeaponSlot(): Boolean = getMockValue(object {})!!
}
