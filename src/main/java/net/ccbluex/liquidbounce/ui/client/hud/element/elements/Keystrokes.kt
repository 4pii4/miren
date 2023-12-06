package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.CPSCounter
import net.ccbluex.liquidbounce.utils.extensions.drawXYCenteredString
import net.ccbluex.liquidbounce.utils.render.*
import net.ccbluex.liquidbounce.utils.render.animations.Direction
import net.ccbluex.liquidbounce.utils.render.animations.impl.CustomAnimation
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.FontValue
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color
import javax.vecmath.Vector2f
import kotlin.math.sqrt

/**
 * state of the art sigma ripoff
 * @author sigma client (the og)
 * @author pie (main logic)
 * @inf blur
 */
@ElementInfo(name = "Keystrokes")
class Keystrokes(x: Double = 10.0, y: Double = 30.0) : Element(x, y) {
    private val pad by FloatValue("Pad", 2f, 0f, 20f)
    private val totalWidth by FloatValue("TotalWidth", 82f, 60f, 400f)
    private val buttonHeight by FloatValue("ButtonHeight", 20f, 10f, 100f)
    private val drawJump by BoolValue("DrawJump", true)
    private val drawMouseButtons by BoolValue("DrawMouseButtons", true)
    private val simpleMouseButtonName by BoolValue("SimpleMouseButtonName", false) { drawMouseButtons }
    private val mouseButtonSkipAnim by BoolValue("MouseButtonSkipAnim", true) { drawMouseButtons }
    private val showCPS by BoolValue("ShowCPS", true) { drawMouseButtons }
    private val blur by BoolValue("Blur", false)
    private val blurStrength by FloatValue("BlurStrength", 10f, 1f, 50f) { blur }
    private val font by FontValue("Font", Fonts.minecraftNativeFont)
    private val fontYOffset by FloatValue("FontYOffset", 0f, -10f, 10f)

    private val wasdWidth: Double
        get() = ((totalWidth - 4 * pad) / 3).toDouble()
    private val mbWidth: Double
        get() = ((totalWidth - 3 * pad) / 2).toDouble()

    private val wButton = KeyBox("W") { mc.gameSettings.keyBindForward.isKeyDown }
    private val aButton = KeyBox("A") { mc.gameSettings.keyBindLeft.isKeyDown }
    private val sButton = KeyBox("S") { mc.gameSettings.keyBindBack.isKeyDown }
    private val dButton = KeyBox("D") { mc.gameSettings.keyBindRight.isKeyDown }
    private val spaceButton = KeyBox("Space", duration = 250) { mc.gameSettings.keyBindJump.isKeyDown }
    private val rmbButton = KeyBox("RMB", duration = 150) { mc.gameSettings.keyBindUseItem.isKeyDown }
    private val lmbButton = KeyBox("LMB", duration = 150) { mc.gameSettings.keyBindAttack.isKeyDown }

    private var lastYPos = 0f

