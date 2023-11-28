/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 * 
 * This code belongs to WYSI-Foundation. Please give credits when using this in your repository.
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce.moduleManager
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.LiquidSlowly
import net.ccbluex.liquidbounce.utils.render.ColorUtils.fade
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

@ModuleInfo(name = "AsianHat", spacedName = "Asian Hat", description = "not your typical china hat", category = ModuleCategory.RENDER)
class AsianHat : Module() {
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Rainbow", "Sky", "LiquidSlowly", "Fade", "Mixer"), "Custom")
    private val colorRedValue = IntegerValue("Red", 255, 0, 255)
    private val colorGreenValue = IntegerValue("Green", 255, 0, 255)
    private val colorBlueValue = IntegerValue("Blue", 255, 0, 255)
    private val colorAlphaValue = IntegerValue("Alpha", 255, 0, 255)
    private val colorEndAlphaValue = IntegerValue("EndAlpha", 255, 0, 255)
    private val saturationValue = FloatValue("Saturation", 1f, 0f, 1f)
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)
    private val mixerSecondsValue = IntegerValue("Seconds", 2, 1, 10)
    private val spaceValue = IntegerValue("Color-Space", 0, 0, 100)
    private val hatBorder = BoolValue("HatBorder", true)
    private val hatRotation = BoolValue("HatRotation", true)
    private val target = BoolValue("Target", true)
    private val borderAlphaValue = IntegerValue("BorderAlpha", 255, 0, 255)
    private val borderWidthValue = FloatValue("BorderWidth", 1f, 0.1f, 4f)
    private val positions: MutableList<DoubleArray> = ArrayList()
    private var lastRadius = 0.0
    private fun checkPosition(radius: Double) {
        if (radius != lastRadius) {
            // generate new positions
            positions.clear()
            var i = 0
            while (i <= 360) {
                positions.add(doubleArrayOf(-sin(i * Math.PI / 180) * radius, cos(i * Math.PI / 180) * radius))
                i += 1
            }
        }
        lastRadius = radius
    }

    @EventTarget
    private fun onRender3D(event: Render3DEvent) {
        for (player in RenderConfig.entities()) {
            if (player === mc.thePlayer) {
                if (mc.gameSettings.thirdPersonView != 0) {
                    drawHat(event, player)
                }
            }
            if (target.value && player === moduleManager.getModule(KillAura::class.java)!!.target) {
                drawHat(event, player)
            }
        }
    }

    private fun drawHat(event: Render3DEvent, entity: Entity?) {
        if (entity == null) return
        val bb = entity.entityBoundingBox
        val partialTicks = event.partialTicks
        val radius = bb.maxX - bb.minX
        val height = bb.maxY - bb.minY
        val posX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks
        val posY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks
        val posZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks
        val viewX = -mc.renderManager.viewerPosX
        val viewY = -mc.renderManager.viewerPosY
        val viewZ = -mc.renderManager.viewerPosZ
        val colour = getColor(entity, 0)
        val r = colour.red / 255.0f
        val g = colour.green / 255.0f
        val b = colour.blue / 255.0f
        val al = colorAlphaValue.get() / 255.0f
        val eal = colorEndAlphaValue.get() / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        checkPosition(radius)
        GL11.glPushMatrix()
        GlStateManager.translate(viewX + posX, viewY + posY + height - 0.4, viewZ + posZ)
        pre3D()
        if (hatRotation.get()) {
            val rotMod = moduleManager.getModule(Rotations::class.java)
            var yaw = RenderUtils.interpolate(entity.prevRotationYaw, entity.rotationYaw, partialTicks)
            var pitch = RenderUtils.interpolate(entity.prevRotationPitch, entity.rotationPitch, partialTicks)
            if (rotMod != null && entity === mc.thePlayer) {
                yaw = if (RotationUtils.targetRotation != null) RotationUtils.targetRotation!!.yaw else RotationUtils.serverRotation.yaw
                pitch = if (RotationUtils.targetRotation != null) RotationUtils.targetRotation!!.pitch else RotationUtils.serverRotation.pitch
            }
            GlStateManager.rotate(-yaw, 0f, 1f, 0f)
            GlStateManager.rotate(pitch, 1f, 0f, 0f)
        }
        worldrenderer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_COLOR)

        // main section
        worldrenderer.pos(0.0, 0.7, 0.0).color(r, g, b, al).endVertex()
        var i = 0
        for (smolPos in positions) {
            if (spaceValue.get() > 0 && !colorModeValue.get().equals("Custom", ignoreCase = true)) {
                val colour2 = getColor(entity, i * spaceValue.get())
                val r2 = colour2.red / 255.0f
                val g2 = colour2.green / 255.0f
                val b2 = colour2.blue / 255.0f
                worldrenderer.pos(smolPos[0], 0.4, smolPos[1]).color(r2, g2, b2, eal).endVertex()
            } else {
                worldrenderer.pos(smolPos[0], 0.4, smolPos[1]).color(r, g, b, eal).endVertex()
            }
            i++
        }
        worldrenderer.pos(0.0, 0.7, 0.0).color(r, g, b, al).endVertex()
        tessellator.draw()

        // border section
        if (hatBorder.get()) {
            val lineAlp = borderAlphaValue.get() / 255.0f
            GL11.glLineWidth(borderWidthValue.get())
            worldrenderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR)
            i = 0
            for (smolPos in positions) {
                if (spaceValue.get() > 0 && !colorModeValue.get().equals("Custom", ignoreCase = true)) {
                    val colour2 = getColor(entity, i * spaceValue.get())
                    val r2 = colour2.red / 255.0f
                    val g2 = colour2.green / 255.0f
                    val b2 = colour2.blue / 255.0f
                    worldrenderer.pos(smolPos[0], 0.4, smolPos[1]).color(r2, g2, b2, lineAlp).endVertex()
                } else {
                    worldrenderer.pos(smolPos[0], 0.4, smolPos[1]).color(r, g, b, lineAlp).endVertex()
                }
                i++
            }
            tessellator.draw()
        }
        post3D()
        GL11.glPopMatrix()
    }

    fun getColor(ent: Entity?, index: Int): Color {
        return when (colorModeValue.get()) {
            "Custom" -> Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
            "Rainbow" -> Color(RenderUtils.getRainbowOpaque(mixerSecondsValue.get(), saturationValue.get(), brightnessValue.get(), index))
            "Sky" -> RenderUtils.skyRainbow(index, saturationValue.get(), brightnessValue.get())
            "LiquidSlowly" -> LiquidSlowly(System.nanoTime(), index, saturationValue.get(), brightnessValue.get())
            "Mixer" -> ColorMixer.getMixedColor(index, mixerSecondsValue.get())
            else -> fade(Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), index, 100)
        }
    }

    companion object {
        fun pre3D() {
            GL11.glPushMatrix()
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glShadeModel(GL11.GL_SMOOTH)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LIGHTING)
            GL11.glDepthMask(false)
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
            GL11.glDisable(2884)
        }

        fun post3D() {
            GL11.glDepthMask(true)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glPopMatrix()
            GL11.glColor4f(1f, 1f, 1f, 1f)
        }
    }
}
