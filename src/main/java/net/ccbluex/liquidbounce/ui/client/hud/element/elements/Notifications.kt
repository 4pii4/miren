/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 *
 * This code belongs to WYSI-Foundation. Please give credits when using this in your repository.
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce.hud
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.TextColorUtils
import net.ccbluex.liquidbounce.utils.render.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.BlurUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.Stencil
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import net.minecraft.util.StringUtils
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*

@ElementInfo(name = "Notifications", single = true)
class Notifications(
    x: Double = 0.0, y: Double = 30.0, scale: Float = 1F,
    side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)
) : Element(x, y, scale, side) {

    val styleValue = ListValue("Style", arrayOf("Full", "Compact", "Material", "Astolfo", "IntelliJ"), "Astolfo")
    val barValue = BoolValue("Bar", true) { styleValue.get().equals("material", true) }
    val bgAlphaValue = IntegerValue("Background-Alpha", 120, 0, 255) { !styleValue.get().equals("material", true) }

    val blurValue = BoolValue("Blur", false) { !styleValue.get().equals("material", true) }
    val blurStrength = FloatValue("Strength", 0F, 0F, 30F) { !styleValue.get().equals("material", true) && blurValue.get() }

    val hAnimModeValue = ListValue("H-Animation", arrayOf("LiquidBounce", "Smooth"), "LiquidBounce")
    val vAnimModeValue = ListValue("V-Animation", arrayOf("None", "Smooth"), "Smooth")
    val animationSpeed = FloatValue("Speed", 0.5F, 0.01F, 1F) { hAnimModeValue.get().equals("smooth", true) || vAnimModeValue.get().equals("smooth", true) }

    /**
     * Example notification for CustomHUD designer
     */
    private val exampleNotification = Notification("Example Notification", Type.WARNING, title = "Example Noti")

    /**
     * Draw element
     */
    override fun drawElement(): Border? {
        var animationY = 30F
        val notifications = mutableListOf<Notification>()

        for (i in hud.notifications)
            notifications.add(i)

        if (mc.currentScreen !is GuiHudDesigner || notifications.isNotEmpty()) {
            var indexz = 0
            for (i in notifications) {
                if (indexz == 0 && styleValue.get().equals("material", true) && side.vertical != Side.Vertical.DOWN) animationY -= i.notifHeight - (if (barValue.get()) 2F else 0F)
                i.drawNotification(animationY, this)
                if (indexz < notifications.size - 1) indexz++
                animationY += (when (styleValue.get().lowercase(Locale.getDefault())) {
                    "compact" -> 20F
                    "full" -> 30F
                    // 2px gap for astolfo and intellij
                    "astolfo" -> 28f
                    "intellij" -> 32f
                    else -> (if (side.vertical == Side.Vertical.DOWN) i.notifHeight else notifications[indexz].notifHeight) + 5F + (if (barValue.get()) 2F else 0F)
                }) * (if (side.vertical == Side.Vertical.DOWN) 1F else -1F)
            }
        } else {
            exampleNotification.drawNotification(
                animationY - if (styleValue.get().equals("material", true) && side.vertical != Side.Vertical.DOWN) (exampleNotification.notifHeight - 5F - (if (barValue.get()) 2F else 0F)) else 0F,
                this
            )
        }

        if (mc.currentScreen is GuiHudDesigner) {
            exampleNotification.fadeState = Notification.FadeState.STAY
            exampleNotification.x = if (styleValue.get().equals("material", true)) 160F else exampleNotification.getWidth(styleValue.get())

            if (exampleNotification.stayTimer.hasTimePassed(exampleNotification.displayTime))
                exampleNotification.stayTimer.reset()

            return getNotifBorder()
        }

        return null
    }

    // these are actually hardcoded sizes of the example notification
    private fun getNotifBorder() = when (styleValue.get().lowercase(Locale.getDefault())) {
        "full" -> Border(-125F, -58F, 0F, -30F)
        "compact" -> Border(-103F, -48F, 0F, -30F)
        "astolfo" -> Border(-133f, -56f, 0f, -30f)
        "intellij" -> Border(-129f, -60f, -0f, -30f)
        else -> Border(-160F, -54F, 0F, -30F)
    }
}

