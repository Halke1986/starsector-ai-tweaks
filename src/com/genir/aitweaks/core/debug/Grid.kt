package com.genir.aitweaks.core.debug

import com.fs.graphics.Sprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatUIAPI
import com.genir.aitweaks.core.state.Config.Companion.config
import java.lang.reflect.Field

fun removeGrid() {
    if (!config.removeCombatMapGrid) return

    val ui: CombatUIAPI = Global.getCombatEngine().combatUI ?: return
    val warroom: Any = ui::class.java.getMethod("getWarroom").invoke(ui) ?: return
    val mapDisplay: Any = warroom::class.java.getMethod("getMapDisplay").invoke(warroom) ?: return

    val texturePath = "graphics/hud/line4x4_translucent.png"
    Global.getSettings().loadTexture(texturePath)
    val sprite = Sprite(texturePath)
    val texture: Any = sprite::class.java.getMethod("getTexture").invoke(sprite) ?: return

    val textureFields: List<Field> = mapDisplay::class.java.declaredFields.filter { it.type == texture::class.java }
    textureFields.forEach { field ->
        field.setAccessible(true)
        field.set(mapDisplay, texture)
    }
}
