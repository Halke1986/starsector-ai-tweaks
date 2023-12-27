package com.genir.aitweaks.utils.mocks

open class Mock(vararg pairs: Pair<String, Any>) {
    public val values = mutableMapOf(*pairs)

    fun <T> getMockValue(name: Any) = values[name.javaClass.enclosingMethod.name] as T
}