package net.ccbluex.liquidbounce.ui.client.clickgui.styles.liquidbounce

import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Mouse


class ModuleElement(module: Module) : ButtonElement(null) {
    val module: Module
    var isShowSettings = false
    var settingsWidth = 0f
    private var wasPressed = false
    var slowlySettingsYPos = 0
    var slowlyFade = 0

    init {
        displayName = module.name
        this.module = module
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, button: Float) {
        LiquidBounceStyle.getInstance().drawModuleElement(mouseX, mouseY, this)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovering(mouseX, mouseY) && isVisible) {
            module.toggle()
            return true
            //mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F)); duplicated lol
        }
        if (mouseButton == 1 && isHovering(mouseX, mouseY) && isVisible) {
            isShowSettings = !isShowSettings
            mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("gui.button.press"), 1.0f))
            return true
        }
        return false
    }

    fun isntPressed(): Boolean {
        return !wasPressed
    }

    fun updatePressed() {
        wasPressed = Mouse.isButtonDown(0)
    }
}