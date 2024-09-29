package com.genir.aitweaks.core.utils.extensions

val Class<*>.classPath: String
    get() = this.name.replace('.', '/')
