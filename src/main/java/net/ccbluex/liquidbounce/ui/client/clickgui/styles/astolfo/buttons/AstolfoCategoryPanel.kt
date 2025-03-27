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
import net.ccbluex.liquidbounce.utils.DesktopUtils
import net.ccbluex.liquidbounce.utils.geom.Rectangle
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.*
import net.ccbluex.liquidbounce.utils.render.animations.Direction
import net.ccbluex.liquidbounce.utils.render.animations.impl.CustomAnimation
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color

open class AstolfoCategoryPanel(x: Float, y: Float, var category: ModuleCategory, var color: Color, private val others: ArrayList<AstolfoCategoryPanel>) : AstolfoButton(x, y, PANEL_WIDTH, PANEL_HEIGHT) {
    var open = false
    var moduleButtons = ArrayList<AstolfoModuleButton>()
    val name = category.displayName
    var zLayer = 0
    private var dragged = false
    private var mouseX2 = 0
    private var mouseY2 = 0
    var hovered = false
    private val anim = CustomAnimation(200, 1.0, Direction.FORWARDS) { EaseUtils.easeInOutQuart(it) }
    private val reloadIcon = ResourceLocation("liquidbounce+/clickgui/reload.png")
    private val reloadArea: Rectangle
        get() = Rectangle(x + width - 24, y + 5, 8f, 8f)
    private val folderIcon = ResourceLocation("liquidbounce+/clickgui/folder.png")
    private val folderArea: Rectangle
        get() = Rectangle(x + width - 36, y + 5, 8f, 8f)

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
        drawRect(x, y + height, x + width, y + height + animHeight, BACKGROUND_MODULE)

        var used = 0f
        if (anim.output > 0) {
            GL11.glPushMatrix()
            scaleStart(this.y, this.y + this.height, 1f, animHeight/expectedHeight)
            val startY = y + height
            for (moduleButton in moduleButtons) {
                moduleButton.x = x
                moduleButton.y = startY + used
                val box = moduleButton.drawPanel(mouseX, mouseY)
                used += box.height
            }
            GL11.glPopMatrix()
        }


        if (moduleButtons.isNotEmpty() && anim.output > 0) {
            drawRect(x, y + height + used*anim.outputFloat, x + width, y + height + used*anim.outputFloat + add, BACKGROUND_MODULE)
        }

        drawBorderedRect(x, y, x + width, y + height + used*anim.outputFloat + add, 2f, color.rgb, Color(0, 0, 0, 0).rgb)

        if (category == ModuleCategory.SCRIPT) {
            drawImage(reloadIcon, reloadArea.x.toInt(), reloadArea.y.toInt(), reloadArea.width.toInt(), reloadArea.height.toInt(), if (reloadArea.contains(mouseX, mouseY)) 1f else 0.5f)
            drawImage(folderIcon, folderArea.x.toInt(), folderArea.y.toInt(), folderArea.width.toInt(), folderArea.height.toInt(), if (folderArea.contains(mouseX, mouseY)) 1f else 0.5f)
        }

//        if (anim.output > 0 && !AstolfoClickGui.shownDesc) {
//            for (moduleButton in moduleButtons) {
//                if (moduleButton.y + moduleButton.height < y + height + animHeight && moduleButton.isHovered(mouseX, mouseY)) {
//                    moduleButton.drawDescription(mouseX, mouseY)
//                }
//            }
//        }

        return Rectangle()
    }

    fun increaseZLayer() {
        zLayer = others.maxBy { it.zLayer }.zLayer + 1
    }

    override fun mouseAction(mouseX: Int, mouseY: Int, click: Boolean, button: Int): Boolean {
        if (category == ModuleCategory.SCRIPT && click) {
            if (reloadArea.contains(mouseX, mouseY)) {
                LiquidBounce.fileManager.saveConfigs(LiquidBounce.fileManager.clickGuiConfig)
                LiquidBounce.commandManager.executeCommands("${LiquidBounce.commandManager.prefix}scriptmanager reload")
                LiquidBounce.fileManager.loadConfig(LiquidBounce.fileManager.clickGuiConfig)
            }
            if (folderArea.contains(mouseX, mouseY)) {
                DesktopUtils.open(LiquidBounce.scriptManager.scriptsFolder)
            }
        }

        if (isHovered(mouseX, mouseY)) {
            if (click) {
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