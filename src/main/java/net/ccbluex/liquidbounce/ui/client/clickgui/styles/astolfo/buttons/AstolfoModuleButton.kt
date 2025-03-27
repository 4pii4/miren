package net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.buttons

import net.ccbluex.liquidbounce.LiquidBounce.commandManager
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.BACKGROUND_MODULE
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.BACKGROUND_VALUE
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.FONT
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.MODULE_HEIGHT
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.VALUE_HEIGHT
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.buttons.value.*
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.drawHeightCenteredString
import net.ccbluex.liquidbounce.utils.MouseButtons
import net.ccbluex.liquidbounce.utils.geom.Rectangle
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.scaleStart
import net.ccbluex.liquidbounce.utils.render.animations.Direction
import net.ccbluex.liquidbounce.utils.render.animations.impl.CustomAnimation
import net.ccbluex.liquidbounce.value.*
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color

class AstolfoModuleButton(x: Float, y: Float, width: Float, height: Float, var module: Module, var color: Color) : AstolfoButton(x, y, width, height) {
    var open = false
    var valueButtons = ArrayList<BaseValueButton>()
    private val shifting: Boolean
        get() = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
    private val openAnim = CustomAnimation(150, 1.0, Direction.FORWARDS) { EaseUtils.easeInOutQuart(it) }
    private val stateAnim = CustomAnimation(150, 1.0, Direction.FORWARDS) { EaseUtils.easeInOutQuart(it) }

    init {
        val startY = y + height
        for ((count, v) in module.values.withIndex()) {
            when (v) {
//                is NoteValue -> valueButtons.add(NoteValueButton(x, startY + MODULE_HEIGHT * count, width, VALUE_HEIGHT, v, color))
                is BlockValue -> valueButtons.add(BlockValueButton(x, startY + MODULE_HEIGHT * count, width, VALUE_HEIGHT, v, color))
                is BoolValue -> valueButtons.add(BoolValueButton(x, startY + MODULE_HEIGHT * count, width, VALUE_HEIGHT, v, color))
                is ListValue -> valueButtons.add(ListValueButton(x, startY + MODULE_HEIGHT * count, width, VALUE_HEIGHT, v, color))
                is IntegerValue -> valueButtons.add(IntegerValueButton(x, startY + MODULE_HEIGHT * count, width, VALUE_HEIGHT, v, color))
                is FloatValue -> valueButtons.add(FloatValueButton(x, startY + MODULE_HEIGHT * count, width, VALUE_HEIGHT, v, color))
                is FontValue -> valueButtons.add(FontValueButton(x, startY + MODULE_HEIGHT * count, width, VALUE_HEIGHT, v, color))
                is TextValue -> valueButtons.add(TextValueButton(x, startY + MODULE_HEIGHT * count, width, VALUE_HEIGHT, v, color))
                else -> {}
            }
        }

        openAnim.setDirection(Direction.BACKWARDS)
        stateAnim.setDirection(Direction.BACKWARDS)
    }

    fun calcHeight() = this.height + if (open) valueButtons.map { it.calcExpectedHeight() }.sum()*openAnim.outputFloat else 0f

    fun drawDescription(mouseXIn: Int, mouseYIn: Int) {
        val mouseX = mouseXIn + 7
        val mouseY = mouseYIn - 3
        if (module.description.isEmpty())
            return
        val desc = module.description
        val textWidth = FONT.getStringWidth(desc)
        val textHeight = FONT.FONT_HEIGHT
        drawRect(mouseX, mouseY, mouseX + textWidth + 4, mouseY + textHeight + 4, module.category.color)
        drawRect(mouseX + 1, mouseY + 1, mouseX + textWidth + 3, mouseY + textHeight + 3, BACKGROUND_VALUE)
        FONT.drawHeightCenteredString(desc, mouseX + 3f, mouseY + 5.5f, -0x1)
    }

    override fun drawPanel(mouseX: Int, mouseY: Int): Rectangle {
        openAnim.setDirection(if (open) Direction.FORWARDS else Direction.BACKWARDS)
        stateAnim.setDirection(if (module.state) Direction.FORWARDS else Direction.BACKWARDS)

        var used = 0f
        val expectedHeight = this.valueButtons.map { it.calcExpectedHeight() }.sum() + if (open) 4 else 0
        val animHeight = openAnim.outputFloat * expectedHeight
        val foreground = Rectangle(x + 2, y, width - 2 * 2, height)
        val background = Rectangle(x, y, width, height)
        drawRect(background, BACKGROUND_MODULE)

        drawRect(foreground, ColorUtils.interpolateColorC(Color(BACKGROUND_MODULE), color, stateAnim.outputFloat))

        FONT.drawHeightCenteredString(module.name.lowercase(), x + width - FONT.getStringWidth(module.name.lowercase()) - 6, y + height / 2, -0x1)

        if (valueButtons.size > 0)
            FONT.drawHeightCenteredString(if (open) "-" else "+", x + 6, y + height / 2, Int.MAX_VALUE)

        if (openAnim.output > 0) {
            drawRect(x, y + height, x + width, y + height + animHeight, BACKGROUND_VALUE)
            val startY = y + height + 2
            GL11.glPushMatrix()
            scaleStart(x, startY, 1f, animHeight/expectedHeight)
            for (valueButton in valueButtons) {
                if (!valueButton.canDisplay()) {
                    valueButton.show = false
                    continue
                }

                valueButton.x = x
                valueButton.y = startY + used

                used += valueButton.drawPanel(mouseX, mouseY).height
            }
            GL11.glPopMatrix()
        }

        return Rectangle(x, y, width, height + animHeight)
    }

    override fun mouseAction(mouseX: Int, mouseY: Int, click: Boolean, button: Int): Boolean {
        if (isHovered(mouseX, mouseY) && click) {
            when (button) {
                MouseButtons.LEFT.ordinal -> if (shifting) commandManager.executeCommands("${commandManager.prefix}hide ${module.name}") else module.toggle()
                MouseButtons.RIGHT.ordinal -> if (valueButtons.isNotEmpty()) open = !open
            }
            return true
        }
        return false
    }

    override fun onClosed() {}
}