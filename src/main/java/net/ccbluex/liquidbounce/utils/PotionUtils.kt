package net.ccbluex.liquidbounce.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.potion.Potion
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

object PotionUtils {

    private val inventoryBackground = ResourceLocation("textures/gui/container/inventory.png")

    fun drawPotionIcon(potion: Potion, x: Float, y: Float) {
        val iconIndex = potion.statusIconIndex
        val mc = Minecraft.getMinecraft()

        GL11.glPushMatrix()

        GL11.glDisable(2929)
        GL11.glEnable(3042)
        GL11.glDepthMask(false)
        OpenGlHelper.glBlendFunc(770, 771, 1, 0)
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)

        mc.textureManager.bindTexture(inventoryBackground)
        mc.ingameGUI.drawTexturedModalRect(x, y, iconIndex % 8 * 18, 198 + iconIndex / 8 * 18, 18, 18)
        GL11.glDepthMask(true)
        GL11.glDisable(3042)
        GL11.glEnable(2929)
        GL11.glPopMatrix()
    }
}