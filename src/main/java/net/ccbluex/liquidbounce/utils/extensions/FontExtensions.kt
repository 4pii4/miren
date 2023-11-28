package net.ccbluex.liquidbounce.utils.extensions

import net.minecraft.client.gui.FontRenderer
import org.lwjgl.opengl.GL11

fun FontRenderer.drawScaledString(text: String, x: Float, y: Float, color: Int, scale: Float = 1f, shadow: Boolean = true) {
    GL11.glPushMatrix()
    GL11.glTranslatef(x, y, 0f)
    GL11.glScalef(scale, scale, scale)
    drawString(text, 0f, 0f, color, shadow)
    GL11.glPopMatrix()
}