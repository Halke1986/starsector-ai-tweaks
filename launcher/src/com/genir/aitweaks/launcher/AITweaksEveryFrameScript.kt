package com.genir.aitweaks.launcher

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.genir.aitweaks.launcher.loading.CoreLoaderManager
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.coreLoader
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.instantiate
import org.lwjgl.input.Keyboard

class AITweaksEveryFrameScript : EveryFrameScript {
    private var prevCoreScript: EveryFrameScript? = null
    private var debounce = 0f

    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = true

    override fun advance(dt: Float) {
//        Global.getLogger(this::class.java).info(Keyboard.isKeyDown(Keyboard.KEY_APOSTROPHE))
//        Global.getLogger(this::class.java).info(debounce)

//        if (Keyboard.isKeyDown(Keyboard.KEY_APOSTROPHE)) {
//            Global.getLogger(this::class.java).info(Keyboard.isKeyDown(Keyboard.KEY_APOSTROPHE))
//        }

        if (prevCoreScript == null || (Keyboard.isKeyDown(Keyboard.KEY_APOSTROPHE) && debounce <= 0f)) {
            reloadCoreScript()
            debounce = 1f
        }

        debounce -= dt
    }

    private fun reloadCoreScript() {
        CoreLoaderManager.updateLoader()
        val efsClass: Class<*> = coreLoader.loadClass("com.genir.aitweaks.core.EveryFrameScript")

        val sector = Global.getSector()
        if (sector.hasTransientScript(efsClass)) {
            return
        }

        Global.getLogger(this::class.java).info("Reload")
        prevCoreScript?.let { sector.removeTransientScript(it) }

        val newScript: EveryFrameScript = efsClass.instantiate()
        sector.addTransientScript(newScript)
        prevCoreScript = newScript
    }
}
