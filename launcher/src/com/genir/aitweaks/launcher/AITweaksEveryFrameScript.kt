package com.genir.aitweaks.launcher

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.genir.aitweaks.launcher.loading.CoreLoaderManager
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.coreLoader
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.instantiate
import lunalib.lunaSettings.LunaSettings
import org.lwjgl.input.Keyboard

class AITweaksEveryFrameScript : EveryFrameScript {
    private var prevCoreScript: EveryFrameScript? = null
    private var debounce = 0f

    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = true

    override fun advance(dt: Float) {
        val devMode = LunaSettings.getBoolean("aitweaks", "aitweaks_enable_devmode") == true
        if (devMode && Keyboard.isKeyDown(Keyboard.KEY_APOSTROPHE) && debounce <= 0f) {
            CoreLoaderManager.updateLoader()
            debounce = 1f
        }

        debounce -= dt

        val efsClass: Class<*> = coreLoader.loadClass("com.genir.aitweaks.core.EveryFrameScript")
        val sector = Global.getSector()
        if (sector.hasTransientScript(efsClass)) {
            return
        }

        prevCoreScript?.let { sector.removeTransientScript(it) }

        val newScript: EveryFrameScript = efsClass.instantiate()
        sector.addTransientScript(newScript)
        prevCoreScript = newScript
    }
}
