package com.genir.aitweaks.core.debug

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.campaign.CampaignState
import com.fs.starfarer.ui.newui.CampaignEntityPickerDialog
import com.genir.aitweaks.core.utils.log
import java.lang.reflect.Method

class EveryFrameScript : EveryFrameScript {
    private val uiPanelClass: Class<*> = CampaignState::class.java.getMethod("getScreenPanel").returnType
    private val dialogClass: Class<*> = CampaignEntityPickerDialog::class.java.superclass

    private val getScreenPanel: Method = CampaignState::class.java.getMethod("getScreenPanel")
    private val getDialogType: Method = CampaignState::class.java.getMethod("getDialogType")
    private val getInnerPanel: Method = dialogClass.getMethod("getInnerPanel")
    private val getChildrenCopy: Method = uiPanelClass.getMethod("getChildrenCopy")

    override fun advance(dt: Float) {
        log("****************************************************")

        val campaignState = Global.getSector().campaignUI as CampaignState
        val screenPanel: Any = getScreenPanel.invoke(campaignState) ?: return

        // Run only when UI is displayed.
        when {
            getDialogType.invoke(campaignState) == null -> return
        }

        iterateUI(screenPanel, 0)
    }

    private fun iterateUI(uiComponent: Any, depth: Int) {
        log("\t".repeat(depth) + uiComponent.javaClass.name)

        when {
            dialogClass.isInstance(uiComponent) -> iterateUI(getInnerPanel.invoke(uiComponent), depth + 1)

            uiPanelClass.isInstance(uiComponent) -> (getChildrenCopy.invoke(uiComponent) as List<*>).forEach { child ->
                iterateUI(child!!, depth + 1)
            }
        }
    }

    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = true
}
