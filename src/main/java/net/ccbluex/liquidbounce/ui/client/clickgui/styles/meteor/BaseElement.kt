package net.ccbluex.liquidbounce.ui.client.clickgui.styles.meteor

abstract class BaseElement(var x: Float, var y: Float, var x2: Float, var y2: Float) {
    abstract fun draw(mouseX: Int, mouseY: Int): Float

    fun isHovered(mouseX: Int, mouseY: Int) = mouseX.toFloat() in x..x2 && mouseY.toFloat() in y..y2

    val width: Float
        get() = x2 - x
    val height: Float
        get() = y2 - y
}