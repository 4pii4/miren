package net.ccbluex.liquidbounce.ui.client.clickgui.styles.newVer

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUI
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.ClickGuiStyle
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.newVer.element.CategoryElement
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.newVer.element.SearchElement
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.AnimationUtils
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MouseUtils.mouseWithinBounds
import net.ccbluex.liquidbounce.utils.extensions.setAlpha
import net.ccbluex.liquidbounce.utils.geom.Rectangle
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.ShaderUtils
import net.ccbluex.liquidbounce.utils.render.animations.Direction
import net.ccbluex.liquidbounce.utils.render.animations.impl.CustomAnimation
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.MathHelper
import org.apache.commons.lang3.tuple.MutablePair
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.awt.Color
import java.io.IOException
import java.util.function.Consumer
import kotlin.math.abs

/**
 * @author inf (original java code)
 * @author pie (refactored)
 */
class InfClickGui private constructor() : ClickGuiStyle("Inf") {
    private val categoryElements: MutableList<CategoryElement> = ArrayList()
    private var startYAnim = height / 2f
    private var endYAnim = height / 2f
    private var searchElement: SearchElement? = null
    private var fading = 0f

    private val backgroundColor = Color(16, 16, 16, 255)
    private val backgroundColor2 = Color(40, 40, 40, 255)

    var windowXStart = 30f
    var windowYStart = 30f
    var windowXEnd = 500f
    var windowYEnd = 400f
    private val windowWidth
        get() = abs(windowXEnd - windowXStart)
    private val windowHeight
        get() = abs(windowYEnd - windowYStart)
    private val minWindowWidth = 475f
    private val minWindowHeight = 350f

    private val searchXOffset = 10f
    private val searchYOffset = 30f

    var sideWidth = 120f
    private val categoryXOffset
        get() = sideWidth
    private val searchWidth
        get() = sideWidth - 10f
    private val searchHeight = 20f

    private val elementHeight = 24f
    private val elementsStartY = 55f

    private val categoriesTopMargin = 20f
    private val categoriesBottommargin = 20f

    private val xButtonColor = Color(0.2f, 0f, 0f, 1f)

    private var moveDragging = false
    private var resizeDragging = false
    private var splitDragging = false

    private var quad = Pair(0, 0)
    private val resizeArea = 12f
    private var x2 = 0f
    private var y2 = 0f
    private var xHoldOffset = 0f
    private var yHoldOffset = 0f

    private val anim = CustomAnimation(200, 1.0, Direction.FORWARDS) { EaseUtils.easeInCubic(it) }
//    private var xAnimDelta = 0f
//    private var yAnimDelta = 0f
//    private var lastMouseX = 0f
//    private var lastMouseY = 0f

    private val moveAera
        get() = Rectangle(windowXStart, windowYStart, windowWidth - 20f, 20f)
    private val splitArea
        get() = Rectangle(windowXStart + sideWidth - 5, windowYStart, 10f, windowHeight)


    init {
        ModuleCategory.values().forEach { categoryElements.add(CategoryElement(it)) }
        searchElement = SearchElement(windowXStart + searchXOffset, windowYStart + searchYOffset, searchWidth, searchHeight)
        categoryElements[0].focused = true
    }

    private fun reload() {
        categoryElements.clear()
        ModuleCategory.values().forEach { categoryElements.add(CategoryElement(it)) }
        categoryElements[0].focused = true
    }


    private fun determineQuadrant(mouseX: Int, mouseY: Int): Pair<Int, Int> {
        val result = MutablePair(0, 0)
        val offset2 = 0f
        if (mouseX.toFloat() in windowXStart-resizeArea..windowXStart-offset2) {
            result.left = -1
            xHoldOffset = mouseX - windowXStart
        }
        if (mouseX.toFloat() in windowXEnd+offset2..windowXEnd+resizeArea) {
            result.left = 1
            xHoldOffset = mouseX - windowXEnd
        }
        if (mouseY.toFloat() in windowYStart-resizeArea..windowYStart-offset2) {
            result.right = 1
            yHoldOffset = mouseY - windowYStart
        }
        if (mouseY.toFloat() in windowYEnd+offset2..windowYEnd+resizeArea) {
            result.right = -1
            yHoldOffset = mouseY - windowYEnd
        }
        return result.toPair()
    }

