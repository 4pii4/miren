package net.ccbluex.liquidbounce.utils.extensions

import net.minecraft.client.gui.FontRenderer
import org.lwjgl.opengl.GL11

fun FontRenderer.drawScaledString(text: String, x: Number, y: Number, color: Int, scale: Float = 1f, shadow: Boolean = true) {
    GL11.glPushMatrix()
    GL11.glTranslatef(x.toFloat(), y.toFloat(), 0f)
    GL11.glScalef(scale, scale, scale)
    drawString(text, 0f, 0f, color, shadow)
    GL11.glPopMatrix()
}

fun FontRenderer.drawXYCenteredString(text: String, x: Number, y: Number, color: Int, shadow: Boolean = true) {
    val w = getStringWidth(text)
    val h = FONT_HEIGHT
    drawString(text, x.toFloat() - w / 2, y.toFloat() - h / 2, color, shadow)
}