package com.genir.aitweaks.core.extensions

fun Float.ifNaN(fallback: Float): Float =
    if (isNaN()) fallback else this
