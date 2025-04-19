package com.genir.aitweaks.launcher

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.genir.aitweaks.launcher.loading.CoreLoaderManager
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.coreLoader
import com.genir.aitweaks.launcher.loading.CoreLoaderManager.instantiate
import com.sun.management.HotSpotDiagnosticMXBean
import lunalib.lunaSettings.LunaSettings
import org.lwjgl.input.Keyboard
import java.lang.management.ManagementFactory

class AITweaksEveryFrameScript : EveryFrameScript {
    private var prevEfs: EveryFrameScript? = null
    private var efsAge = 0f

    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = true

    override fun advance(dt: Float) {
        if (LunaSettings.getBoolean("aitweaks", "aitweaks_enable_devmode") != true) {
            return
        }

        efsAge += dt
        if (Keyboard.isKeyDown(Keyboard.KEY_LBRACKET) && efsAge >= 1f) {
            reloadEFS()
            efsAge = 0f
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_RBRACKET)) {
            dumpHeap()
        }
    }

    private fun reloadEFS() {
        CoreLoaderManager.updateLoader()

        val efsClass: Class<*> = coreLoader.loadClass("com.genir.aitweaks.core.EveryFrameScript")
        val sector = Global.getSector()
        if (sector.hasTransientScript(efsClass)) {
            return
        }

        prevEfs?.let { sector.removeTransientScript(it) }

        val newScript: EveryFrameScript = efsClass.instantiate()
        sector.addTransientScript(newScript)
        prevEfs = newScript
    }

    @Throws(Exception::class)
    private fun dumpHeap() {
        val mxBean = ManagementFactory.newPlatformMXBeanProxy(ManagementFactory.getPlatformMBeanServer(), "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean::class.java)
        val path = "heapdump_${System.currentTimeMillis() / 1000}.hprof"
        mxBean.dumpHeap(path, false)
    }
}
