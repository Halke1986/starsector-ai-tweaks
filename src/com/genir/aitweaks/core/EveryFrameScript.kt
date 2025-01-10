package com.genir.aitweaks.core

//import com.fs.starfarer.coreui.OOO0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO as FleetPanel
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.Fonts
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.TextFieldAPI
import com.fs.starfarer.campaign.CampaignState
import com.fs.starfarer.campaign.fleet.FleetData
import com.fs.starfarer.campaign.fleet.FleetMember
import java.lang.reflect.Method
import com.fs.starfarer.coreui.OOO0OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO as FleetPanel

class EveryFrameScript : EveryFrameScript {
    private val uiPanelClass: Class<*> = CampaignState::class.java.getMethod("getScreenPanel").returnType

    private val getDialogType: Method = CampaignState::class.java.getMethod("getDialogType")
    private val getChildrenCopy: Method = uiPanelClass.getMethod("getChildrenCopy")

    private var filterPanel: FleetFilterPanel? = null
    private var prevFleetPanel: FleetPanel? = null

    override fun advance(dt: Float) {
        val campaignState = Global.getSector().campaignUI as CampaignState
        val screenPanel = campaignState.screenPanel

        val uiDisplayed: Boolean = when {
            screenPanel == null -> false

            getDialogType.invoke(campaignState) == null -> false

            else -> true
        }

        // Run only when UI is displayed.
        if (uiDisplayed) {
            val fleetPanel = findFleetPanel(screenPanel)
            if (fleetPanel != prevFleetPanel) {
                filterPanel?.applyStash()
                filterPanel = null

                if (fleetPanel != null) {
                    if (fleetPanel.fleetData.fleet?.faction == null) {
                        filterPanel = FleetFilterPanel(200f, 20f, fleetPanel)
                    }
                }
            }

            this.prevFleetPanel = fleetPanel
        }
    }

    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = true

    private fun findFleetPanel(uiComponent: Any): FleetPanel? {
        return when {
            uiComponent is FleetPanel -> return uiComponent

            uiPanelClass.isInstance(uiComponent) -> {
                val children = getChildrenCopy.invoke(uiComponent) as List<*>
                val fleetPanels = children.asSequence().map { child -> findFleetPanel(child!!) }
                fleetPanels.filterNotNull().firstOrNull()
            }

            else -> null
        }
    }
}

class FleetFilterPanel(width: Float, height: Float, private val fleetPanel: FleetPanel) : CustomUIPanelPlugin {
    private val stash: FleetData = FleetData("aitweaks", fleetPanel.fleetData.fleet?.faction?.id)
    private var order: List<FleetMemberAPI>? = null

    private var mainPanel: CustomPanelAPI = Global.getSettings().createCustom(width, height, this)
    private var textField: TextFieldAPI
    private var prevString: String = ""

    private val xPad = 15f
    private val yPad = 15f

    init {
        val tooltip = mainPanel.createUIElement(width, height, false)
        textField = tooltip.addTextField(width, height, Fonts.DEFAULT_SMALL, 0f)
        mainPanel.addUIElement(tooltip).inTL(0f, 0f)
        fleetPanel.addComponent(mainPanel).inTR(xPad, yPad)
    }

    override fun advance(dt: Float) {
        if (textField.text == prevString) {
            return
        }

        if (order == null) {
            val newOrder: MutableList<FleetMemberAPI> = mutableListOf()
            fleetPanel.fleetData.members.forEach {
                newOrder.add(it)
            }
            order = newOrder
        }

        val toStash: List<FleetMember> = fleetPanel.fleetData.members.filter { fleetMember ->
            !fleetMember.variant.hullSpec.hullId.contains(textField.text)
        }

        val toFleet: List<FleetMember> = stash.members.filter { fleetMember ->
            fleetMember.variant.hullSpec.hullId.contains(textField.text)
        }

        toStash.forEach { fleetMember ->
            fleetPanel.fleetData.removeFleetMember(fleetMember)
            stash.addFleetMember(fleetMember)
        }

        toFleet.forEach { fleetMember ->
            fleetPanel.fleetData.addFleetMember(fleetMember)
            stash.removeFleetMember(fleetMember)
        }

        fleetPanel.fleetData.sortToMatchOrder(order)

        fleetPanel.recreateUI()
        fleetPanel.addComponent(mainPanel).inTR(xPad, yPad)

        prevString = textField.text
    }

    fun applyStash() {
        stash.members.forEach { fleetMember ->
            fleetPanel.fleetData.addFleetMember(fleetMember)
            stash.removeFleetMember(fleetMember)
        }
    }

    override fun positionChanged(position: PositionAPI) = Unit

    override fun renderBelow(alphaMult: Float) = Unit

    override fun render(alphaMult: Float) = Unit

    override fun processInput(events: List<InputEventAPI>) = Unit

    override fun buttonPressed(buttonId: Any) = Unit
}