    private fun handleMove(mouseX: Int, mouseY: Int) { // handling move here? yeah prob
        if (moveDragging) {
            val w = windowWidth
            val h = windowHeight
            windowXStart = mouseX + x2
            windowYStart = mouseY + y2
            windowXEnd = windowXStart + w
            windowYEnd = windowYStart + h
//            lastMouseX = mouseX.toFloat()
//            lastMouseY = mouseY.toFloat()
        }

//        xAnimDelta = AnimationHelper.animation(xAnimDelta, lastMouseX, 0.01f)
//        yAnimDelta = AnimationHelper.animation(yAnimDelta, lastMouseY, 0.01f)
    }
    
//    private fun handlingPreRotationAnimation(): Boolean {
//        if (!isDoneRotatingAnimation()) {
//            GlStateManager.pushMatrix()
//            GlStateManager.translate(lastMouseX, lastMouseY, 0F)
//            GlStateManager.rotate((lastMouseX - xAnimDelta) / width.toFloat() * 180f + (lastMouseY - yAnimDelta) / (height.toFloat()) * 360f, 0f, 0f, 1f)
//            return true
//        }
//        return false
//    }
//
//    private fun handlingPostRotationAnimation() {
//        if (!isDoneRotatingAnimation())
//            GlStateManager.popMatrix()
//    }
//
//    private fun isDoneRotatingAnimation() = NewGUI.fastRenderValue.get() || (abs(lastMouseX - xAnimDelta) <= 0f && abs(lastMouseY - yAnimDelta) <= 0f)

    private fun handleResize(mouseX: Int, mouseY: Int) {
        val mouseX = mouseX - xHoldOffset
        val mouseY = mouseY - yHoldOffset
        if (resizeDragging) {
            val triangleColor = Color(255, 255, 255)
            when (quad.first to quad.second) {
                1 to 1 -> {
                    windowXEnd = mouseX.coerceAtLeast(windowXStart + minWindowWidth)
                    windowYStart = mouseY.coerceAtMost(windowYEnd - minWindowHeight)
                    RenderUtils.drawSquareTriangle(windowXEnd + resizeArea, windowYStart - resizeArea, -resizeArea, resizeArea, triangleColor, true)
                }
                -1 to -1 -> {
                    windowXStart = mouseX.coerceAtMost(windowXEnd - minWindowWidth)
                    windowYEnd = mouseY.coerceAtLeast(windowYStart + minWindowHeight)
                    RenderUtils.drawSquareTriangle(windowXStart - resizeArea, windowYEnd + resizeArea, resizeArea, -resizeArea, triangleColor, true)
                }

                -1 to 1 -> {
                    windowXStart = mouseX.coerceAtMost(windowXEnd - minWindowWidth)
                    windowYStart = mouseY.coerceAtMost(windowYEnd - minWindowHeight)
                    RenderUtils.drawSquareTriangle(windowXStart - resizeArea, windowYStart - resizeArea, resizeArea, resizeArea, triangleColor, true)
                }
                1 to -1 -> {
                    windowXEnd = mouseX.coerceAtLeast(windowXStart + minWindowWidth)
                    windowYEnd = mouseY.coerceAtLeast(windowYStart + minWindowHeight)
                    RenderUtils.drawSquareTriangle(windowXEnd + resizeArea, windowYEnd + resizeArea, -resizeArea, -resizeArea, triangleColor, true)
                }
            }
        }
    }

    private fun resetPositions() {
        windowXStart = 30f
        windowYStart = 30f
        windowXEnd = 500f
        windowYEnd = 400f
        resizeDragging = false
        moveDragging = false
    }

    private fun handleSplit(mouseX: Int) {
        if (splitDragging) {
            sideWidth = (mouseX - windowXStart).coerceIn(80f, windowWidth/2)
        }
    }

