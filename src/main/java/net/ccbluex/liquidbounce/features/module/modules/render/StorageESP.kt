/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import co.uk.hexeption.utils.OutlineUtils
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.render.ColorMixer.Companion.getMixedColor
import net.ccbluex.liquidbounce.features.module.modules.world.ChestAura.clickedBlocks
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.LiquidSlowly
import net.ccbluex.liquidbounce.utils.render.ColorUtils.fade
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GlowShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.OutlineShader
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.entity.item.EntityMinecartChest
import net.minecraft.tileentity.*
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*

@ModuleInfo(name = "StorageESP", spacedName = "Storage ESP", description = "Allows you to see chests, dispensers, etc. through walls.", category = ModuleCategory.RENDER)
class StorageESP : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Box", "OtherBox", "Outline", "ShaderOutline", "ShaderGlow", "2D", "WireFrame"), "Outline")
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Rainbow", "Sky", "LiquidSlowly", "Fade", "Mixer"), "Custom")
    private val saturationValue = FloatValue("Saturation", 1f, 0f, 1f)
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)
    private val mixerSecondsValue = IntegerValue("Seconds", 2, 1, 10)
    private val chestValue = BoolValue("Chest", true)
    private val enderChestValue = BoolValue("EnderChest", true)
    private val furnaceValue = BoolValue("Furnace", true)
    private val dispenserValue = BoolValue("Dispenser", true)
    private val hopperValue = BoolValue("Hopper", true)
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        try {
            val mode = modeValue.get()
            if (mode.equals("outline", ignoreCase = true)) {
                ClientUtils.disableFastRender()
                OutlineUtils.checkSetupFBO()
            }
            val gamma = mc.gameSettings.gammaSetting
            mc.gameSettings.gammaSetting = 100000.0f
            for (tileEntity in mc.theWorld.loadedTileEntityList) {
                var color: Color? = null
                val index = 0
                if (chestValue.get() && tileEntity is TileEntityChest && !clickedBlocks.contains(tileEntity.getPos())) if (colorModeValue.isMode("Custom")) {
                    color = Color(colorRedValue.get(), colorBlueValue.get(), colorGreenValue.get())
                } else {
                    if (colorModeValue.isMode("Rainbow")) {
                        color = Color(RenderUtils.getRainbowOpaque(mixerSecondsValue.get(), saturationValue.get(), brightnessValue.get(), index))
                    } else {
                        if (colorModeValue.isMode("Sky")) {
                            color = RenderUtils.skyRainbow(index, saturationValue.get(), brightnessValue.get())
                        } else {
                            if (colorModeValue.isMode("LiquidSlowly")) {
                                color = LiquidSlowly(System.nanoTime(), index, saturationValue.get(), brightnessValue.get())
                            } else {
                                if (colorModeValue.isMode("Fade")) {
                                    color = getMixedColor(index, mixerSecondsValue.get())
                                } else {
                                    if (colorModeValue.isMode("Mixer")) {
                                        color = fade(Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), index, 100)
                                    }
                                }
                            }
                        }
                    }
                }
                if (enderChestValue.get() && tileEntity is TileEntityEnderChest && !clickedBlocks.contains(tileEntity.getPos())) color = Color.MAGENTA
                if (furnaceValue.get() && tileEntity is TileEntityFurnace) color = Color.BLACK
                if (dispenserValue.get() && tileEntity is TileEntityDispenser) color = Color.BLACK
                if (hopperValue.get() && tileEntity is TileEntityHopper) color = Color.GRAY
                if (color == null) continue
                if (!(tileEntity is TileEntityChest || tileEntity is TileEntityEnderChest)) {
                    RenderUtils.drawBlockBox(tileEntity.pos, color, !mode.equals("otherbox", ignoreCase = true))
                    continue
                }
                when (mode.lowercase(Locale.getDefault())) {
                    "otherbox", "box" -> RenderUtils.drawBlockBox(tileEntity.pos, color, !mode.equals("otherbox", ignoreCase = true))
                    "2d" -> RenderUtils.draw2D(tileEntity.pos, color.rgb, Color.BLACK.rgb)
                    "outline" -> {
                        RenderUtils.glColor(color)
                        OutlineUtils.renderOne(3f)
                        TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity, event.partialTicks, -1)
                        OutlineUtils.renderTwo()
                        TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity, event.partialTicks, -1)
                        OutlineUtils.renderThree()
                        TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity, event.partialTicks, -1)
                        OutlineUtils.renderFour(color)
                        TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity, event.partialTicks, -1)
                        OutlineUtils.renderFive()
                        OutlineUtils.setColor(Color.WHITE)
                    }

                    "wireframe" -> {
                        GL11.glPushMatrix()
                        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
                        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE)
                        GL11.glDisable(GL11.GL_TEXTURE_2D)
                        GL11.glDisable(GL11.GL_LIGHTING)
                        GL11.glDisable(GL11.GL_DEPTH_TEST)
                        GL11.glEnable(GL11.GL_LINE_SMOOTH)
                        GL11.glEnable(GL11.GL_BLEND)
                        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                        TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity, event.partialTicks, -1)
                        RenderUtils.glColor(color)
                        GL11.glLineWidth(1.5f)
                        TileEntityRendererDispatcher.instance.renderTileEntity(tileEntity, event.partialTicks, -1)
                        GL11.glPopAttrib()
                        GL11.glPopMatrix()
                    }
                }
            }
            for (entity in mc.theWorld.loadedEntityList) if (entity is EntityMinecartChest) {
                when (mode.lowercase(Locale.getDefault())) {
                    "otherbox", "box" -> RenderUtils.drawEntityBox(entity, Color(colorRedValue.get(), colorBlueValue.get(), colorGreenValue.get()), !mode.equals("otherbox", ignoreCase = true))
                    "2d" -> RenderUtils.draw2D(entity.getPosition(), Color(colorRedValue.get(), colorBlueValue.get(), colorGreenValue.get()).rgb, Color.BLACK.rgb)
                    "outline" -> {
                        val entityShadow = mc.gameSettings.entityShadows
                        mc.gameSettings.entityShadows = false
                        RenderUtils.glColor(Color(colorRedValue.get(), colorBlueValue.get(), colorGreenValue.get()))
                        OutlineUtils.renderOne(3f)
                        mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
                        OutlineUtils.renderTwo()
                        mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
                        OutlineUtils.renderThree()
                        mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
                        OutlineUtils.renderFour(Color(colorRedValue.get(), colorBlueValue.get(), colorGreenValue.get()))
                        mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
                        OutlineUtils.renderFive()
                        OutlineUtils.setColor(Color.WHITE)
                        mc.gameSettings.entityShadows = entityShadow
                    }

                    "wireframe" -> {
                        val entityShadow = mc.gameSettings.entityShadows
                        mc.gameSettings.entityShadows = false
                        GL11.glPushMatrix()
                        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
                        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE)
                        GL11.glDisable(GL11.GL_TEXTURE_2D)
                        GL11.glDisable(GL11.GL_LIGHTING)
                        GL11.glDisable(GL11.GL_DEPTH_TEST)
                        GL11.glEnable(GL11.GL_LINE_SMOOTH)
                        GL11.glEnable(GL11.GL_BLEND)
                        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                        RenderUtils.glColor(Color(colorRedValue.get(), colorBlueValue.get(), colorGreenValue.get()))
                        mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
                        RenderUtils.glColor(Color(colorRedValue.get(), colorBlueValue.get(), colorGreenValue.get()))
                        GL11.glLineWidth(1.5f)
                        mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
                        GL11.glPopAttrib()
                        GL11.glPopMatrix()
                        mc.gameSettings.entityShadows = entityShadow
                    }
                }
            }
            RenderUtils.glColor(Color(255, 255, 255, 255))
            mc.gameSettings.gammaSetting = gamma
        } catch (ignored: Exception) {
        }
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val mode = modeValue.get()
        val shader = (if (mode.equals("shaderoutline", ignoreCase = true)) OutlineShader.OUTLINE_SHADER else if (mode.equals("shaderglow", ignoreCase = true)) GlowShader.GLOW_SHADER else null) ?: return
        shader.startDraw(event.partialTicks)
        try {
            val renderManager = mc.renderManager
            for (entity in mc.theWorld.loadedTileEntityList) {
                if (entity !is TileEntityChest) continue
                if (clickedBlocks.contains(entity.getPos())) continue
                TileEntityRendererDispatcher.instance.renderTileEntityAt(
                    entity,
                    entity.getPos().x - renderManager.renderPosX,
                    entity.getPos().y - renderManager.renderPosY,
                    entity.getPos().z - renderManager.renderPosZ,
                    event.partialTicks
                )
            }
            for (entity in mc.theWorld.loadedEntityList) {
                if (entity !is EntityMinecartChest) continue
                renderManager.renderEntityStatic(entity, event.partialTicks, true)
            }
        } catch (ex: Exception) {
            ClientUtils.getLogger().error("An error occurred while rendering all storages for shader esp", ex)
        }
        shader.stopDraw(Color(colorRedValue.get(), colorBlueValue.get(), colorGreenValue.get()), if (mode.equals("shaderglow", ignoreCase = true)) 2.5f else 1.5f, 1f)
    }

    companion object {
        val colorRedValue = IntegerValue("Red", 0, 0, 255)
        val colorGreenValue = IntegerValue("Green", 160, 0, 255)
        val colorBlueValue = IntegerValue("Blue", 255, 0, 255)
    }
}
