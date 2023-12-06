package net.ccbluex.liquidbounce.ui.client.clickgui.styles.liquidbounce

/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */

import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUI
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlockName
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.animations.Direction
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.MathHelper
import net.minecraft.util.StringUtils
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class LiquidBounceStyle : GuiScreen() {
    private var mouseDown = false
    private var rightMouseDown = false
    fun drawPanel(mouseX: Int, mouseY: Int, panel: Panel) {
        RenderUtils.drawBorderedRect(panel.x.toFloat() - if (panel.scrollbar) 4 else 0, panel.y.toFloat(), panel.x.toFloat() + panel.width, panel.y.toFloat() + 19 + panel.fade, 1f, Color(255, 255, 255, 90).rgb, Int.MIN_VALUE)
        val textWidth = Fonts.font35.getStringWidth("§f" + StringUtils.stripControlCodes(panel.name)).toFloat()
        Fonts.font35.drawString("§f" + panel.name, (panel.x - (textWidth - 100.0f) / 2f).toInt(), panel.y + 7, -16777216)
        if (panel.scrollbar && panel.fade > 0) {
            RenderUtils.drawRect(panel.x - 1.5f, panel.y + 21f, panel.x - 0.5f, panel.y + 16f + panel.fade, Int.MAX_VALUE)
            RenderUtils.drawRect(panel.x - 2f, panel.y + 30f + (panel.fade - 24f) / (panel.elements.size - ClickGUI.maxElements) * panel.dragged - 10.0f, panel.x.toFloat(), panel.y + 40f + (panel.fade - 24.0f) / (panel.elements.size - ClickGUI.maxElements) * panel.dragged, Int.MIN_VALUE)
        }
    }

    fun drawDescription(mouseX: Int, mouseY: Int, me: ModuleElement) {
        val desc = me.module.description
        if (desc.isEmpty()) return
        val textWidth = Fonts.font35.getStringWidth(desc)
        RenderUtils.makeScissorBox(mouseX + 8f, mouseY - 1f, (mouseX + 8f + (textWidth + 7f) * (1 - me.anim.outputFloat)), mouseY + Fonts.font35.FONT_HEIGHT + 4f)
        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        RenderUtils.drawBorderedRect((mouseX + 9).toFloat(), mouseY.toFloat(), (mouseX + textWidth + 14).toFloat(), (mouseY + Fonts.font35.FONT_HEIGHT + 3).toFloat(), 1f, Color(255, 255, 255, 90).rgb, Int.MIN_VALUE)
        GlStateManager.resetColor()
        Fonts.font35.drawString(desc, mouseX + 12, mouseY + Fonts.font35.FONT_HEIGHT / 2, Int.MAX_VALUE)
        GL11.glDisable(GL11.GL_SCISSOR_TEST)

    }

    fun drawButtonElement(mouseX: Int, mouseY: Int, buttonElement: ButtonElement) {
        GlStateManager.resetColor()
        Fonts.font35.drawString(buttonElement.displayName, (buttonElement.x + 5), buttonElement.y + 7, buttonElement.color)
    }

    fun drawModuleElement(mouseX: Int, mouseY: Int, me: ModuleElement, parent: Panel) {
        val guiColor: Int = ClickGUI.accentColor.rgb
        val moduleValues = me.module.values


        if (moduleValues.isNotEmpty()) {
            me.anim.setDirection(if (me.isShowSettings) Direction.FORWARDS else Direction.BACKWARDS)

            // janky ass visibility check
            if (abs(me.anim.output) > 10E-3 && me.isVisible && me.y + me.height < parent.y + parent.height + parent.fade) {
                val expectedHeight = moduleValues.sumOf {
                    when (it) {
                        is BoolValue -> 12
                        is ListValue -> 12 + 12 * if (it.openList) it.values.size else 0
                        is FloatValue -> 22
                        is IntegerValue -> 22
                        is FontValue -> 11
                        else -> 12
                    }
                }

                var yPos: Int = me.y + 4

                val xStart = me.x + me.width + 4f
                val yStart = yPos + 0f
                val xEnd = me.x + me.width + me.settingsWidth
                val yEnd = yPos + expectedHeight + 4f
                val width = xEnd - xStart
                val height = yEnd - yStart
                val animProgress = me.anim.outputFloat

                RenderUtils.makeScissorBox(
                    (xStart) * ClickGUI.scale,
                    (yStart) * ClickGUI.scale,
                    (xStart + width * animProgress) * ClickGUI.scale,
                    (yEnd) * ClickGUI.scale,
                )

                GL11.glEnable(GL11.GL_SCISSOR_TEST)

                for (value in moduleValues) {
                    if (!value.canDisplay.invoke()) continue
                    val isNumber = value.get() is Number
                    if (isNumber) {
                        assumeNonVolatile = false
                    }
                    if (value is BoolValue) {
                        val text = value.name
                        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
                        if (me.settingsWidth < textWidth + 8) me.settingsWidth = textWidth + 8
                        RenderUtils.drawRect(me.x + me.width + 4f, yPos + 2f, me.x + me.width + me.settingsWidth + 0f, yPos + 14f, Int.MIN_VALUE)
                        if (mouseX >= me.x + me.width + 4 && mouseX <= me.x + me.width + me.settingsWidth && mouseY >= yPos + 2 && mouseY <= yPos + 14) {
                            if (Mouse.isButtonDown(0) && me.isntPressed()) {
                                value.set(!value.get())
//                                mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("gui.button.press"), 1.0f))
                            }
                        }
                        GlStateManager.resetColor()
                        Fonts.font35.drawString(text, me.x + me.width + 6, yPos + 4, if (value.get()) guiColor else Int.MAX_VALUE)
                        yPos += 12
                    } else if (value is ListValue) {
                        val text = value.name
                        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
                        if (me.settingsWidth < textWidth + 16) me.settingsWidth = textWidth + 16
                        RenderUtils.drawRect(me.x + me.width + 4f, yPos + 2f, me.x + me.width + me.settingsWidth + 0f, yPos + 14f, Int.MIN_VALUE)
                        GlStateManager.resetColor()
                        Fonts.font35.drawString("§c$text", me.x + me.width + 6, yPos + 4, 0xffffff)
                        Fonts.font35.drawString(if (value.openList) "-" else "+", (me.x + me.width + me.settingsWidth - if (value.openList) 5 else 6).toInt(), yPos + 4, 0xffffff)
                        if (mouseX >= me.x + me.width + 4 && mouseX <= me.x + me.width + me.settingsWidth && mouseY >= yPos + 2 && mouseY <= yPos + 14) {
                            if (Mouse.isButtonDown(0) && me.isntPressed()) {
                                value.openList = !value.openList
//                                mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("gui.button.press"), 1.0f))
                            }
                        }
                        yPos += 12
                        for (valueOfList in value.values) {
                            val textWidth2 = Fonts.font35.getStringWidth(">  $valueOfList").toFloat()
                            if (me.settingsWidth < textWidth2 + 8) me.settingsWidth = (textWidth2 + 8)
                            if (value.openList) {
                                RenderUtils.drawRect(me.x + me.width + 4f, yPos + 2f, me.x + me.width + me.settingsWidth + 0f, yPos + 14f, Int.MIN_VALUE)
                                if (mouseX >= me.x + me.width + 4 && mouseX <= me.x + me.width + me.settingsWidth && mouseY >= yPos + 2 && mouseY <= yPos + 14) {
                                    if (Mouse.isButtonDown(0) && me.isntPressed()) {
                                        value.set(valueOfList)
//                                        mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("gui.button.press"), 1.0f))
                                    }
                                }
                                GlStateManager.resetColor()
                                Fonts.font35.drawString(">", me.x + me.width + 6, yPos + 4, Int.MAX_VALUE)
                                Fonts.font35.drawString(valueOfList, me.x + me.width + 14, yPos + 4, if (value.get() != null && value.get().equals(valueOfList, ignoreCase = true)) guiColor else Int.MAX_VALUE)
                                yPos += 12
                            }
                        }
                    } else if (value is FloatValue) {
                        val text = value.name + "§f: §c" + round(value.get()) + value.suffix
                        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
                        if (me.settingsWidth < textWidth + 8) me.settingsWidth = (textWidth + 8)
                        RenderUtils.drawRect(me.x + me.width + 4f, yPos + 2f, me.x + me.width + me.settingsWidth + 0f, yPos + 24f, Int.MIN_VALUE)
                        RenderUtils.drawRect(me.x + me.width + 8f, yPos + 18f, me.x + me.width + me.settingsWidth - 4 + 0f, yPos + 19f, Int.MAX_VALUE)
                        val sliderValue: Float = me.x + me.width + (me.settingsWidth - 12) * (value.get() - value.minimum) / (value.maximum - value.minimum)
                        RenderUtils.drawRect(8 + sliderValue, (yPos + 15).toFloat(), sliderValue + 11, (yPos + 21).toFloat(), guiColor)
                        if (mouseX >= me.x + me.width + 4 && mouseX <= me.x + me.width + me.settingsWidth - 4 && mouseY >= yPos + 15 && mouseY <= yPos + 21) {
                            val dWheel = Mouse.getDWheel()
                            if (Mouse.hasWheel() && dWheel != 0) {
                                if (dWheel > 0) value.set(min(value.get() + 0.01f, value.maximum))
                                if (dWheel < 0) value.set(max(value.get() - 0.01f, value.minimum))
                            }
                            if (Mouse.isButtonDown(0)) {
                                val i = MathHelper.clamp_double(((mouseX - me.x - me.width - 8) / (me.settingsWidth - 12)).toDouble(), 0.0, 1.0)
                                value.set(round((value.minimum + (value.maximum - value.minimum) * i).toFloat()).toFloat())
                            }
                        }
                        GlStateManager.resetColor()
                        Fonts.font35.drawString(text, me.x + me.width + 6, yPos + 4, 0xffffff)
                        yPos += 22
                    } else if (value is IntegerValue) {
                        val text = value.name + "§f: §c" + if (value is BlockValue) getBlockName(value.get()) + " (" + value.get() + ")" else value.get().toString() + value.suffix
                        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
                        if (me.settingsWidth < textWidth + 8) me.settingsWidth = (textWidth + 8)
                        RenderUtils.drawRect(me.x + me.width + 4f, yPos + 2f, me.x + me.width + me.settingsWidth + 0f, yPos + 24f, Int.MIN_VALUE)
                        RenderUtils.drawRect(me.x + me.width + 8f, yPos + 18f, me.x + me.width + me.settingsWidth - 4f, yPos + 19f, Int.MAX_VALUE)
                        val sliderValue: Float = me.x + me.width + (me.settingsWidth - 12) * (value.get() - value.minimum) / (value.maximum - value.minimum)
                        RenderUtils.drawRect(8 + sliderValue, (yPos + 15).toFloat(), sliderValue + 11, (yPos + 21).toFloat(), guiColor)
                        if (mouseX >= me.x + me.width + 4 && mouseX <= me.x + me.width + me.settingsWidth && mouseY >= yPos + 15 && mouseY <= yPos + 21) {
                            val dWheel = Mouse.getDWheel()
                            if (Mouse.hasWheel() && dWheel != 0) {
                                if (dWheel > 0) value.set(min(value.get() + 1, value.maximum))
                                if (dWheel < 0) value.set(max(value.get() - 1, value.minimum))
                            }
                            if (Mouse.isButtonDown(0)) {
                                val i = MathHelper.clamp_double(((mouseX - me.x - me.width - 8) / (me.settingsWidth - 12)).toDouble(), 0.0, 1.0)
                                value.set((value.minimum + (value.maximum - value.minimum) * i).toInt())
                            }
                        }
                        GlStateManager.resetColor()
                        Fonts.font35.drawString(text, me.x + me.width + 6, yPos + 4, 0xffffff)
                        yPos += 22
                    } else if (value is FontValue) {
                        val fontRenderer = value.get()
                        RenderUtils.drawRect(me.x + me.width + 4f, yPos + 2f, me.x + me.width + me.settingsWidth + 0f, yPos + 14f, Int.MIN_VALUE)
                        var displayString = "Font: Unknown"
                        if (fontRenderer is GameFontRenderer) {
                            displayString = "Font: " + fontRenderer.defaultFont.font.name + " - " + fontRenderer.defaultFont.font.size
                        } else if (fontRenderer === Fonts.minecraftFont) displayString = "Font: Minecraft" else {
                            val objects = Fonts.getFontDetails(fontRenderer)
                            if (objects != null) {
                                displayString = objects[0].toString() + if (objects[1] as Int != -1) " - " + objects[1] else ""
                            }
                        }
                        Fonts.font35.drawString(displayString, me.x + me.width + 6, yPos + 4, Color.WHITE.rgb)
                        val stringWidth = Fonts.font35.getStringWidth(displayString)
                        if (me.settingsWidth < stringWidth + 8) me.settingsWidth = (stringWidth + 8).toFloat()
                        if ((Mouse.isButtonDown(0) && !mouseDown || Mouse.isButtonDown(1) && !rightMouseDown) && mouseX >= me.x + me.width + 4 && mouseX <= me.x + me.width + me.settingsWidth && mouseY >= yPos + 4 && mouseY <= yPos + 12) {
                            val fonts = Fonts.getFonts()
                            if (Mouse.isButtonDown(0)) {
                                var i = 0
                                while (i < fonts.size) {
                                    val font = fonts[i]
                                    if (font === fontRenderer) {
                                        i++
                                        if (i >= fonts.size) i = 0
                                        value.set(fonts[i])
                                        break
                                    }
                                    i++
                                }
                            } else {
                                var i = fonts.size - 1
                                while (i >= 0) {
                                    val font = fonts[i]
                                    if (font === fontRenderer) {
                                        i--
                                        if (i >= fonts.size) i = 0
                                        if (i < 0) i = fonts.size - 1
                                        value.set(fonts[i])
                                        break
                                    }
                                    i--
                                }
                            }
                        }
                        yPos += 11
                    } else {
                        val text = value.name + "§f: §c" + value.get()
                        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
                        if (me.settingsWidth < textWidth + 8) me.settingsWidth = textWidth + 8
                        RenderUtils.drawRect(me.x + me.width + 4f, yPos + 2f, me.x + me.width + me.settingsWidth + 0f, yPos + 14f, Int.MIN_VALUE)
                        GlStateManager.resetColor()
                        Fonts.font35.drawString(text, me.x + me.width + 6, yPos + 4, 0xffffff)
                        yPos += 12
                    }
                    if (isNumber) {
                        assumeNonVolatile = true
                    }
                }

                GL11.glDisable(GL11.GL_SCISSOR_TEST)
                me.updatePressed()
                mouseDown = Mouse.isButtonDown(0)
                rightMouseDown = Mouse.isButtonDown(1)
//                if (me.settingsWidth > 0f && yPos > me.y + 4)
//                    RenderUtils.drawBorderedRect(
//                        (xStart + halfWidth - halfWidth * animProgress) * ClickGUI.scale,
//                        (yStart + halfHeight - halfHeight * animProgress + 4f) * ClickGUI.scale,
//                        (xEnd - halfWidth + halfWidth * animProgress) * ClickGUI.scale,
//                        (yEnd - halfHeight + halfHeight * animProgress - 4f) * ClickGUI.scale, 1f, Int.MIN_VALUE, 0)
            }
        }
    }

    private fun round(f: Float): BigDecimal {
        var bd = BigDecimal(f.toString())
        bd = bd.setScale(2, 4)
        return bd
    }

    fun preDrawScreen(x: Float, y: Float, x2: Float, y2: Float, me: ModuleElement) {

        RenderUtils.makeScissorBox(x * ClickGUI.scale, y * ClickGUI.scale, x2 * ClickGUI.scale, y2 * ClickGUI.scale)
        GL11.glEnable(GL11.GL_SCISSOR_TEST)

        GlStateManager.resetColor()
        Fonts.font35.drawString(me.displayName, (me.x + 5), me.y + 7, if (me.module.state) ClickGUI.accentColor.rgb else Int.MAX_VALUE)
        if (me.module.values.isNotEmpty()) {
            Fonts.font35.drawString("+", me.x + me.width - 8, me.y + me.height / 2, Color.WHITE.rgb)
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST)
    }

    companion object {
        private var instance: LiquidBounceStyle? = null
        fun getInstance(): LiquidBounceStyle {
            return if (instance == null) LiquidBounceStyle().also { instance = it } else instance!!
        }

        fun resetInstance() {
            instance = LiquidBounceStyle()
        }
    }
}