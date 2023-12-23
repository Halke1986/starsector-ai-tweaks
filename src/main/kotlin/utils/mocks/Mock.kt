package com.genir.aitweaks.utils.mocks

open class Mock(vararg pairs: Pair<String, Any>) {
    private val values = mapOf(*pairs)

    fun <T> getMockValue(name: Any) = values[name.javaClass.enclosingMethod.name] as T
}