class Notification(message: String, type: Type, displayLength: Long, title: String = "") {
    private val notifyDir = "liquidbounce+/notification"

    private val fullSuccess = ResourceLocation("${notifyDir}/full/checkmark.png")
    private val fullError = ResourceLocation("${notifyDir}/full/error.png")
    private val fullWarning = ResourceLocation("${notifyDir}/full/warning.png")
    private val fullInfo = ResourceLocation("${notifyDir}/full/info.png")

    private val materialSuccess = ResourceLocation("${notifyDir}/material/checkmark.png")
    private val materialError = ResourceLocation("${notifyDir}/material/error.png")
    private val materialWarning = ResourceLocation("${notifyDir}/material/warning.png")
    private val materialInfo = ResourceLocation("${notifyDir}/material/info.png")


    private val astolfoSuccess = ResourceLocation("${notifyDir}/astolfo/checkmark.png")
    private val astolfoError = ResourceLocation("${notifyDir}/astolfo/error.png")
    private val astolfoWarning = ResourceLocation("${notifyDir}/astolfo/warning.png")
    private val astolfoInfo = ResourceLocation("${notifyDir}/astolfo/info.png")

    private val intellijSuccess = ResourceLocation("${notifyDir}/intellij/checkmark.png")
    private val intellijError = ResourceLocation("${notifyDir}/intellij/error.png")
    private val intellijWarning = ResourceLocation("${notifyDir}/intellij/warning.png")
    private val intellijInfo = ResourceLocation("${notifyDir}/intellij/info.png")

    var x = 0F
    var textLength = 0
    var fadeState = FadeState.IN
    var displayTime = 0L
    var stayTimer = MSTimer()
    var notifHeight = 0F
    private var message = ""
    var messageList: List<String>
    private var stay = 0F
    private var fadeStep = 0F
    private var firstY = 0f
    private var type: Type
    private var title: String

    init {
        this.message = message
        this.messageList = Fonts.font40.listFormattedStringToWidth(message, 105)
        this.notifHeight = messageList.size.toFloat() * (Fonts.font40.FONT_HEIGHT.toFloat() + 2F) + 8F
        this.type = type
        this.title = title
        this.displayTime = displayLength
        this.firstY = 19190F
        this.stayTimer.reset()
        this.textLength = Fonts.font40.getStringWidth(message)
    }

    constructor(message: String, type: Type, title: String) : this(message, type, 2000L, title)

    constructor(message: String, type: Type) : this(message, type, 2000L)

    constructor(message: String) : this(message, Type.INFO, 500L)

    constructor(message: String, displayLength: Long) : this(message, Type.INFO, displayLength)


    enum class FadeState {
        IN, STAY, OUT, END
    }

    private fun getAstolfoWidth(msg: String) = 20f + Fonts.minecraftNativeFont.getStringWidth(msg) + 8f
    private fun getIntelliJWidth(msg: String) = 12f + Fonts.minecraftNativeFont.getStringWidth(msg) + 12f

    fun getWidth(style: String) = when (style.lowercase()) {
        "material" -> 160F
        "astolfo" -> getAstolfoWidth(message)
        "intellij" -> getIntelliJWidth(message)
        else -> textLength + 8.0f
    }

