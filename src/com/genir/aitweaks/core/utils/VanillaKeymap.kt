package com.genir.aitweaks.core.utils

import com.fs.starfarer.campaign.ui.fleet.FleetMemberView
import com.genir.aitweaks.core.utils.extensions.classPath
import com.genir.aitweaks.core.utils.loading.Bytecode
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method

class VanillaKeymap {
    private val isKeyDown: MethodHandle
    private val actions: Map<String, Any>

    init {
        val fleetMemberView: Class<*> = FleetMemberView::class.java
        val button: Class<*> = fleetMemberView.getMethod("getButton").returnType
        val actionEnum: Class<*> = button.getMethod("getShortcut").returnType
        val keymap: Class<*> = actionEnum.enclosingClass

        val isKeyDownName = Bytecode.getMethodsInOrder(keymap).first { it.desc == "(L${actionEnum.classPath};)Z" }
        val isKeyDown: Method = keymap.methods.first { it.name == isKeyDownName.name && it.parameterTypes.contentEquals(arrayOf(actionEnum)) }

        this.isKeyDown = MethodHandles.lookup().unreflect(isKeyDown)
        this.actions = actionEnum.enumConstants.associateBy { (it as Enum<*>).name }
    }

    fun isKeyDown(action: Action): Boolean {
        return isKeyDown.invoke(actions[action.name]) as Boolean
    }

    enum class Action {
        SHIP_STRAFE_KEY,
        SHIP_FIRE,
        SHIP_TURN_LEFT,
        SHIP_TURN_RIGHT,
        SHIP_STRAFE_LEFT_NOTURN,
        SHIP_STRAFE_RIGHT_NOTURN,
        SHIP_ACCELERATE,
        SHIP_ACCELERATE_BACKWARDS,
    }
}
