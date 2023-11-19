/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer.Companion.getColorIndex
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.render.BlendUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.LiquidSlowly
import net.ccbluex.liquidbounce.utils.render.ColorUtils.fade
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.WorldToScreen
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GlowShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.OutlineShader
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector3f
import java.awt.Color
import java.text.DecimalFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

@ModuleInfo(name = "ESP", description = "Allows you to see targets through walls.", category = ModuleCategory.RENDER)
class ESP : Module() {
    private val decimalFormat = DecimalFormat("0.0")
	val modeValue = ListValue("Mode", arrayOf("Box", "OtherBox", "WireFrame", "2D", "Real2D", "Outline", "ShaderOutline", "ShaderGlow"), "Box")
    private val real2dcsgo = BoolValue("2D-CSGOStyle", true) { modeValue.get().equals("real2d", ignoreCase = true) }
    private val real2dShowHealth = BoolValue("2D-ShowHealth", true) { modeValue.get().equals("real2d", ignoreCase = true) }
    private val real2dShowHeldItem = BoolValue("2D-ShowHeldItem", true) { modeValue.get().equals("real2d", ignoreCase = true) }
    private val real2dShowName = BoolValue("2D-ShowEntityName", true) { modeValue.get().equals("real2d", ignoreCase = true) }
    private val real2dOutline = BoolValue("2D-Outline", true) { modeValue.get().equals("real2d", ignoreCase = true) }
	val outlineWidth = FloatValue("Outline-Width", 3f, 0.5f, 5f) { modeValue.get().equals("outline", ignoreCase = true) }
	val wireframeWidth = FloatValue("WireFrame-Width", 2f, 0.5f, 5f) { modeValue.get().equals("wireframe", ignoreCase = true) }
    private val shaderOutlineRadius = FloatValue("ShaderOutline-Radius", 1.35f, 1f, 2f, "x") { modeValue.get().equals("shaderoutline", ignoreCase = true) }
    private val shaderGlowRadius = FloatValue("ShaderGlow-Radius", 2.3f, 2f, 3f, "x") { modeValue.get().equals("shaderglow", ignoreCase = true) }
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Health", "Rainbow", "Sky", "LiquidSlowly", "Fade", "Mixer"), "Custom")
    private val colorRedValue = IntegerValue("Red", 255, 0, 255)
    private val colorGreenValue = IntegerValue("Green", 255, 0, 255)
    private val colorBlueValue = IntegerValue("Blue", 255, 0, 255)
    private val saturationValue = FloatValue("Saturation", 1f, 0f, 1f)
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)
    private val mixerSecondsValue = IntegerValue("Seconds", 2, 1, 10)
    private val colorTeam = BoolValue("Team", false)
    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        val mode = modeValue.get()
        val mvMatrix = WorldToScreen.getMatrix(GL11.GL_MODELVIEW_MATRIX)
        val projectionMatrix = WorldToScreen.getMatrix(GL11.GL_PROJECTION_MATRIX)
        val real2d = mode.equals("real2d", ignoreCase = true)
        if (real2d) {
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glPushMatrix()
            GL11.glLoadIdentity()
            GL11.glOrtho(0.0, mc.displayWidth.toDouble(), mc.displayHeight.toDouble(), 0.0, -1.0, 1.0)
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glPushMatrix()
            GL11.glLoadIdentity()
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GlStateManager.enableTexture2D()
            GlStateManager.depthMask(true)
            GL11.glLineWidth(1.0f)
        }
        for (entity in RenderConfig.entities()) {
            if (entity !== mc.thePlayer && RenderUtils.isInViewFrustrum(entity)) {
                val entityLiving = entity
                val color = getColor(entityLiving)
                when (mode.lowercase(Locale.getDefault())) {
                    "box", "otherbox" -> RenderUtils.drawEntityBox(entity, color, !mode.equals("otherbox", ignoreCase = true))
                    "2d" -> {
                        val renderManager = mc.renderManager
                        val timer = mc.timer
                        val posX = entityLiving.lastTickPosX + (entityLiving.posX - entityLiving.lastTickPosX) * timer.renderPartialTicks - renderManager.renderPosX
                        val posY = entityLiving.lastTickPosY + (entityLiving.posY - entityLiving.lastTickPosY) * timer.renderPartialTicks - renderManager.renderPosY
                        val posZ = entityLiving.lastTickPosZ + (entityLiving.posZ - entityLiving.lastTickPosZ) * timer.renderPartialTicks - renderManager.renderPosZ
                        RenderUtils.draw2D(entityLiving, posX, posY, posZ, color.rgb, Color.BLACK.rgb)
                    }

                    "real2d" -> {
                        val renderManager = mc.renderManager
                        val timer = mc.timer
                        val bb = entityLiving.entityBoundingBox
                            .offset(-entityLiving.posX, -entityLiving.posY, -entityLiving.posZ)
                            .offset(
                                entityLiving.lastTickPosX + (entityLiving.posX - entityLiving.lastTickPosX) * timer.renderPartialTicks,
                                entityLiving.lastTickPosY + (entityLiving.posY - entityLiving.lastTickPosY) * timer.renderPartialTicks,
                                entityLiving.lastTickPosZ + (entityLiving.posZ - entityLiving.lastTickPosZ) * timer.renderPartialTicks
                            )
                            .offset(-renderManager.renderPosX, -renderManager.renderPosY, -renderManager.renderPosZ)
                        val boxVertices = arrayOf(doubleArrayOf(bb.minX, bb.minY, bb.minZ), doubleArrayOf(bb.minX, bb.maxY, bb.minZ), doubleArrayOf(bb.maxX, bb.maxY, bb.minZ), doubleArrayOf(bb.maxX, bb.minY, bb.minZ), doubleArrayOf(bb.minX, bb.minY, bb.maxZ), doubleArrayOf(bb.minX, bb.maxY, bb.maxZ), doubleArrayOf(bb.maxX, bb.maxY, bb.maxZ), doubleArrayOf(bb.maxX, bb.minY, bb.maxZ))
                        var minX = mc.displayWidth.toFloat()
                        var minY = mc.displayHeight.toFloat()
                        var maxX = 0f
                        var maxY = 0f
                        for (boxVertex in boxVertices) {
                            val screenPos = WorldToScreen.worldToScreen(Vector3f(boxVertex[0].toFloat(), boxVertex[1].toFloat(), boxVertex[2].toFloat()), mvMatrix, projectionMatrix, mc.displayWidth, mc.displayHeight) ?: continue
                            minX = min(screenPos.x.toDouble(), minX.toDouble()).toFloat()
                            minY = min(screenPos.y.toDouble(), minY.toDouble()).toFloat()
                            maxX = max(screenPos.x.toDouble(), maxX.toDouble()).toFloat()
                            maxY = max(screenPos.y.toDouble(), maxY.toDouble()).toFloat()
                        }
                        if (!(minX >= mc.displayWidth || minY >= mc.displayHeight || maxX <= 0 || maxY <= 0)) {
                            if (real2dOutline.get()) {
                                GL11.glLineWidth(2f)
                                GL11.glColor4f(0f, 0f, 0f, 1.0f)
                                if (real2dcsgo.get()) {
                                    val distX = (maxX - minX) / 3f
                                    val distY = (maxY - minY) / 3f
                                    GL11.glBegin(GL11.GL_LINE_STRIP)
                                    GL11.glVertex2f(minX, minY + distY)
                                    GL11.glVertex2f(minX, minY)
                                    GL11.glVertex2f(minX + distX, minY)
                                    GL11.glEnd()
                                    GL11.glBegin(GL11.GL_LINE_STRIP)
                                    GL11.glVertex2f(minX, maxY - distY)
                                    GL11.glVertex2f(minX, maxY)
                                    GL11.glVertex2f(minX + distX, maxY)
                                    GL11.glEnd()
                                    GL11.glBegin(GL11.GL_LINE_STRIP)
                                    GL11.glVertex2f(maxX - distX, minY)
                                    GL11.glVertex2f(maxX, minY)
                                    GL11.glVertex2f(maxX, minY + distY)
                                    GL11.glEnd()
                                    GL11.glBegin(GL11.GL_LINE_STRIP)
                                    GL11.glVertex2f(maxX - distX, maxY)
                                    GL11.glVertex2f(maxX, maxY)
                                    GL11.glVertex2f(maxX, maxY - distY)
                                    GL11.glEnd()
                                } else {
                                    GL11.glBegin(GL11.GL_LINE_LOOP)
                                    GL11.glVertex2f(minX, minY)
                                    GL11.glVertex2f(minX, maxY)
                                    GL11.glVertex2f(maxX, maxY)
                                    GL11.glVertex2f(maxX, minY)
                                    GL11.glEnd()
                                }
                                GL11.glLineWidth(1.0f)
                            }
                            GL11.glColor4f(color.red / 255.0f, color.green / 255.0f, color.blue / 255.0f, 1.0f)
                            if (real2dcsgo.get()) {
                                val distX = (maxX - minX) / 3f
                                val distY = (maxY - minY) / 3f
                                GL11.glBegin(GL11.GL_LINE_STRIP)
                                GL11.glVertex2f(minX, minY + distY)
                                GL11.glVertex2f(minX, minY)
                                GL11.glVertex2f(minX + distX, minY)
                                GL11.glEnd()
                                GL11.glBegin(GL11.GL_LINE_STRIP)
                                GL11.glVertex2f(minX, maxY - distY)
                                GL11.glVertex2f(minX, maxY)
                                GL11.glVertex2f(minX + distX, maxY)
                                GL11.glEnd()
                                GL11.glBegin(GL11.GL_LINE_STRIP)
                                GL11.glVertex2f(maxX - distX, minY)
                                GL11.glVertex2f(maxX, minY)
                                GL11.glVertex2f(maxX, minY + distY)
                                GL11.glEnd()
                                GL11.glBegin(GL11.GL_LINE_STRIP)
                                GL11.glVertex2f(maxX - distX, maxY)
                                GL11.glVertex2f(maxX, maxY)
                                GL11.glVertex2f(maxX, maxY - distY)
                                GL11.glEnd()
                            } else {
                                GL11.glBegin(GL11.GL_LINE_LOOP)
                                GL11.glVertex2f(minX, minY)
                                GL11.glVertex2f(minX, maxY)
                                GL11.glVertex2f(maxX, maxY)
                                GL11.glVertex2f(maxX, minY)
                                GL11.glEnd()
                            }
                            if (real2dShowHealth.get()) {
                                val barHeight = (maxY - minY) * (1 - entityLiving.health / entityLiving.maxHealth)
                                GL11.glColor4f(0.1f, 1f, 0.1f, 1f)
                                GL11.glBegin(GL11.GL_QUADS)
                                GL11.glVertex2f(maxX + 2, minY + barHeight)
                                GL11.glVertex2f(maxX + 2, maxY)
                                GL11.glVertex2f(maxX + 4, maxY)
                                GL11.glVertex2f(maxX + 4, minY + barHeight)
                                GL11.glEnd()
                                GL11.glColor4f(1f, 1f, 1f, 1f)
                                GL11.glEnable(GL11.GL_TEXTURE_2D)
                                GL11.glEnable(GL11.GL_DEPTH_TEST)
                                mc.fontRendererObj.drawStringWithShadow(decimalFormat.format(entityLiving.health.toDouble()) + " HP", maxX + 4, minY + barHeight, -1)
                                GL11.glDisable(GL11.GL_TEXTURE_2D)
                                GL11.glDisable(GL11.GL_DEPTH_TEST)
                                GlStateManager.resetColor()
                            }
                            if (real2dShowHeldItem.get() && entityLiving.heldItem != null && entityLiving.heldItem.item != null) {
                                GL11.glEnable(GL11.GL_TEXTURE_2D)
                                GL11.glEnable(GL11.GL_DEPTH_TEST)
                                val stringWidth = mc.fontRendererObj.getStringWidth(entityLiving.heldItem.displayName)
                                mc.fontRendererObj.drawStringWithShadow(entityLiving.heldItem.displayName, minX + (maxX - minX) / 2 - stringWidth / 2, maxY + 2, -1)
                                GL11.glDisable(GL11.GL_TEXTURE_2D)
                                GL11.glDisable(GL11.GL_DEPTH_TEST)
                            }
                            if (real2dShowName.get()) {
                                GL11.glEnable(GL11.GL_TEXTURE_2D)
                                GL11.glEnable(GL11.GL_DEPTH_TEST)
                                val stringWidth = mc.fontRendererObj.getStringWidth(entityLiving.displayName.formattedText)
                                mc.fontRendererObj.drawStringWithShadow(entityLiving.displayName.formattedText, minX + (maxX - minX) / 2 - stringWidth / 2, minY - 12, -1)
                                GL11.glDisable(GL11.GL_TEXTURE_2D)
                                GL11.glDisable(GL11.GL_DEPTH_TEST)
                            }
                        }
                    }
                }
            }
        }
        if (real2d) {
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glPopMatrix()
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glPopMatrix()
            GL11.glPopAttrib()
        }
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val mode = modeValue.get().lowercase(Locale.getDefault())
        val shader = (if (mode.equals("shaderoutline", ignoreCase = true)) OutlineShader.OUTLINE_SHADER else if (mode.equals("shaderglow", ignoreCase = true)) GlowShader.GLOW_SHADER else null) ?: return
        shader.startDraw(event.partialTicks)
        renderNameTags = false
        try {
            for (entity in RenderConfig.entities()) {
                mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
            }
        } catch (ex: Exception) {
            ClientUtils.getLogger().error("An error occurred while rendering all entities for shader esp", ex)
        }
        renderNameTags = true
        val radius = if (mode.equals("shaderoutline", ignoreCase = true)) shaderOutlineRadius.get() else if (mode.equals("shaderglow", ignoreCase = true)) shaderGlowRadius.get() else 1f
        shader.stopDraw(getColor(null), radius, 1f)
    }

    fun getColor(entity: Entity?): Color {
        if (entity is EntityLivingBase) {
            if (colorModeValue.get().equals("Health", ignoreCase = true)) return BlendUtils.getHealthColor(entity.health, entity.maxHealth)
            if (entity.hurtTime > 0) return Color.RED
            if (EntityUtils.isFriend(entity)) return Color.BLUE
            if (colorTeam.get()) {
                val chars = entity.displayName.formattedText.toCharArray()
                var color = Int.MAX_VALUE
                for (i in chars.indices) {
                    if (chars[i] != '§' || i + 1 >= chars.size) continue
                    val index = getColorIndex(chars[i + 1])
                    if (index < 0 || index > 15) continue
                    color = ColorUtils.hexColors[index]
                    break
                }
                return Color(color)
            }
        }
        return when (colorModeValue.get()) {
            "Custom" -> Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
            "Rainbow" -> Color(RenderUtils.getRainbowOpaque(mixerSecondsValue.get(), saturationValue.get(), brightnessValue.get(), 0))
            "Sky" -> RenderUtils.skyRainbow(0, saturationValue.get(), brightnessValue.get())
            "LiquidSlowly" -> LiquidSlowly(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get())
            "Mixer" -> ColorMixer.getMixedColor(0, mixerSecondsValue.get())
            "Fade" -> fade(Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), 0, 100)
            else -> Color.white
        }
    }

    companion object {
        @JvmField
		var renderNameTags = true
    }
}