    fun drawNotification(animationY: Float, parent: Notifications) {
        val delta = RenderUtils.deltaTime

        val style = parent.styleValue.get()
        val barMaterial = parent.barValue.get()

        val blur = parent.blurValue.get()
        val strength = parent.blurStrength.get()

        val hAnimMode = parent.hAnimModeValue.get()
        val vAnimMode = parent.vAnimModeValue.get()
        val animSpeed = parent.animationSpeed.get()

        val originalX = parent.renderX.toFloat()
        val originalY = parent.renderY.toFloat()

        val width = getWidth(style)
        val backgroundColor = Color(0, 0, 0, parent.bgAlphaValue.get())
        val enumColor = when (type) {
            Type.SUCCESS -> Color(80, 255, 80).rgb
            Type.ERROR -> Color(255, 80, 80).rgb
            Type.INFO -> Color(255, 255, 255).rgb
            Type.WARNING -> Color(255, 255, 0).rgb
        }

        firstY = if (vAnimMode.equals("smooth", true)) {
            if (firstY == 19190.0F)
                animationY
            else
                net.ccbluex.liquidbounce.utils.AnimationUtils.animate(animationY, firstY, 0.02F * delta)
        } else {
            animationY
        }

        val y = firstY

        when (style.lowercase(Locale.getDefault())) {
            "compact" -> {
                GlStateManager.resetColor()

                if (blur) {
                    GL11.glTranslatef(-originalX, -originalY, 0F)
                    GL11.glPushMatrix()
                    BlurUtils.blurAreaRounded(originalX + -x - 5F, originalY + -18F - y, originalX + -x + 8F + textLength, originalY + -y, 3F, strength)
                    GL11.glPopMatrix()
                    GL11.glTranslatef(originalX, originalY, 0F)
                }

                RenderUtils.customRounded(-x + 8F + textLength, -y, -x - 2F, -18F - y, 0F, 3F, 3F, 0F, backgroundColor.rgb)
                RenderUtils.customRounded(
                    -x - 2F, -y, -x - 5F, -18F - y, 3F, 0F, 0F, 3F, when (type) {
                        Type.SUCCESS -> Color(80, 255, 80).rgb
                        Type.ERROR -> Color(255, 80, 80).rgb
                        Type.INFO -> Color(255, 255, 255).rgb
                        Type.WARNING -> Color(255, 255, 0).rgb
                    }
                )

                GlStateManager.resetColor()
                Fonts.font40.drawString(message, -x + 3, -13F - y, -1)
            }

            "full" -> {
                val dist = (x + 1 + 26F) - (x - 8 - textLength)
                val kek = -x - 1 - 26F

                GlStateManager.resetColor()

                if (blur) {
                    GL11.glTranslatef(-originalX, -originalY, 0F)
                    GL11.glPushMatrix()
                    BlurUtils.blurArea(originalX + kek, originalY + -28F - y, originalX + -x + 8 + textLength, originalY + -y, strength)
                    GL11.glPopMatrix()
                    GL11.glTranslatef(originalX, originalY, 0F)
                }

                RenderUtils.drawRect(-x + 8 + textLength, -y, kek, -28F - y, backgroundColor.rgb)

                GL11.glPushMatrix()
                GlStateManager.disableAlpha()
                RenderUtils.drawImage2(
                    when (type) {
                        Type.SUCCESS -> fullSuccess
                        Type.ERROR -> fullError
                        Type.WARNING -> fullWarning
                        Type.INFO -> fullInfo
                    }, kek, -27F - y, 26, 26
                )
                GlStateManager.enableAlpha()
                GL11.glPopMatrix()

                GlStateManager.resetColor()
                if (fadeState == FadeState.STAY && !stayTimer.hasTimePassed(displayTime))
                    RenderUtils.drawRect(kek, -y, kek + (dist * if (stayTimer.hasTimePassed(displayTime)) 0F else ((displayTime - (System.currentTimeMillis() - stayTimer.time)).toFloat() / displayTime.toFloat())), -1F - y, enumColor)
                else if (fadeState == FadeState.IN)
                    RenderUtils.drawRect(kek, -y, kek + dist, -1F - y, enumColor)

                GlStateManager.resetColor()
                Fonts.font40.drawString(message, -x + 2, -18F - y, -1)
            }

            "astolfo" -> {
                val afont = Fonts.minecraftNativeFont
                val notifyWidth = getAstolfoWidth(message)
                val notifyHeight = 26f

                GlStateManager.resetColor()
                GL11.glPushMatrix()
                GL11.glTranslatef(-x, -y - notifyHeight, 0f)
                RenderUtils.drawRect(
                    0f, 0f, notifyWidth, notifyHeight, Color(0, 0, 0, 140)
                )
                RenderUtils.drawRect(0f, notifyHeight - 1.5f, notifyWidth * (1 - ((System.currentTimeMillis() - stayTimer.time) / displayTime.toFloat())).coerceIn(0f, 1f), notifyHeight, enumColor)

                GL11.glPushMatrix()
                GlStateManager.disableAlpha()
                val c = Color(enumColor)
                GL11.glColor4f(c.red / 255f, c.green / 255f, c.blue / 255f, 1f)
                GL11.glScalef(0.5f, 0.5f, 0.5f)
                RenderUtils.drawImage3(
                    when (type) {
                        Type.SUCCESS -> astolfoSuccess
                        Type.ERROR -> astolfoError
                        Type.WARNING -> astolfoWarning
                        Type.INFO -> astolfoInfo
                    }, 8f, 10f, 32, 32, c.red / 255f, c.green / 255f, c.blue / 255f, 1f
                )
                GlStateManager.enableAlpha()
                GL11.glPopMatrix()

                afont.drawString(this.title.ifEmpty { type.titlecasedName() }, 24f, 5f, -1, true)
                afont.drawString(TextColorUtils.gray(StringUtils.stripControlCodes(message)), 24f, 14f, -1, true)
                GL11.glPopMatrix()
                GlStateManager.resetColor()

            }

            "intellij" -> {
                val jfont = Fonts.minecraftNativeFont
                val notifyWidth = getIntelliJWidth(message)
                val notifyHeight = 30f
                val primaryColor: Color
                val secondaryColor: Color

                when (type) {
                    Type.ERROR -> {
                        secondaryColor = Color(115, 69, 75)
                        primaryColor = Color(89, 61, 65)
                    }

                    Type.INFO -> {
                        secondaryColor = Color(70, 94, 115)
                        primaryColor = Color(61, 72, 87)
                    }

                    Type.SUCCESS -> {
                        secondaryColor = Color(67, 104, 67)
                        primaryColor = Color(55, 78, 55)
                    }

                    Type.WARNING -> {
                        secondaryColor = Color(103, 103, 63)
                        primaryColor = Color(80, 80, 57)
                    }
                }

                GlStateManager.resetColor()
                GL11.glPushMatrix()
                GL11.glTranslatef(-x, -y - notifyHeight, 0f)
                RenderUtils.drawRect(0f, 0f, notifyWidth, notifyHeight, secondaryColor)
                RenderUtils.drawRect(1f, 1f, notifyWidth - 1f, notifyHeight - 1f, primaryColor)

                GL11.glPushMatrix()
                GL11.glTranslatef(4f, 6f, 0f)
                GlStateManager.resetColor()
                GL11.glColor4f(1f, 1f, 1f, 1f)
                RenderUtils.drawImage2(
                    when (type) {
                        Type.SUCCESS -> intellijSuccess
                        Type.ERROR -> intellijError
                        Type.WARNING -> intellijWarning
                        Type.INFO -> intellijInfo
                    }, 0f, 0f, 7, 7
                )
                GL11.glPopMatrix()

                jfont.drawString(this.title.ifEmpty { type.titlecasedName() }, 15f, 6f, enumColor, false)
                jfont.drawString(StringUtils.stripControlCodes(message), 15f, 17f, -1, false)

                GL11.glPopMatrix()
            }

            "material" -> {
                GlStateManager.resetColor()

                GL11.glPushMatrix()
                GL11.glTranslatef(-x, -y - notifHeight - (if (barMaterial) 2F else 0F), 0F)

                RenderUtils.originalRoundedRect(
                    1F, -1F, 159F, notifHeight + (if (barMaterial) 2F else 0F) + 1F, 1F, when (type) {
                        Type.SUCCESS -> Color(72, 210, 48, 70).rgb
                        Type.ERROR -> Color(227, 28, 28, 70).rgb
                        Type.WARNING -> Color(245, 212, 25, 70).rgb
                        Type.INFO -> Color(255, 255, 255, 70).rgb
                    }
                )
                RenderUtils.originalRoundedRect(
                    -1F, 1F, 161F, notifHeight + (if (barMaterial) 2F else 0F) - 1F, 1F, when (type) {
                        Type.SUCCESS -> Color(72, 210, 48, 70).rgb
                        Type.ERROR -> Color(227, 28, 28, 70).rgb
                        Type.WARNING -> Color(245, 212, 25, 70).rgb
                        Type.INFO -> Color(255, 255, 255, 70).rgb
                    }
                )
                RenderUtils.originalRoundedRect(
                    -0.5F, -0.5F, 160.5F, notifHeight + (if (barMaterial) 2F else 0F) + 0.5F, 1F, when (type) {
                        Type.SUCCESS -> Color(72, 210, 48, 80).rgb
                        Type.ERROR -> Color(227, 28, 28, 80).rgb
                        Type.WARNING -> Color(245, 212, 25, 80).rgb
                        Type.INFO -> Color(255, 255, 255, 80).rgb
                    }
                )

                if (barMaterial) {
                    Stencil.write(true)
                    RenderUtils.originalRoundedRect(
                        0F, 0F, 160F, notifHeight + 2F, 1F, when (type) {
                            Type.SUCCESS -> Color(72, 210, 48, 255).rgb
                            Type.ERROR -> Color(227, 28, 28, 255).rgb
                            Type.WARNING -> Color(245, 212, 25, 255).rgb
                            Type.INFO -> Color(255, 255, 255, 255).rgb
                        }
                    )
                    Stencil.erase(true)
                    if (fadeState == FadeState.STAY) RenderUtils.newDrawRect(
                        0F, notifHeight, 160F * if (stayTimer.hasTimePassed(displayTime)) 1F else ((System.currentTimeMillis() - stayTimer.time).toFloat() / displayTime.toFloat()), notifHeight + 2F, when (type) {
                            Type.SUCCESS -> Color(72 + 90, 210 + 30, 48 + 90, 255).rgb
                            Type.ERROR -> Color(227 + 20, 28 + 90, 28 + 90, 255).rgb
                            Type.WARNING -> Color(245 - 70, 212 - 70, 25, 255).rgb
                            Type.INFO -> Color(155, 155, 155, 255).rgb
                        }
                    )
                    Stencil.dispose()
                } else RenderUtils.originalRoundedRect(
                    0F, 0F, 160F, notifHeight, 1F, when (type) {
                        Type.SUCCESS -> Color(72, 210, 48, 255).rgb
                        Type.ERROR -> Color(227, 28, 28, 255).rgb
                        Type.WARNING -> Color(245, 212, 25, 255).rgb
                        Type.INFO -> Color(255, 255, 255, 255).rgb
                    }
                )

                var yHeight = 7F
                for (s in messageList) {
                    Fonts.font40.drawString(s, 30F, yHeight, if (type == Type.ERROR) -1 else 0)
                    yHeight += Fonts.font40.FONT_HEIGHT.toFloat() + 2F
                }

                GL11.glPushMatrix()
                GlStateManager.disableAlpha()
                RenderUtils.drawImage3(
                    when (type) {
                        Type.SUCCESS -> materialSuccess
                        Type.ERROR -> materialError
                        Type.WARNING -> materialWarning
                        Type.INFO -> materialInfo
                    }, 9F, notifHeight / 2F - 6F, 12, 12,
                    if (type == Type.ERROR) 1F else 0F,
                    if (type == Type.ERROR) 1F else 0F,
                    if (type == Type.ERROR) 1F else 0F, 1F
                )
                GlStateManager.enableAlpha()
                GL11.glPopMatrix()

                GL11.glPopMatrix()

                GlStateManager.resetColor()
            }
        }

        when (fadeState) {
            FadeState.IN -> {
                if (x < width) {
                    x = if (hAnimMode.equals("smooth", true))
                        net.ccbluex.liquidbounce.utils.AnimationUtils.animate(width, x, animSpeed * 0.025F * delta)
                    else
                        AnimationUtils.easeOut(fadeStep, width) * width
                    fadeStep += delta / 4F
                }
                if (x >= width) {
                    fadeState = FadeState.STAY
                    x = width
                    fadeStep = width
                }

                stay = 60F
                stayTimer.reset()
            }

            FadeState.STAY -> {
                if (stay > 0) {
                    stay = 0F
                    stayTimer.reset()
                }
                if (stayTimer.hasTimePassed(displayTime))
                    fadeState = FadeState.OUT
            }

            FadeState.OUT -> if (x > 0) {
                x = if (hAnimMode.equals("smooth", true))
                    net.ccbluex.liquidbounce.utils.AnimationUtils.animate(-width / 2F, x, animSpeed * 0.025F * delta)
                else
                    AnimationUtils.easeOut(fadeStep, width) * width

                fadeStep -= delta / 4F
            } else
                fadeState = FadeState.END

            FadeState.END -> hud.removeNotification(this)
        }
    }
}

@Suppress("DEPRECATION")
enum class Type {
    SUCCESS, INFO, WARNING, ERROR;

    fun titlecasedName() = this.toString().capitalize()
}