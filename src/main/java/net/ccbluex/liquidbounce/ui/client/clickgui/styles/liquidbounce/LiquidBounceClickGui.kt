package net.ccbluex.liquidbounce.ui.client.clickgui.styles.liquidbounce

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUI
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.ClickGuiStyle
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.EaseUtils.easeOutBack
import net.ccbluex.liquidbounce.utils.render.EaseUtils.easeOutQuart
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Mouse
import java.io.IOException


class LiquidBounceClickGui : ClickGuiStyle("LiquidBounce") {
    val panels: MutableList<Panel> = ArrayList()
    private val hudIcon = ResourceLocation("liquidbounce+/custom_hud_icon.png")
    val style: LiquidBounceStyle
        get() = LiquidBounceStyle.getInstance()

    private var clickedPanel: Panel? = null
    private var mouseX = 0
    private var mouseY = 0
    var slide = 0.0
    var progress = 0.0
    var lastMS = System.currentTimeMillis()

    init {
        val width = 100
        val height = 18
        var yPos = 5
        for (category in ModuleCategory.entries) {
            panels.add(object : Panel(category.displayName, 100, yPos, width, height, false) {
                override fun setupItems() {
                    elements.addAll(LiquidBounce.moduleManager.getModuleInCategory(category).map { ModuleElement(it) })
                }
            })
            yPos += 20
        }

    }

    override fun initGui() {
        progress = 0.0
        slide = progress
        lastMS = System.currentTimeMillis()
        super.initGui()
    }

    override fun dumpConfig(): JsonElement {
        val jsonObject = JsonObject()

        for (panel in panels) {
            val panelObject = JsonObject()
            panelObject.addProperty("open", panel.open)
            panelObject.addProperty("visible", panel.isVisible)
            panelObject.addProperty("posX", panel.x)
            panelObject.addProperty("posY", panel.y)
            for (element in panel.elements) {
                if (element !is ModuleElement) continue
                val elementObject = JsonObject()
                elementObject.addProperty("Settings", element.isShowSettings)
                panelObject.add(element.module.name, elementObject)
            }
            jsonObject.add(panel.name, panelObject)
        }

        return jsonObject
    }

