package mocks

import com.fs.starfarer.api.combat.BoundsAPI
import org.lwjgl.util.vector.Vector2f

open class MockBoundsAPI(vararg values: Pair<String, Any?>) : BoundsAPI, Mock(*values) {
    override fun update(p0: Vector2f?, p1: Float) = Unit

    override fun getSegments(): MutableList<BoundsAPI.SegmentAPI>? = getMockValue(object {})

    override fun clear() = Unit

    override fun addSegment(p0: Float, p1: Float, p2: Float, p3: Float) = Unit

    override fun addSegment(p0: Float, p1: Float) = Unit

    override fun getOrigSegments(): MutableList<BoundsAPI.SegmentAPI>? = getMockValue(object {})
}

open class MockSegmentAPI(private val a: Vector2f, private val b: Vector2f) : BoundsAPI.SegmentAPI {
    override fun getP1(): Vector2f {
        return a
    }

    override fun getP2(): Vector2f {
        return b
    }
}
