package net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUI
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.ClickGuiStyle
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.buttons.AstolfoCategoryPanel
import net.ccbluex.liquidbounce.utils.ClientUtils
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.roundToInt

/**
 * Astolfo Click GUI
 *
 * Designed in a way that only this class know about scale and scrolling.
 * @author pii4
 * @property panels the categories
 * @property scale scale factor
 * @property scrollAmount scroll amount
 */
class AstolfoClickGui : ClickGuiStyle("Astolfo") {
    private var panels = ArrayList<AstolfoCategoryPanel>()
    private val scale: Float
        get() = ClickGUI.scale
    private val scrollAmount: Float
        get() = ClickGUI.scroll

    private var pressed = mutableMapOf("UP" to false, "DOWN" to false, "LEFT" to false, "RIGHT" to false)

    private fun updatePressed() {
        pressed["UP"] = Keyboard.isKeyDown(Keyboard.KEY_UP)
        pressed["DOWN"] = Keyboard.isKeyDown(Keyboard.KEY_DOWN)
        pressed["LEFT"] = Keyboard.isKeyDown(Keyboard.KEY_LEFT)
        pressed["RIGHT"] = Keyboard.isKeyDown(Keyboard.KEY_RIGHT)
    }

    init {
        var xPos = 4f
        for (cat in ModuleCategory.entries) {
            panels.add(AstolfoCategoryPanel(xPos, 4f, cat, Color(cat.color), panels))
            xPos += AstolfoConstants.PANEL_WIDTH.toInt() + 10
        }

    }

    private fun handleScrolling() {
        if (Mouse.hasWheel() && panels.count { it.hovered } == 0) {
            val wheel = Mouse.getDWheel()
            val scrollAmount = scrollAmount * (if (wheel > 0) 1 else if (wheel < 0) -1 else 0)
            panels.map { it.y += scrollAmount }
        }
    }

    override fun onGuiClosed() {
        for (panel in panels) {
            panel.onClosed()
            for (moduleButton in panel.moduleButtons) {
                moduleButton.onClosed()
                for (valueButton in moduleButton.valueButtons) {
                    valueButton.onClosed()
                }
            }
        }
        LiquidBounce.fileManager.saveConfigs(LiquidBounce.fileManager.clickGuiConfig)
    }

    override fun drawScreen(mouseXIn: Int, mouseYIn: Int, partialTicks: Float) {
        val mouseX = (mouseXIn / scale).roundToInt()
        val mouseY = (mouseYIn / scale).roundToInt()

        ClickGUI.drawBackground(this)
        GL11.glPushMatrix()
        GL11.glScalef(scale, scale, scale)

        drawRect(0, 0, mc.currentScreen.width, mc.currentScreen.height, Color(0, 0, 0, 50).rgb)

        if (Keyboard.isKeyDown(Keyboard.KEY_UP) && !pressed["UP"]!!) panels.map { it.y -= scrollAmount }
        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) && !pressed["DOWN"]!!) panels.map { it.y += scrollAmount }
        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) && !pressed["LEFT"]!!) panels.map { it.x -= scrollAmount }
        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT) && !pressed["RIGHT"]!!) panels.map { it.x += scrollAmount }

        if (Keyboard.isKeyDown(Keyboard.KEY_F10)) panels.mapIndexed { index, panel -> panel.x = 10 + (10 + AstolfoConstants.PANEL_WIDTH) * index; panel.y = 10f }

        shownDesc = false
        for (catPanel in panels.sortedBy { it.zLayer }) {
            catPanel.drawPanel(mouseX, mouseY)
        }

        GL11.glPopMatrix()
        updatePressed()
        handleScrolling()
    }

    private fun mouseAction(mouseXIn: Int, mouseYIn: Int, mouseButton: Int, click: Boolean) {
        fun incz(panel: AstolfoCategoryPanel) {
            if (click)
                panel.increaseZLayer()
        }
        val mouseX = (mouseXIn / scale).roundToInt()
        val mouseY = (mouseYIn / scale).roundToInt()

        loop@ for (panel in panels.sortedBy { -it.zLayer }) {
            if (panel.mouseAction(mouseX, mouseY, click, mouseButton)) {
                incz(panel)
                break@loop
            }
            if (panel.open) {
                for (moduleButton in panel.moduleButtons) {
                    if (moduleButton.mouseAction(mouseX, mouseY, click, mouseButton)) {
                        incz(panel)
                        break@loop
                    }
                    if (moduleButton.open) {
                        for (pan in moduleButton.valueButtons) {
                            if (pan.mouseAction(mouseX, mouseY, click, mouseButton)) {
                                incz(panel)
                                break@loop
                            }
                        }
                    }
                }
            }
        }
    }

    override fun mouseClicked(mouseXIn: Int, mouseYIn: Int, mouseButton: Int) {
        mouseAction(mouseXIn, mouseYIn, mouseButton, true)
    }

    override fun mouseReleased(mouseXIn: Int, mouseYIn: Int, mouseButton: Int) {
        mouseAction(mouseXIn, mouseYIn, mouseButton, false)
    }

    override fun loadConfig(json: JsonObject) {
        for (panel in panels) {
            if (!json.has(panel.category.displayName)) continue
            try {
                val panelObject = json.getAsJsonObject(panel.name)
                panel.open = panelObject["open"].asBoolean
                panel.x = panelObject["posX"].asFloat
                panel.y = panelObject["posY"].asFloat
                for (moduleElement in panel.moduleButtons) {
                    if (!panelObject.has(moduleElement.module.name)) continue
                    try {
                        val elementObject = panelObject.getAsJsonObject(moduleElement.module.name)
                        moduleElement.open = elementObject["Settings"].asBoolean
                    } catch (e: Exception) {
                        ClientUtils.logger.error("Error while loading clickgui module element with the name '" + moduleElement.module.name + "' (Panel Name: " + panel.name + ").", e)
                    }
                }
            } catch (e: Exception) {
                ClientUtils.logger.error("Error while loading clickgui panel with the name ${panel.name}: e")
            }
        }
    }

    override fun dumpConfig(): JsonElement {
        val jsonObject = JsonObject()

        for (panel in panels) {
            val panelObject = JsonObject()
            panelObject.addProperty("open", panel.open)
            panelObject.addProperty("visible", true)
            panelObject.addProperty("posX", panel.x)
            panelObject.addProperty("posY", panel.y)
            for (moduleElement in panel.moduleButtons) {
                val elementObject = JsonObject()
                elementObject.addProperty("Settings", moduleElement.open)
                panelObject.add(moduleElement.module.name, elementObject)
            }
            jsonObject.add(panel.name, panelObject)
        }

        return jsonObject
    }

    override fun getNewInstance(): AstolfoClickGui {
        return AstolfoClickGui()
    }

    companion object {
        var shownDesc = false
    }
}
