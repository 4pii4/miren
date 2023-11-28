package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.ChangelogUtils
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.*
import java.awt.Color
import java.util.*
import kotlin.concurrent.schedule

class GuiMainMenu : GuiScreen(), GuiYesNoCallback {
    override fun initGui() {
        val defaultHeight = this.height / 3 + 30
        val defaultWidth = this.width / 2 - 60
        val buttonWidth = 120
        val buttonHeight = 20

        buttonList.run {
            add(GuiButton(0, defaultWidth, defaultHeight, buttonWidth, buttonHeight, "Single Player"))
            add(GuiButton(1, defaultWidth, defaultHeight + 25, buttonWidth, buttonHeight, "Multi Player"))
            add(GuiButton(2, defaultWidth, defaultHeight + 50, buttonWidth, buttonHeight, "Alt Manager"))
            add(GuiButton(3, defaultWidth, defaultHeight + 75, buttonWidth, buttonHeight, "Script Manager"))
            add(GuiButton(4, defaultWidth, defaultHeight + 100, buttonWidth, buttonHeight, "Background"))
            add(GuiButton(5, defaultWidth, defaultHeight + 125, buttonWidth, buttonHeight, "Game Options"))
            add(GuiButton(6, defaultWidth, defaultHeight + 150, buttonWidth, buttonHeight, "Quit Game"))
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        val fontLarge = Fonts.font72
        val name = "${LiquidBounce.CLIENT_NAME} ${LiquidBounce.CLIENT_VERSION}"
        fontLarge.drawCenteredString(name, this.width / 2f, this.height / 3f, -1)

        val font = Fonts.minecraftNativeFont
        val maxLength = font.getStringWidth(ChangelogUtils.changes.map { it }.maxByOrNull { font.getStringWidth(it) })
        val fontHeight = font.FONT_HEIGHT

        if (maxLength <= width * 0.45f) {
            font.drawStringWithShadow(ChangelogUtils.buildMsg, 5f, 5f, Color(255, 255, 255, 220).rgb)
            for (i in ChangelogUtils.changes.indices) {
                val change = ChangelogUtils.changes[i]
                font.drawStringWithShadow(change, 9f, 17f + i * fontHeight, -1)
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks)
    }


    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(GuiSelectWorld(this))
            1 -> mc.displayGuiScreen(GuiMultiplayer(this))
            2 -> mc.displayGuiScreen(GuiAltManager(this))
            3 -> mc.displayGuiScreen(GuiScripts(this))
            4 -> mc.displayGuiScreen(GuiBackground(this))
            5 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            6 -> mc.shutdown()
        }
    }
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        when (typedChar) {
            's' -> mc.displayGuiScreen(GuiSelectWorld(this))
            'm' -> mc.displayGuiScreen(GuiMultiplayer(this))
            'a' -> mc.displayGuiScreen(GuiAltManager(this))
            'o' -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            'b' -> mc.displayGuiScreen(GuiBackground(this))
        }
    }
}