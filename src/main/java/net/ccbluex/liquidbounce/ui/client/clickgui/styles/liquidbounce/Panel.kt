package net.ccbluex.liquidbounce.ui.client.clickgui.styles.liquidbounce

import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUI
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.animations.Direction
import net.ccbluex.liquidbounce.utils.render.animations.impl.CustomAnimation
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.util.ResourceLocation
import net.minecraft.util.StringUtils
import java.util.*
import kotlin.collections.ArrayList


abstract class Panel(val name: String, var x: Int, var y: Int, val width: Int, val height: Int, var open: Boolean) : MinecraftInstance() {
    var x2 = 0
    var y2 = 0
    private var scroll = 0
    var dragged = 0
        private set
    var drag = false
    var scrollbar = false
        private set
    val elements: MutableList<Element>
    var isVisible = true
    private var elementsHeight = 0f
    var fade = 0f
    val anim = CustomAnimation(200, 1.0, Direction.FORWARDS) { EaseUtils.easeInQuad(it) }

    init {
        elements = ArrayList()
        setupItems()
    }

    abstract fun setupItems()
    fun drawScreen(mouseX: Int, mouseY: Int, button: Float) {
        if (!isVisible) return
        val maxElements: Int = ClickGUI.maxElements

        // Drag
        if (drag) {
            val nx = x2 + mouseX
            val ny = y2 + mouseY
            if (nx > -1) x = nx
            if (ny > -1) y = ny
        }
        elementsHeight = (getElementsHeight() - 1).toFloat()
        val scrollbar = elements.size >= maxElements
        if (this.scrollbar != scrollbar) this.scrollbar = scrollbar
        LiquidBounceStyle.getInstance().drawPanel(mouseX, mouseY, this)
        var y = y + height - 2
        var count = 0
        for (element in elements) {
            if (++count > scroll && count < scroll + (maxElements + 1) && scroll < elements.size) {
                element.setLocation(x, y)
                element.width = width
//                if (y <= this.y + fade) element.drawScreen(mouseX, mouseY, button)
                element.preDrawScreen(this.x + 0f, this.y + 0f, this.x + this.width + 0f, this.y + this.height + fade)
                element.drawScreen(mouseX, mouseY, button, this)
                y += element.height + 1
                element.isVisible = true
            } else element.isVisible = false
        }
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (!isVisible) return false
        if (mouseButton == 1 && isHovering(mouseX, mouseY)) {
            open = !open
            mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("random.bow"), 1.0f))
            return true
        }
        for (element in elements) {
            if (element.y <= y + fade && element.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true
            }
        }
        return false
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, state: Int): Boolean {
        if (!isVisible) return false
        drag = false
        if (!open) return false
        for (element in elements) {
            if (element.y <= y + fade && element.mouseReleased(mouseX, mouseY, state)) {
                return true
            }
        }
        return false
    }

    fun handleScroll(mouseX: Int, mouseY: Int, wheel: Int): Boolean {
        val maxElements: Int = ClickGUI.maxElements
        if (mouseX >= x && mouseX <= x + 100 && mouseY >= y && mouseY <= y + 19 + elementsHeight) {
            if (wheel < 0 && scroll < elements.size - maxElements) {
                ++scroll
                if (scroll < 0) scroll = 0
            } else if (wheel > 0) {
                --scroll
                if (scroll < 0) scroll = 0
            }
            if (wheel < 0) {
                if (dragged < elements.size - maxElements) ++dragged
            } else if (wheel > 0 && dragged >= 1) {
                --dragged
            }
            return true
        }
        return false
    }

    fun updateFade(delta: Int) {
        fade = if (ClickGUI.animationValue.equals("none", true)) {
            if (open) elementsHeight else 0f
        } else {
            anim.setDirection(if (open) Direction.FORWARDS else Direction.BACKWARDS)
            elementsHeight * anim.outputFloat.coerceIn(0f, 1f)
        }
    }

    private fun getElementsHeight(): Int {
        var height = 0
        var count = 0
        for (element in elements) {
            if (count >= ClickGUI.maxElements) continue
            height += element.height + 1
            ++count
        }
        return height
    }

    fun isHovering(mouseX: Int, mouseY: Int): Boolean {
        val textWidth = mc.fontRendererObj.getStringWidth(StringUtils.stripControlCodes(name)) - 100f
        return mouseX >= x - textWidth / 2f - 19f && mouseX <= x - textWidth / 2f + mc.fontRendererObj.getStringWidth(StringUtils.stripControlCodes(name)) + 19f && mouseY >= y && mouseY <= y + height - if (open) 2 else 0
    }
}