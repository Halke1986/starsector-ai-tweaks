package mocks

@Suppress("UNCHECKED_CAST")
open class Mock(vararg pairs: Pair<String, Any>) {
    private val values = mutableMapOf(*pairs)

    operator fun set(index: String, value: Any) {
        values[index] = value
    }

    fun <T> getMockValue(name: Any) = values[name.javaClass.enclosingMethod.name] as T
}