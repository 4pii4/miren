package net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.buttons

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoClickGui
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.BACKGROUND_CATEGORY
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.BACKGROUND_MODULE
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.FONT
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.PANEL_HEIGHT
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoConstants.PANEL_WIDTH
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.drawHeightCenteredString
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl.Astolfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.geom.Rectangle
import net.ccbluex.liquidbounce.utils.math.sign
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.*
import net.ccbluex.liquidbounce.utils.render.Stencil
import net.ccbluex.liquidbounce.utils.render.animations.Direction
import net.ccbluex.liquidbounce.utils.render.animations.impl.CustomAnimation
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color

class AstolfoCategoryPanel(x: Float, y: Float, var category: ModuleCategory, var color: Color, private val others: ArrayList<AstolfoCategoryPanel>) : AstolfoButton(x, y, PANEL_WIDTH, PANEL_HEIGHT) {
    var open = false
    var moduleButtons = ArrayList<AstolfoModuleButton>()
    val name = category.displayName
    var zLayer = 0
    private var dragged = false
    private var mouseX2 = 0
    private var mouseY2 = 0
    var hovered = false
    private val anim = CustomAnimation(200, 1.0, Direction.FORWARDS) { EaseUtils.easeInOutQuart(it) }

    init {
        val startY = y + height
        for ((count, mod) in LiquidBounce.moduleManager.modules.filter { it.category.displayName.equals(this.category.displayName, true) }.withIndex()) {
            moduleButtons.add(AstolfoModuleButton(x, startY + height * count, width, height, mod, color))
        }
    }

    override fun drawPanel(mouseX: Int, mouseY: Int): Rectangle {
        if (dragged) {
            x = (mouseX2 + mouseX).toFloat()
            y = (mouseY2 + mouseY).toFloat()
        }

        anim.setDirection(if (open) Direction.FORWARDS else Direction.BACKWARDS)
        hovered = isHovered(mouseX, mouseY)

        val add = if (moduleButtons.isNotEmpty() && open) 1f else 0f
        val expectedHeight = moduleButtons.map { it.calcHeight() }.sum()
        val animHeight = anim.outputFloat * expectedHeight

        drawRect(x, y, x + width, y + height, BACKGROUND_CATEGORY)
        drawImage(category.icon, (x + width - 12).toInt(), (y + 5).toInt(), 8, 8, ColorUtils.interpolate(0.4, 1.0, anim.output).toFloat())

        FONT.drawHeightCenteredString(category.displayName.lowercase(), x + 8, y + height / 2, -0x1)

        makeScissorBox(x, y + height, x + width, y + height + animHeight)
        GL11.glEnable(GL11.GL_SCISSOR_TEST)

        var used = 0f
        if (anim.output > 0) {
            val startY = y + height
            for (moduleButton in moduleButtons) {
                moduleButton.x = x
                moduleButton.y = startY + used
                val box = moduleButton.drawPanel(mouseX, mouseY)
                used += box.height
            }
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST)

        if (moduleButtons.isNotEmpty() && anim.output > 0) {
            drawRect(x, y + height + used, x + width, y + height + used + add, BACKGROUND_MODULE)
        }

        drawBorderedRect(x, y, x + width, y + height + used*anim.outputFloat + add, 2f, color.rgb, Color(0, 0, 0, 0).rgb)


        return Rectangle()
    }

    override fun mouseAction(mouseX: Int, mouseY: Int, click: Boolean, button: Int): Boolean {
        if (isHovered(mouseX, mouseY)) {
            if (click) {
                zLayer = others.maxBy { it.zLayer }.zLayer + 1
                if (button == 0) {
                    dragged = true
                    mouseX2 = (x - mouseX).toInt()
                    mouseY2 = (y - mouseY).toInt()
                } else {
                    open = !open
                }
                return true
            }
        }
        if (!click) dragged = false
        return false
    }

    override fun onClosed() {
        dragged = false
    }
}
