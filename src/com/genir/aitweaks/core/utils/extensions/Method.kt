package com.genir.aitweaks.core.utils.extensions

import java.lang.reflect.Method

fun Method.hasParameters(vararg params: Class<*>): Boolean {
    return parameterTypes.contentEquals(arrayOf(*params))
}
