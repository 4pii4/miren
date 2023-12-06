package net.ccbluex.liquidbounce.ui.client.clickgui.styles.liquidbounce

import org.lwjgl.opengl.Display.getWidth


open class ButtonElement(displayName: String?) : Element() {
    var displayName: String? = null
        protected set
    var color = 0xffffff
    var hoverTime = 0

    init {
        createButton(displayName)
    }

    fun createButton(displayName: String?) {
        this.displayName = displayName
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, button: Float, parent: Panel) {
        LiquidBounceStyle.getInstance().drawButtonElement(mouseX, mouseY, this)
        super.drawScreen(mouseX, mouseY, button, parent)
    }

    override var height = 16

    fun isHovering(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 16
    }
}