package com.genir.aitweaks.core.extensions

import java.lang.reflect.Field

fun Any.getPrivateField(name: String, superClass: Class<*>? = null): Any? {
    val field: Field = (superClass ?: this::class.java).getDeclaredField(name).also { it.setAccessible(true) }
    return field.get(this)
}

fun Any.setPrivateField(name: String, value: Any, superClass: Class<*>? = null) {
    val field: Field = (superClass ?: this::class.java).getDeclaredField(name).also { it.setAccessible(true) }
    field.set(this, value)
}