    override fun drawElement(): Border {
        val pad = pad.toDouble()
        val buttonHeight = buttonHeight.toDouble()

        var totalHeight = pad + (buttonHeight + pad) * 2
        if (drawJump)
            totalHeight += pad + buttonHeight
        if (drawMouseButtons)
            totalHeight += pad + buttonHeight

        var yPos = pad

        if (blur) {
            GL11.glScalef(1F, 1F, 1F)
            GL11.glPopMatrix()
            GL11.glPushMatrix()
            BlurUtils.blur((renderX * scale).toFloat(), (renderY * scale).toFloat(), ((renderX + totalWidth) * scale).toFloat(), ((renderY + totalHeight) * scale).toFloat(), blurStrength, true) {
                GlStateManager.enableBlend()
                GlStateManager.disableTexture2D()
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
                wButton.mask(pad + wasdWidth + pad, yPos, wasdWidth, buttonHeight)
                yPos += pad + buttonHeight
                aButton.mask(pad, yPos, wasdWidth, buttonHeight)
                sButton.mask(pad + wasdWidth + pad, yPos, wasdWidth, buttonHeight)
                dButton.mask(pad + wasdWidth * 2 + pad * 2, yPos, wasdWidth, buttonHeight)
                yPos += pad + buttonHeight

                if (drawJump) {
                    spaceButton.mask(pad, yPos, totalWidth - pad * 2, buttonHeight)
                    yPos += pad + buttonHeight
                }

                if (drawMouseButtons) {
                    lmbButton.mask(pad, yPos, mbWidth, buttonHeight)
                    rmbButton.mask(pad + mbWidth + pad, yPos, mbWidth, buttonHeight)
                    yPos += pad + buttonHeight
                }
                GlStateManager.enableTexture2D()
                GlStateManager.disableBlend()
            }
            GL11.glPopMatrix()
            GL11.glPushMatrix()
            GL11.glScalef(scale, scale, scale)
            GL11.glTranslated(renderX, renderY, 0.0)
        }

        yPos = pad
        wButton.draw(pad + wasdWidth + pad, yPos, wasdWidth, buttonHeight)
        yPos += pad + buttonHeight
        aButton.draw(pad, yPos, wasdWidth, buttonHeight)
        sButton.draw(pad + wasdWidth + pad, yPos, wasdWidth, buttonHeight)
        dButton.draw(pad + wasdWidth * 2 + pad * 2, yPos, wasdWidth, buttonHeight)
        yPos += pad + buttonHeight

        if (drawJump) {
            spaceButton.draw(pad, yPos, totalWidth - pad * 2, buttonHeight)
            yPos += pad + buttonHeight
        }

        if (drawMouseButtons) {
            lmbButton.draw(pad, yPos, mbWidth, buttonHeight)
            rmbButton.draw(pad + mbWidth + pad, yPos, mbWidth, buttonHeight)
            yPos += pad + buttonHeight
        }

        lastYPos = yPos.toFloat()
        return Border(0f, 0f, totalWidth, yPos.toFloat())
    }

    inner class KeyBox(val name: String, duration: Int = 200, val pressed: () -> Boolean) {
        private val anim = CustomAnimation(duration, 1.0, Direction.FORWARDS) { EaseUtils.easeInOutQuart(it) }

        fun mask(x: Double, y: Double, width: Double, height: Double) {
            RenderUtils.quickDrawRect((renderX + x) * scale, (renderY + y) * scale, (renderX + x + width) * scale, (renderY + y + height) * scale)
        }

        fun draw(x: Double, y: Double, width: Double, height: Double) {
            anim.setDirection(if (pressed()) Direction.FORWARDS else Direction.BACKWARDS)
            val halfWidth = width / 2
            val halfHeight = height / 2
            val radius = sqrt(halfWidth * halfWidth + halfHeight * halfHeight)
            val center = Vector2f((x + halfWidth).toFloat(), (y + halfHeight).toFloat())
            var name = name

            var animProgress = anim.output

            if (name.endsWith("MB")) {
                if (mouseButtonSkipAnim)
                    animProgress = if (pressed()) 1.0 else 0.0
                if (simpleMouseButtonName)
                    name = name.replace("MB", "")
                if (showCPS) {
                    val cps = if (name.startsWith("L")) CPSCounter.getCPS(CPSCounter.MouseButton.LEFT) else if (name.startsWith("R")) CPSCounter.getCPS(CPSCounter.MouseButton.RIGHT) else 0
                    if (cps > 1)
                        name = "$cps CPS"
                }
            }

            when (animProgress) {
                0.0 -> RenderUtils.drawRect(x, y, x + width, y + height, Color(0f, 0f, 0f, .5f).rgb)

                1.0 -> RenderUtils.drawRect(x, y, x + width, y + height, Color(1f, 1f, 1f, .7f).rgb)

                else -> {
                    Stencil.write(false)
                    RenderUtils.drawFilledCircle(center.x, center.y, (radius * animProgress).toFloat(), Color(1f, 1f, 1f, 1f))
                    Stencil.erase(false)
                    RenderUtils.drawRect(x, y, x + width, y + height, Color(0f, 0f, 0f, .5f).rgb)
                    Stencil.dispose()

                    Stencil.write(false)
                    RenderUtils.quickDrawRect(x, y, x + width, y + height)
                    Stencil.erase(true)
                    ShaderUtils.drawFilledCircle(center.x, center.y, (radius * animProgress).toFloat(), Color(1f, 1f, 1f, 1f - (0.3 * animProgress).toFloat()))
                    Stencil.dispose()
                }
            }

            font.drawXYCenteredString(name, x + width / 2, y + height / 2 + fontYOffset, Color(255, 255, 255).rgb)
        }
    }
}