    private fun handleMisc() {
        if (Keyboard.isKeyDown(Keyboard.KEY_F12)) {
            resetPositions()
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_F5)) {
            reload()
        }
    }

    override fun initGui() {
        Keyboard.enableRepeatEvents(true)
        categoryElements.forEach { cat ->
            cat.moduleElements.filter { it.listeningKeybind() }.forEach { mod ->
                mod.resetState()
            }
        }

        super.initGui()
    }

    override fun onGuiClosed() {
        categoryElements.filter { it.focused }.map { it.handleMouseRelease(-1, -1, 0, 0f, 0f, 0f, 0f) }
        moveDragging = false
        resizeDragging = false
        splitDragging = false
        Keyboard.enableRepeatEvents(false)
        LiquidBounce.fileManager.saveConfigs(LiquidBounce.fileManager.valuesConfig)
    }

    override fun dumpConfig(): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("WindowXStart", windowXStart)
        jsonObject.addProperty("WindowYStart", windowYStart)
        jsonObject.addProperty("WindowXEnd", windowXEnd)
        jsonObject.addProperty("WindowYEnd", windowYEnd)

        return jsonObject
    }

    override fun loadConfig(json: JsonObject) {
        runCatching {
            windowXStart = json.get("WindowXStart").asFloat
            windowYStart = json.get("WindowYStart").asFloat
            windowXEnd = json.get("WindowXEnd").asFloat
            windowYEnd = json.get("WindowYEnd").asFloat
        }.onFailure {
            ClientUtils.logger.warn("Failed to load config")
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        handleMisc()
        handleMove(mouseX, mouseY)
        handleResize(mouseX, mouseY)
        handleSplit(mouseX)

//        if (handlingPreRotationAnimation())
//            drawFullSized(mouseX, mouseY, partialTicks, NewGUI.accentColor, -mouseX.toFloat(), -mouseY.toFloat())
//        else
//            drawFullSized(mouseX, mouseY, partialTicks, NewGUI.accentColor)
        drawFullSized(mouseX, mouseY, partialTicks, ClickGUI.accentColor)
//        handlingPostRotationAnimation()
    }

    private fun drawFullSized(mouseX: Int, mouseY: Int, partialTicks: Float, accentColor: Color, xOffset: Float = 0f, yOffset: Float = 0f) {
        val windowRadius = 4f
        ShaderUtils.drawRoundedRect((windowXStart + xOffset), (windowYStart + yOffset), (windowXEnd + xOffset), (windowYEnd + yOffset), windowRadius, backgroundColor)
        RenderUtils.customRounded((windowXStart + xOffset), (windowYStart + yOffset), (windowXEnd + xOffset), (windowYStart + yOffset) + 20f, windowRadius, windowRadius, 0f, 0f, backgroundColor2.rgb)

        // something to make it look more like windoze - inf, 2022
        if (mouseX.toFloat() in (windowXStart + xOffset)..(windowYStart + yOffset) && mouseY.toFloat() in (windowYStart + yOffset)..(windowYEnd + yOffset))
            fading += 0.2f * RenderUtils.deltaTime * 0.045f
        else
            fading -= 0.2f * RenderUtils.deltaTime * 0.045f
        fading = MathHelper.clamp_float(fading, 0f, 1f)
        xButtonColor.setAlpha(fading)
        RenderUtils.customRounded((windowXEnd + xOffset) - 20f, (windowYStart + yOffset), (windowXEnd + xOffset), (windowYStart + yOffset) + 20f, 0f, windowRadius, 0f, 0f, xButtonColor.rgb)
        GlStateManager.disableAlpha()
        RenderUtils.drawImage(IconManager.removeIcon, (windowXEnd + xOffset) - 15.0, (windowYStart + yOffset) + 5.0, 10.0, 10.0)
        GlStateManager.enableAlpha()

        // reset search pos
        searchElement!!.xPos = (windowXStart + xOffset) + searchXOffset
        searchElement!!.yPos = (windowYStart + yOffset) + searchYOffset
        searchElement!!.width = searchWidth

        // taken from searchBox's constructor
        searchElement!!.searchBox.width = searchWidth.toInt() - 4
        searchElement!!.searchBox.xPosition = ((windowXStart + xOffset) + searchXOffset + 2).toInt()
        searchElement!!.searchBox.yPosition = ((windowYStart + yOffset) + searchYOffset + 2).toInt()

        if (searchElement!!.drawBox(mouseX, mouseY, accentColor)) {
            searchElement!!.drawPanel(mouseX, mouseY, (windowXStart + xOffset) + categoryXOffset, (windowYStart + yOffset) + categoriesTopMargin, windowWidth - categoryXOffset, windowHeight - categoriesBottommargin, Mouse.getDWheel(), categoryElements, accentColor)
            return
        }

        var startY = (windowYStart + yOffset) + elementsStartY
        var lastFastYStart = 0f
        var lastFastYEnd = 0f

        for (ce in categoryElements) {
            ce.drawLabel(mouseX, mouseY, (windowXStart + xOffset), startY, categoryXOffset, elementHeight)
            if (ce.focused) {
                lastFastYStart = startY + 6f
                lastFastYEnd = startY + elementHeight - 6f
                startYAnim = if (ClickGUI.fastRenderValue.get())
                    startY + 6f
                             else
                                 AnimationUtils.animate(startY + 6f,
                                    startYAnim,
                                    (if (startYAnim - (startY + 5f) > 0) 0.65f else 0.55f) * RenderUtils.deltaTime * 0.025f
                                )
                endYAnim =  if (ClickGUI.fastRenderValue.get())
                                startY + elementHeight - 6f
                            else
                                AnimationUtils.animate(
                                    startY + elementHeight - 6f,
                                    endYAnim,
                                    (if (endYAnim - (startY + elementHeight - 5f) < 0) 0.65f else 0.55f) * RenderUtils.deltaTime * 0.025f
                                )
                ce.drawPanel(mouseX, mouseY, (windowXStart + xOffset) + categoryXOffset, (windowYStart + yOffset) + categoriesTopMargin, windowWidth - categoryXOffset, windowHeight - categoriesBottommargin, Mouse.getDWheel(), accentColor)
                Fonts.font40.drawString(ce.name, (windowXStart + xOffset) + 7, (windowYStart + yOffset) + 7, -1)
            }
            startY += elementHeight
        }
        val offset = 8f
        val drawYStart = if (resizeDragging || moveDragging) lastFastYStart else startYAnim
        val drawYEnd = if (resizeDragging || moveDragging) lastFastYEnd else endYAnim
        ShaderUtils.drawRoundedRect((windowXStart + xOffset) + 2f + offset, drawYStart, (windowXStart + xOffset) + 4f + offset, drawYEnd, 1f, accentColor)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        // search back button
        if (searchElement!!.isTyping() && Rectangle(windowXStart, windowYStart, 60f, 24f).contains(mouseX, mouseY)) {
            searchElement!!.searchBox.text = ""
            return
        }

        // window move
        if (moveAera.contains(mouseX, mouseY) && !moveDragging) {
            moveDragging = true
            x2 = windowXStart - mouseX
            y2 = windowYStart - mouseY
            return
        }

        // close button
        if (Rectangle(windowXEnd - 20, windowYStart, 20f, 20f).contains(mouseX, mouseY)) {
            mc.displayGuiScreen(null)
            return
        }

        if (splitArea.contains(mouseX, mouseY)) {
            splitDragging = true
            return
        }

        // window resize
        val quad2 = determineQuadrant(mouseX, mouseY)
        if (quad2.first != 0 && quad2.second != 0) {
            quad = quad2
            resizeDragging = true
            return
        }


        var startY = windowYStart + elementsStartY

        searchElement!!.handleMouseClick(mouseX, mouseY, mouseButton, windowXStart + categoryXOffset, windowYStart + categoriesTopMargin, windowWidth - categoryXOffset, windowHeight - categoriesBottommargin, categoryElements)
        if (!searchElement!!.isTyping()) {
            categoryElements.forEach { cat ->
                if (cat.focused)
                    cat.handleMouseClick(mouseX, mouseY, mouseButton, windowXStart + categoryXOffset, windowYStart + categoriesTopMargin, windowWidth - categoryXOffset, windowHeight - categoriesBottommargin)
                if (mouseWithinBounds(mouseX, mouseY, windowXStart, startY, windowXStart + categoryXOffset, startY + elementHeight) && !searchElement!!.isTyping()) {
                    categoryElements.forEach(Consumer { e: CategoryElement -> e.focused = false })
                    cat.focused = true
                    return
                }
                startY += elementHeight
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (moveDragging && moveAera.contains(mouseX, mouseY)) {
            moveDragging = false
            return
        }

        if (resizeDragging)
            resizeDragging = false

        if (splitDragging)
            splitDragging = false

        searchElement!!.handleMouseRelease(mouseX, mouseY, state, windowXStart + categoryXOffset, windowYStart + categoriesTopMargin, windowWidth - categoryXOffset, windowHeight - categoriesBottommargin, categoryElements)
        if (!searchElement!!.isTyping()) {
            categoryElements.filter { it.focused }.forEach { cat ->
                cat.handleMouseRelease(mouseX, mouseY, state, windowXStart + categoryXOffset, windowYStart + categoriesTopMargin, windowWidth - categoryXOffset, windowHeight - categoriesBottommargin)
            }
        }
        super.mouseReleased(mouseX, mouseY, state)
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        categoryElements.filter { it.focused }.forEach { cat ->
            if (cat.handleKeyTyped(typedChar, keyCode)) return
        }

        if (searchElement!!.handleTyping(typedChar, keyCode, windowXStart + categoryXOffset, windowYStart + categoriesTopMargin, windowWidth - categoryXOffset, windowHeight - categoriesBottommargin, categoryElements))
            return
        super.keyTyped(typedChar, keyCode)
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    companion object {
        private var instance: InfClickGui? = null
        fun getInstance(): InfClickGui {
            return if (instance == null) InfClickGui().also { instance = it } else instance!!
        }

        fun resetInstance() {
            instance = InfClickGui()
        }
    }
}
