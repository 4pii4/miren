package net.ccbluex.liquidbounce.ui.client.clickgui.styles.liquidbounce

import net.ccbluex.liquidbounce.utils.MinecraftInstance


open class Element : MinecraftInstance() {
    var x = 0
    var y = 0
    var width = 0
    open var height = 0
    var isVisible = false

    fun setLocation(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    open fun preDrawScreen(x: Float, y: Float, x2: Float, y2: Float) {}
    open fun drawScreen(mouseX: Int, mouseY: Int, button: Float, parent: Panel) {}
    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        return false
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, state: Int): Boolean {
        return false
    }
}