package net.ccbluex.liquidbounce.ui.client.clickgui.styles.meteor

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.ClickGuiStyle

class MeteorClickGui: ClickGuiStyle("Meteor") {
    private val categoryPanels = mutableListOf<CategoryElement>()

    init {
        
    }
    override fun dumpConfig(): JsonElement {
        TODO("Not yet implemented")
    }

    override fun loadConfig(json: JsonObject) {
        TODO("Not yet implemented")
    }

    override fun getNewInstance(): MeteorClickGui {
        return MeteorClickGui()
    }
}