    override fun loadConfig(json: JsonObject) {
        for (panel in panels) {
            if (!json.has(panel.name)) continue
            try {
                val panelObject: JsonObject = json.getAsJsonObject(panel.name)
                panel.open = panelObject["open"].asBoolean
                panel.isVisible = panelObject["visible"].asBoolean
                panel.x = panelObject["posX"].asInt
                panel.y = panelObject["posY"].asInt
                for (element in panel.elements) {
                    if (element !is ModuleElement) continue
                    if (!panelObject.has(element.module.name)) continue
                    try {
                        val elementObject = panelObject.getAsJsonObject(element.module.name)
                        element.isShowSettings = elementObject["Settings"].asBoolean
                    } catch (e: Exception) {
                        ClientUtils.logger.error("Error while loading clickgui module element with the name '" + element.module.name + "' (Panel Name: " + panel.name + ").", e)
                    }
                }
            } catch (e: Exception) {
                ClientUtils.logger.error("Error while loading clickgui panel with the name '" + panel.name + "'.", e)
            }
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        var mouseX = mouseX
        var mouseY = mouseY
        if (progress < 1) progress = ((System.currentTimeMillis() - lastMS).toFloat() / (500f / ClickGUI.animSpeed)).toDouble() // fully fps async
        else progress = 1.0
        when (ClickGUI.animationValue.lowercase()) {
            "slidebounce", "zoombounce" -> slide = easeOutBack(progress)
            "slide", "zoom", "azura" -> slide = easeOutQuart(progress)
            "none" -> slide = 1.0
        }
        if (Mouse.isButtonDown(0) && mouseX >= 5 && mouseX <= 50 && mouseY <= height - 5 && mouseY >= height - 50) mc.displayGuiScreen(GuiHudDesigner())

        // Enable DisplayList optimization
        assumeNonVolatile = true
        val scale: Double = ClickGUI.scale.toDouble()
        mouseX = (mouseX / scale).toInt()
        mouseY = (mouseY / scale).toInt()
        this.mouseX = mouseX
        this.mouseY = mouseY
        when (ClickGUI.backgroundValue.get().lowercase()) {
            "Default" -> drawDefaultBackground()
            "Gradient" -> drawGradientRect(
                0, 0, width, height,
                ColorUtils.reAlpha(ClickGUI.accentColor, ClickGUI.gradStartValue.get()).rgb,
                ColorUtils.reAlpha(ClickGUI.accentColor,ClickGUI.gradEndValue.get()).rgb
            )

            else -> {}
        }
        GlStateManager.disableAlpha()
        RenderUtils.drawImage(hudIcon, 9, height - 41, 32, 32)
        GlStateManager.enableAlpha()
        when (ClickGUI.animationValue.lowercase()) {
            "azura" -> {
                GlStateManager.translate(0.0, (1.0 - slide) * height * 2.0, 0.0)
                GlStateManager.scale(scale, scale + (1.0 - slide) * 2.0, scale)
            }

            "slide", "slidebounce" -> {
                GlStateManager.translate(0.0, (1.0 - slide) * height * 2.0, 0.0)
                GlStateManager.scale(scale, scale, scale)
            }

            "zoom" -> {
                GlStateManager.translate((1.0 - slide) * (width / 2.0), (1.0 - slide) * (height / 2.0), (1.0 - slide) * (width / 2.0))
                GlStateManager.scale(scale * slide, scale * slide, scale * slide)
            }

            "zoombounce" -> {
                GlStateManager.translate((1.0 - slide) * (width / 2.0), (1.0 - slide) * (height / 2.0), 0.0)
                GlStateManager.scale(scale * slide, scale * slide, scale * slide)
            }

            "none" -> GlStateManager.scale(scale, scale, scale)
        }
        for (panel in panels) {
            panel.updateFade(RenderUtils.deltaTime)
            panel.drawScreen(mouseX, mouseY, partialTicks)
        }
        for (panel in panels) {
            for (element in panel.elements) {
                if (element is ModuleElement) {
                    val moduleElement = element
                    if (mouseX != 0 && mouseY != 0 && moduleElement.isHovering(mouseX, mouseY) && moduleElement.isVisible && element.y <= panel.y + panel.fade) style.drawDescription(mouseX, mouseY, moduleElement)
                }
            }
        }
        GlStateManager.disableLighting()
        RenderHelper.disableStandardItemLighting()
        when (ClickGUI.animationValue.lowercase()) {
            "azura" -> GlStateManager.translate(0.0, (1.0 - slide) * height * -2.0, 0.0)
            "slide", "slidebounce" -> GlStateManager.translate(0.0, (1.0 - slide) * height * -2.0, 0.0)
            "zoom" -> GlStateManager.translate(-1 * (1.0 - slide) * (width / 2.0), -1 * (1.0 - slide) * (height / 2.0), -1 * (1.0 - slide) * (width / 2.0))
            "zoombounce" -> GlStateManager.translate(-1 * (1.0 - slide) * (width / 2.0), -1 * (1.0 - slide) * (height / 2.0), 0.0)
        }
        GlStateManager.scale(1f, 1f, 1f)
        assumeNonVolatile = false
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    @Throws(IOException::class)
    override fun handleMouseInput() {
        super.handleMouseInput()
        val wheel: Int = Mouse.getEventDWheel()
        for (i in panels.indices.reversed()) if (panels[i].handleScroll(mouseX, mouseY, wheel)) break
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        var mouseX = mouseX
        var mouseY = mouseY
        val scale: Double = ClickGUI.scale.toDouble()
        mouseX = (mouseX / scale).toInt()
        mouseY = (mouseY / scale).toInt()
        for (i in panels.indices.reversed()) {
            if (panels[i].mouseClicked(mouseX, mouseY, mouseButton)) {
                break
            }
        }
        for (panel in panels) {
            panel.drag = false
            if (mouseButton == 0 && panel.isHovering(mouseX, mouseY)) {
                clickedPanel = panel
                break
            }
        }
        if (clickedPanel != null) {
            clickedPanel!!.x2 = clickedPanel!!.x - mouseX
            clickedPanel!!.y2 = clickedPanel!!.y - mouseY
            clickedPanel!!.drag = true
            panels.remove(clickedPanel)
            panels.add(clickedPanel!!)
            clickedPanel = null
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        var mouseX = mouseX
        var mouseY = mouseY
        val scale: Double = ClickGUI.scale.toDouble()
        mouseX = (mouseX / scale).toInt()
        mouseY = (mouseY / scale).toInt()
        for (panel in panels) {
            panel.mouseReleased(mouseX, mouseY, state)
        }
        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun updateScreen() {
        for (panel in panels) {
            for (element in panel.elements) {
                if (element is ButtonElement) {
                    val buttonElement = element
                    if (buttonElement.isHovering(mouseX, mouseY)) {
                        if (buttonElement.hoverTime < 7) buttonElement.hoverTime++
                    } else if (buttonElement.hoverTime > 0) buttonElement.hoverTime--
                }
                if (element is ModuleElement) {
                    if (element.module.state) {
                        if (element.slowlyFade < 255) element.slowlyFade += 50
                    } else if (element.slowlyFade > 0) element.slowlyFade -= 50
                    if (element.slowlyFade > 255) element.slowlyFade = 255
                    if (element.slowlyFade < 0) element.slowlyFade = 0
                }
            }
        }
        super.updateScreen()
    }

    override fun onGuiClosed() {
        LiquidBounce.fileManager.saveConfig(LiquidBounce.fileManager.clickGuiConfig)
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    companion object {
        private var instance: LiquidBounceClickGui? = null
        fun getInstance(): LiquidBounceClickGui {
            return if (instance == null) LiquidBounceClickGui().also { instance = it } else instance!!
        }

        fun resetInstance() {
            instance = LiquidBounceClickGui()
        }
    }
}