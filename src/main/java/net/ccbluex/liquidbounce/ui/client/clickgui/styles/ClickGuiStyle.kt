package net.ccbluex.liquidbounce.ui.client.clickgui.styles

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.client.gui.GuiScreen

abstract class ClickGuiStyle(val name: String): GuiScreen() {
    abstract fun dumpConfig(): JsonElement
    abstract fun loadConfig(json: JsonObject)
}