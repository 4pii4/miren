package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.GLUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * CustomHUD Inventory element
 *
 * Shows a horizontal display of current inventory
 */
@ElementInfo(name = "Inventory")
class Inventory(x: Double = 10.0, y: Double = 10.0, scale: Float = 1F) : Element(x, y, scale) {
    private val backgroundMode = ListValue("BackgroundMode", arrayOf("Solid", "Textured", "None"), "Textured")
    private val bgredValue = IntegerValue("Background-Red", 0, 0, 255) { backgroundMode.isMode("Solid") }
    private val bggreenValue = IntegerValue("Background-Green", 0, 0, 255) { backgroundMode.isMode("Solid") }
    private val bgblueValue = IntegerValue("Background-Blue", 0, 0, 255) { backgroundMode.isMode("Solid") }
    private val bgalphaValue = IntegerValue("Background-Alpha", 120, 0, 255) { backgroundMode.isMode("Solid") }

    private val inventoryBackground = ResourceLocation("textures/gui/container/inventory.png")

    override fun drawElement(): Border {
        when (backgroundMode.get()) {
            "Solid" -> {
                RenderUtils.drawRect(0f, 0f, 163f, 54f, Color(bgredValue.get(), bggreenValue.get(), bgblueValue.get(), bgalphaValue.get()).rgb)
            }

            "Textured" -> {
                mc.textureManager.bindTexture(inventoryBackground)
                mc.ingameGUI.drawTexturedModalRect(0, 0, 6, 83, 163, 54)
            }
        }

        val size = 18
        for (index in mc.thePlayer.inventory.mainInventory.indices) {
            if (index < 9) continue
            val stack = mc.thePlayer.inventory.mainInventory[index]
            GlStateManager.resetColor()
            GL11.glPushMatrix()
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
            if (mc.theWorld != null) GLUtils.enableGUIStandardItemLighting()
            GlStateManager.pushMatrix()
            GlStateManager.disableAlpha()
            GlStateManager.clear(256)
            mc.renderItem.renderItemAndEffectIntoGUI(stack, 1 + index % 9 * size,  1 + (index / 9 - 1) * size)
            mc.renderItem.renderItemOverlays(Fonts.minecraftFont, stack, 1 + index % 9 * size, 1 + (index / 9 - 1) * size)
            if (mc.theWorld != null) GLUtils.disableStandardItemLighting()
            GlStateManager.enableAlpha()
            GlStateManager.popMatrix()
            GL11.glPopMatrix()
        }

        return Border(0f, 0f, 163f, 54f)
    }

}
