/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 * 
 * This code belongs to WYSI-Foundation. Please give credits when using this in your repository.
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.LiquidSlowly
import net.ccbluex.liquidbounce.utils.render.ColorUtils.fade
import net.ccbluex.liquidbounce.utils.render.ColorUtils.reAlpha
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color

@ModuleInfo(name = "Crosshair", description = "The CS:GO.", category = ModuleCategory.RENDER)
class Crosshair : Module() {
    //Color
    var colorModeValue = ListValue("Color", arrayOf("Custom", "Rainbow", "LiquidSlowly", "Sky", "Fade", "Mixer"), "Custom")
    var colorRedValue = IntegerValue("Red", 0, 0, 255)
    var colorGreenValue = IntegerValue("Green", 0, 0, 255)
    var colorBlueValue = IntegerValue("Blue", 0, 0, 255)
    var colorAlphaValue = IntegerValue("Alpha", 255, 0, 255)

    //Rainbow thingy
    private val saturationValue = FloatValue("Saturation", 1f, 0f, 1f)
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)
    private val mixerSecondsValue = IntegerValue("Seconds", 2, 1, 10)

    //Size, width, hitmarker
    var widthVal = FloatValue("Width", 2f, 0.25f, 10f)
    var sizeVal = FloatValue("Size/Length", 7f, 0.25f, 15f)
    var gapVal = FloatValue("Gap", 5f, 0.25f, 15f)
    var dynamicVal = BoolValue("Dynamic", true)
    var hitMarkerVal = BoolValue("HitMarker", true)
    var noVanillaCH = BoolValue("NoVanillaCrossHair", true)
    @EventTarget
    fun onRender2D(event: Render2DEvent?) {
        val scaledRes = ScaledResolution(mc)
        val width = widthVal.get()
        val size = sizeVal.get()
        val gap = gapVal.get()
        GL11.glPushMatrix()
        RenderUtils.drawBorderedRect(scaledRes.scaledWidth / 2f - width, scaledRes.scaledHeight / 2f - gap - size - if (isMoving) 2 else 0, scaledRes.scaledWidth / 2f + 1.0f + width, scaledRes.scaledHeight / 2f - gap - if (isMoving) 2 else 0, 0.5f, Color(0, 0, 0, colorAlphaValue.get()).rgb, crosshairColor.rgb)
        RenderUtils.drawBorderedRect(scaledRes.scaledWidth / 2f - width, scaledRes.scaledHeight / 2f + gap + 1 + (if (isMoving) 2 else 0) - 0.15f, scaledRes.scaledWidth / 2f + 1.0f + width, scaledRes.scaledHeight / 2f + 1 + gap + size + (if (isMoving) 2 else 0) - 0.15f, 0.5f, Color(0, 0, 0, colorAlphaValue.get()).rgb, crosshairColor.rgb)
        RenderUtils.drawBorderedRect(scaledRes.scaledWidth / 2f - gap - size - (if (isMoving) 2 else 0) + 0.15f, scaledRes.scaledHeight / 2f - width, scaledRes.scaledWidth / 2f - gap - (if (isMoving) 2 else 0) + 0.15f, scaledRes.scaledHeight / 2 + 1.0f + width, 0.5f, Color(0, 0, 0, colorAlphaValue.get()).rgb, crosshairColor.rgb)
        RenderUtils.drawBorderedRect(scaledRes.scaledWidth / 2f + 1 + gap + if (isMoving) 2 else 0, scaledRes.scaledHeight / 2f - width, scaledRes.scaledWidth / 2f + size + gap + 1.0f + if (isMoving) 2 else 0, scaledRes.scaledHeight / 2 + 1.0f + width, 0.5f, Color(0, 0, 0, colorAlphaValue.get()).rgb, crosshairColor.rgb)
        GL11.glPopMatrix()
        GlStateManager.resetColor()
        //glColor4f(0F, 0F, 0F, 0F)
        val target = LiquidBounce.moduleManager.getModule(KillAura::class.java)!!.target
        if (hitMarkerVal.get() && target != null && target.hurtTime > 0) {
            GL11.glPushMatrix()
            GlStateManager.enableBlend()
            GlStateManager.disableTexture2D()
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
            GL11.glColor4f(1f, 1f, 1f, target.hurtTime.toFloat() / target.maxHurtTime.toFloat())
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glLineWidth(1f)
            GL11.glBegin(3)
            GL11.glVertex2f(scaledRes.scaledWidth / 2f + gap, scaledRes.scaledHeight / 2f + gap)
            GL11.glVertex2f(scaledRes.scaledWidth / 2f + gap + size, scaledRes.scaledHeight / 2f + gap + size)
            GL11.glEnd()
            GL11.glBegin(3)
            GL11.glVertex2f(scaledRes.scaledWidth / 2f - gap, scaledRes.scaledHeight / 2f - gap)
            GL11.glVertex2f(scaledRes.scaledWidth / 2f - gap - size, scaledRes.scaledHeight / 2f - gap - size)
            GL11.glEnd()
            GL11.glBegin(3)
            GL11.glVertex2f(scaledRes.scaledWidth / 2f - gap, scaledRes.scaledHeight / 2f + gap)
            GL11.glVertex2f(scaledRes.scaledWidth / 2f - gap - size, scaledRes.scaledHeight / 2f + gap + size)
            GL11.glEnd()
            GL11.glBegin(3)
            GL11.glVertex2f(scaledRes.scaledWidth / 2f + gap, scaledRes.scaledHeight / 2f - gap)
            GL11.glVertex2f(scaledRes.scaledWidth / 2f + gap + size, scaledRes.scaledHeight / 2f - gap - size)
            GL11.glEnd()
            GlStateManager.enableTexture2D()
            GlStateManager.disableBlend()
            GL11.glPopMatrix()
        }
    }

    private val isMoving: Boolean
        private get() = dynamicVal.get() && MovementUtils.isMoving
    private val crosshairColor: Color
        private get() = when (colorModeValue.get()) {
            "Custom" -> Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), colorAlphaValue.get())
            "Rainbow" -> Color(RenderUtils.getRainbowOpaque(mixerSecondsValue.get(), saturationValue.get(), brightnessValue.get(), 0))
            "Sky" -> reAlpha(RenderUtils.skyRainbow(0, saturationValue.get(), brightnessValue.get()), colorAlphaValue.get())
            "LiquidSlowly" -> reAlpha(LiquidSlowly(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get()), colorAlphaValue.get())
            "Mixer" -> reAlpha(ColorMixer.getMixedColor(0, mixerSecondsValue.get()), colorAlphaValue.get())
            else -> reAlpha(fade(Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), 0, 100), colorAlphaValue.get())
        }
}