package net.ccbluex.liquidbounce.ui.client.clickgui.styles

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.client.gui.GuiScreen

abstract class ClickGuiStyle(val name: String): GuiScreen() {
    abstract fun dumpConfig(): JsonElement
    abstract fun loadConfig(json: JsonObject)

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    open fun getNewInstance(): ClickGuiStyle {
        return this::class.java.getDeclaredConstructor(String::class.java).newInstance(name)
    }

    fun resetInstance() {
        this.instance = getNewInstance()
    }

    var instance: ClickGuiStyle? = null
        get() = if (field == null) getNewInstance().also { field = it } else field
}