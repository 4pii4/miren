package net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.buttons.value

import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.FONT
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.MODULE_HEIGHT
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.VALUE_HEIGHT
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.buttons.AstolfoButton
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.getHeight
import net.ccbluex.liquidbounce.utils.geom.Rectangle
import net.ccbluex.liquidbounce.value.Value

abstract class BaseValueButton(x: Float, y: Float, width: Float, height: Float, val value: Value<*>) : AstolfoButton(x, y, width, height) {
    val baseRect: Rectangle
        get() = Rectangle(x, y, width, height)
    val hOffset: Float
        get() = (height - getHeight(FONT)) / 2 + 4
    var show = true

    fun canDisplay() = value.canDisplay.invoke()

    open val expectedHeight = VALUE_HEIGHT

    fun calcExpectedHeight(): Float {
        if (!value.canDisplay.invoke())
            return 0f
        return expectedHeight
    }
}