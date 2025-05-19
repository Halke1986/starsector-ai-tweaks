package com.genir.aitweaks.core.extensions

import com.fs.starfarer.api.combat.WeaponGroupAPI
import com.genir.aitweaks.core.handles.WeaponHandle
import com.genir.aitweaks.core.handles.WeaponHandle.Companion.handle

val WeaponGroupAPI.weapons: List<WeaponHandle>
    get() = weaponsCopy.map { weaponAPI -> weaponAPI.handle }
