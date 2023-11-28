/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.PotionUtils
import net.ccbluex.liquidbounce.utils.math.toRoman
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.FontValue
import net.minecraft.client.resources.I18n
import net.minecraft.potion.Potion
import org.lwjgl.opengl.GL11

/**
 * CustomHUD effects element
 *
 * Shows a list of active potion effects
 */
@ElementInfo(name = "Effects")
class Effects(x: Double = 2.0, y: Double = 10.0, scale: Float = 1F, side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)) : Element(x, y, scale, side) {

    private val fontValue = FontValue("Font", Fonts.minecraftNativeFont)
    private val icon = BoolValue("DrawIcon", true)
    private val numericPotency = BoolValue("NumericPotency", true)
    private val iconYOffset = FloatValue("IconYOffset", 0F, -10f, 10f) { icon.get() }
    private val shadow = BoolValue("Shadow", true)

    /**
     * Draw element
     */
    override fun drawElement(): Border {
        val fontRenderer = fontValue.get()

        var y = 0F
        var width = 0F

        assumeNonVolatile = true

        for (effect in mc.thePlayer.activePotionEffects) {
            if (side.vertical == Side.Vertical.DOWN)
                y -= fontRenderer.FONT_HEIGHT
            else if (side.vertical == Side.Vertical.UP)
                y += fontRenderer.FONT_HEIGHT

            val potion = Potion.potionTypes[effect.potionID]

            val number: String = if (numericPotency.get()) effect.amplifier.toString() else effect.amplifier.toRoman()

            val name = "${I18n.format(potion.name)} $number§f: ${Potion.getDurationString(effect)}"
            val stringWidth = fontRenderer.getStringWidth(name).toFloat()

            width = if (side.horizontal == Side.Horizontal.RIGHT)
                width.coerceAtMost(-stringWidth)
            else
                width.coerceAtLeast(stringWidth)


            when (side.horizontal) {
                Side.Horizontal.RIGHT -> fontRenderer.drawString(name, -stringWidth, y + if (side.vertical == Side.Vertical.UP) -fontRenderer.FONT_HEIGHT.toFloat() else 0F, potion.liquidColor, shadow.get())
                Side.Horizontal.LEFT, Side.Horizontal.MIDDLE -> fontRenderer.drawString(name, 0F, y + if (side.vertical == Side.Vertical.UP) -fontRenderer.FONT_HEIGHT.toFloat() else 0F, potion.liquidColor, shadow.get())
            }

            if (icon.get()) {
                val scale = fontValue.get().FONT_HEIGHT / 18f
                GL11.glPushMatrix()
                GL11.glTranslatef(
                    if (side.horizontal == Side.Horizontal.RIGHT) 2F else -12F,
                    y + if (side.vertical == Side.Vertical.UP) (-fontRenderer.FONT_HEIGHT -iconYOffset.get()) else (iconYOffset.get()),
                    0f)
                GL11.glScalef(scale, scale, scale)
                PotionUtils.drawPotionIcon(potion, 0f, 0f)
                GL11.glPopMatrix()
            }
        }

        assumeNonVolatile = false

        if (width == 0F)
            width = if (side.horizontal == Side.Horizontal.RIGHT) -40F else 40F

        if (y == 0F) // alr checked above
            y = if (side.vertical == Side.Vertical.UP) fontRenderer.FONT_HEIGHT.toFloat() else -fontRenderer.FONT_HEIGHT.toFloat()

        return Border(0F, 0F, width, y)
    }
}