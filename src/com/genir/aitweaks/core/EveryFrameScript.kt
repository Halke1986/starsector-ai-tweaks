package com.genir.aitweaks.core

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.*
import com.fs.starfarer.campaign.CampaignState
import com.fs.starfarer.campaign.fleet.FleetData
import com.fs.starfarer.campaign.fleet.FleetMember
import java.lang.reflect.Method

class EveryFrameScript : EveryFrameScript {
    private val getScreenPanel: Method = CampaignState::class.java.getMethod("getScreenPanel")
    private val uiPanelClass: Class<*> = getScreenPanel.returnType
    private val getChildrenCopy: Method = uiPanelClass.getMethod("getChildrenCopy")
    private var fleetPanelClass: Class<*>? = null

    private var filterPanel: FleetFilterPanel? = null
    private var prevFleetPanel: Any? = null

    override fun advance(dt: Float) {
        val campaignState = Global.getSector().campaignUI
        if (campaignState.currentCoreTab != CoreUITabId.FLEET) {
            updateFilterPanel(null)
            return
        }

        // Attach a new filter panel to each fleet panel.
        val screenPanel: Any = getScreenPanel.invoke(campaignState)
        val fleetPanel = findFleetPanel(screenPanel) as? UIPanelAPI
        if (fleetPanel != prevFleetPanel) {
            updateFilterPanel(fleetPanel)
        }
    }

    private fun updateFilterPanel(fleetPanel: UIPanelAPI?) {
        filterPanel?.applyStash()
        filterPanel = fleetPanel?.let { FleetFilterPanel(200f, 20f, it) }
        prevFleetPanel = fleetPanel
    }

    /** Find the currently displayed FleetPanel, if any. Assume
     * there's only one FleetPanel being displayed at a time. */
    private fun findFleetPanel(uiComponent: Any): Any? {
        // There's no easy way to statically find the FleetPanel Class.
        // Here we find it dynamically, when traversing the UI tree.
        // isFleetPanelClass() call is very expensive, so the result is cached.
        if (fleetPanelClass == null && isFleetPanelClass(uiComponent::class.java)) {
            fleetPanelClass = uiComponent::class.java
        }

        return when {
            fleetPanelClass?.isInstance(uiComponent) == true -> {
                return uiComponent
            }

            uiPanelClass.isInstance(uiComponent) -> {
                val children = getChildrenCopy.invoke(uiComponent) as List<*>
                val fleetPanels = children.asSequence().map { child -> findFleetPanel(child!!) }
                fleetPanels.filterNotNull().firstOrNull()
            }

            else -> null
        }
    }

    private fun isFleetPanelClass(clazz: Class<*>): Boolean {
        return clazz.methods.any { it.name == "getOther" && it.returnType == FleetData::class.java }
    }

    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = true
}

class FleetFilterPanel(width: Float, height: Float, private val fleetPanel: UIPanelAPI) : CustomUIPanelPlugin {
    private val fleetPanelClass: Class<*> = fleetPanel::class.java
    private val getFleetData: Method = fleetPanelClass.methods.first { it.name == "getFleetData" }
    private val recreateUI: Method = fleetPanelClass.methods.first { it.name == "recreateUI" }

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

        // Work only for storage or market fleets,
        // which can be recognized by missing faction.
        if (fleetPanel.fleetData.fleet?.faction == null) {
            mainPanel.addUIElement(tooltip).inTL(0f, 0f)
            fleetPanel.addComponent(mainPanel).inTR(xPad, yPad)
        }
    }

    override fun advance(dt: Float) {
        if (textField.text == prevString) {
            return
        }

        val fleetData = fleetPanel.fleetData

        if (order == null) {
            val newOrder: MutableList<FleetMemberAPI> = mutableListOf()
            fleetData.members.forEach {
                newOrder.add(it)
            }
            order = newOrder
        }

        val toStash: List<FleetMember> = fleetData.members.filter { fleetMember ->
            !fleetMember.hullIdCleaned.contains(textField.text)
        }

        val toFleet: List<FleetMember> = stash.members.filter { fleetMember ->
            fleetMember.hullIdCleaned.contains(textField.text)
        }

        toStash.forEach { fleetMember ->
            fleetData.removeFleetMember(fleetMember)
            stash.addFleetMember(fleetMember)
        }

        toFleet.forEach { fleetMember ->
            fleetData.addFleetMember(fleetMember)
            stash.removeFleetMember(fleetMember)
        }

        fleetData.sortToMatchOrder(order)

        recreateUI.invoke(fleetPanel)
        fleetPanel.addComponent(mainPanel).inTR(xPad, yPad)

        prevString = textField.text
    }

    fun applyStash() {
        val fleetData = fleetPanel.fleetData
        stash.members.forEach { fleetMember ->
            fleetData.addFleetMember(fleetMember)
            stash.removeFleetMember(fleetMember)
        }
    }

    private val FleetMember.hullIdCleaned: String
        get() = hullId.removeSuffix("_default_D")

    private val UIPanelAPI.fleetData: FleetData
        get() = getFleetData.invoke(this) as FleetData

    override fun positionChanged(position: PositionAPI) = Unit

    override fun renderBelow(alphaMult: Float) = Unit

    override fun render(alphaMult: Float) = Unit

    override fun processInput(events: List<InputEventAPI>) = Unit

    override fun buttonPressed(buttonId: Any) = Unit
}
