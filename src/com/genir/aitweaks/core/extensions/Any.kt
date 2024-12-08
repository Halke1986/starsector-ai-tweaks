package com.genir.aitweaks.core.extensions

import java.lang.reflect.Field

fun Any.getPrivateField(name: String): Any? {
    val field: Field = this::class.java.getDeclaredField(name).also { it.setAccessible(true) }
    return field.get(this)
}
