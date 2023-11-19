/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.render.ColorUtils.LiquidSlowly
import net.ccbluex.liquidbounce.utils.render.ColorUtils.fade
import net.ccbluex.liquidbounce.utils.render.ColorUtils.reAlpha
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*

@ModuleInfo(name = "Breadcrumbs", description = "Leaves a trail behind you.", category = ModuleCategory.RENDER)
class Breadcrumbs : Module() {
    val unlimitedValue = BoolValue("Unlimited", false)
    val lineWidth = FloatValue("LineWidth", 1f, 1f, 10f)
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Rainbow", "Sky", "LiquidSlowly", "Fade", "Mixer"), "Custom")
    val colorRedValue = IntegerValue("R", 255, 0, 255) { colorModeValue.get().contains("Custom") || colorModeValue.get().contains("Fade") }
    val colorGreenValue = IntegerValue("G", 179, 0, 255) { colorModeValue.get().contains("Custom") || colorModeValue.get().contains("Fade") }
    val colorBlueValue = IntegerValue("B", 72, 0, 255) { colorModeValue.get().contains("Custom") || colorModeValue.get().contains("Fade") }
    private val saturationValue = FloatValue("Saturation", 1f, 0f, 1f) { !(colorModeValue.get().contains("Custom") || colorModeValue.get().contains("Fade")) }
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f) { !(colorModeValue.get().contains("Custom") || colorModeValue.get().contains("Fade")) }
    private val mixerSecondsValue = IntegerValue("Seconds", 2, 1, 10) { !(colorModeValue.get().contains("Custom") || colorModeValue.get().contains("Fade")) }
    val fadeSpeedValue = IntegerValue("Fade-Speed", 25, 0, 255)
    val colorRainbow = BoolValue("Rainbow", false)
    private val positions = LinkedList<Dot>()
    private var lastX = 0.0
    private var lastY = 0.0
    private var lastZ = 0.0
    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        val color = color
        synchronized(positions) {
            GL11.glPushMatrix()
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            mc.entityRenderer.disableLightmap()
            GL11.glLineWidth(lineWidth.get())
            GL11.glBegin(GL11.GL_LINE_STRIP)
            val renderPosX = mc.renderManager.viewerPosX
            val renderPosY = mc.renderManager.viewerPosY
            val renderPosZ = mc.renderManager.viewerPosZ
            val removeQueue: MutableList<Dot> = ArrayList()
            for (dot in positions) {
                if (dot.alpha > 0) dot.render(color, renderPosX, renderPosY, renderPosZ, if (unlimitedValue.get()) 0 else fadeSpeedValue.get()) else removeQueue.add(dot)
            }
            for (removeDot in removeQueue) positions.remove(removeDot)
            GL11.glColor4d(1.0, 1.0, 1.0, 1.0)
            GL11.glEnd()
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glPopMatrix()
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        synchronized(positions) {
            if (mc.thePlayer.posX != lastX || mc.thePlayer.entityBoundingBox.minY != lastY || mc.thePlayer.posZ != lastZ) {
                positions.add(Dot(doubleArrayOf(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY, mc.thePlayer.posZ)))
                lastX = mc.thePlayer.posX
                lastY = mc.thePlayer.entityBoundingBox.minY
                lastZ = mc.thePlayer.posZ
            }
        }
    }

    override fun onEnable() {
        if (mc.thePlayer == null) return
        synchronized(positions) {
            positions.add(
                Dot(
                    doubleArrayOf(
                        mc.thePlayer.posX,
                        mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight() * 0.5f,
                        mc.thePlayer.posZ
                    )
                )
            )
            positions.add(Dot(doubleArrayOf(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY, mc.thePlayer.posZ)))
        }
        super.onEnable()
    }

    override fun onDisable() {
        synchronized(positions) { positions.clear() }
        super.onDisable()
    }

    internal inner class Dot(private val pos: DoubleArray) {
        var alpha = 255
        fun render(color: Color?, renderPosX: Double, renderPosY: Double, renderPosZ: Double, decreaseBy: Int) {
            val reColor = reAlpha(color!!, alpha)
            RenderUtils.glColor(reColor)
            GL11.glVertex3d(pos[0] - renderPosX, pos[1] - renderPosY, pos[2] - renderPosZ)
            alpha -= decreaseBy
            if (alpha < 0) alpha = 0
        }
    }

    val color: Color
        get() = when (colorModeValue.get()) {
            "Custom" -> Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
            "Rainbow" -> Color(RenderUtils.getRainbowOpaque(mixerSecondsValue.get(), saturationValue.get(), brightnessValue.get(), 0))
            "Sky" -> RenderUtils.skyRainbow(0, saturationValue.get(), brightnessValue.get())
            "LiquidSlowly" -> LiquidSlowly(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get())
            "Mixer" -> ColorMixer.Companion.getMixedColor(0, mixerSecondsValue.get())
            else -> fade(Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), 0, 100)
        }
}
