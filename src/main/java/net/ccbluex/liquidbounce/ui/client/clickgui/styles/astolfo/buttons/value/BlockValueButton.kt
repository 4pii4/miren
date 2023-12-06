package net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.buttons.value

import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.BACKGROUND_VALUE
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.FONT
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.SELECTED_FORMAT
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.VALUE_HEIGHT
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.drawHeightCenteredString
import net.ccbluex.liquidbounce.utils.MouseButtons
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.geom.Rectangle
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.value.BlockValue
import java.awt.Color


class BlockValueButton(x: Float, y: Float, width: Float, height: Float, var setting: BlockValue, var color: Color) : BaseValueButton(x, y, width, height, setting) {
    private val listEntryBoxTriples = mutableListOf<Entry>()

    class Entry(val rect: Rectangle, val name: String, val id: Int)

    override val expectedHeight: Float
        get() = VALUE_HEIGHT + VALUE_HEIGHT * if (setting.openList) BlockUtils.getBlockNamesAndIDs().size else 0

    override fun drawPanel(mouseX: Int, mouseY: Int): Rectangle {
        val background = Rectangle(x, y, width, height)
        drawRect(background, BACKGROUND_VALUE)
        FONT.drawHeightCenteredString(setting.name, x + hOffset, y + height / 2, -0x1)

        val format = BlockUtils.getBlockName2(setting.get())
        val formatWidth = FONT.getStringWidth(format)
        FONT.drawHeightCenteredString(format, x + width - formatWidth - hOffset, y + height / 2, -0x1)

        var count = 0
        listEntryBoxTriples.clear()
        if (setting.openList) {
            for (blockPair in BlockUtils.getBlockNamesAndIDs()) {
                val rect = Rectangle(x, y + (count + 1) * height, width, height)
                listEntryBoxTriples.add(Entry(rect, blockPair.first, blockPair.second))
                drawRect(rect, BACKGROUND_VALUE)

                val listEntryText = (if (setting.get() == blockPair.second) SELECTED_FORMAT else "") + blockPair.first
                FONT.drawHeightCenteredString(listEntryText, rect.x + width - FONT.getStringWidth(listEntryText) - hOffset, rect.y + height / 2, if (setting.get() == blockPair.second) color.rgb else Color(128, 128, 128).rgb)
                count++
            }
        }

        background.height += count * height

        return background
    }

    override fun mouseAction(mouseX: Int, mouseY: Int, click: Boolean, button: Int): Boolean {
        if (!show) return false
        if (click) {
            when (button) {
                MouseButtons.LEFT.ordinal -> {
                    for (t in listEntryBoxTriples) {
                        if (t.rect.contains(mouseX, mouseY)) {
                            setting.set(t.id)
                            return true
                        }
                    }
                }

                MouseButtons.RIGHT.ordinal -> {
                    if (baseRect.contains(mouseX, mouseY)) {
                        setting.openList = !setting.openList
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun onClosed() {}
}
