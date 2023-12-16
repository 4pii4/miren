/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.render.ColorMixer
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.ClickGuiStyle
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoClickGui
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.liquidbounce.LiquidBounceClickGui
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.newVer.InfClickGui
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.GuiScreen
import java.awt.Color
import java.util.*


@ModuleInfo(name = "ClickGUI", description = "ClickGUI.", category = ModuleCategory.CLIENT, forceNoSound = true, onlyEnable = true)
object ClickGUI : Module() {
    // thanks inf for deduplication method
    private val clickGUIs: List<ClickGuiStyle> = arrayOf(
        LiquidBounceClickGui::class.java,
        AstolfoClickGui::class.java,
        InfClickGui::class.java,
    ).map { it.newInstance() }

    val style by ListValue("Style", clickGUIs.map { it.name }.toTypedArray(), "Astolfo")
    val scale by FloatValue("Scale", 1f, 0f, 10f)
    val scroll by FloatValue("Scroll", 20f, 0f, 200f) { style.equals("Astolfo", true) }

    val lbStyle by ListValue("LiquidBounceStyle", arrayOf("Black", "Blue", "White", "Astolfo"), "Black")
    val lbMaxElement by IntegerValue("MaxElements", 5, 1, 10) { style.equals("liquidbounce", true)}
    val lbAnimation by ListValue("Animation", arrayOf("Azura", "Slide", "SlideBounce", "Zoom", "ZoomBounce", "None"), "Azura") { style.equals("liquidbounce", true)}
    val lbAnimSpeed by FloatValue("AnimSpeed", 1f, 0.01f, 5f, "x") { style.equals("liquidbounce", true) && !lbAnimation.equals("none", true)}
    private val backgroundValue by ListValue("Background", arrayOf("Default", "Gradient", "None"), "Default")
    private val gradStartValue by IntegerValue("GradientStartAlpha", 255, 0, 255) { backgroundValue.equals("gradient", true) }
    private val gradEndValue by IntegerValue("GradientEndAlpha", 0, 0, 255) { backgroundValue.equals("gradient", true) }

    val fastRenderValue = BoolValue("FastRender", false) { style.equals("inf", true) }
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Sky", "Rainbow", "LiquidSlowly", "Fade", "Mixer"), "Custom")
    private val colorRedValue = IntegerValue("Red", 0, 0, 255)
    private val colorGreenValue = IntegerValue("Green", 140, 0, 255)
    private val colorBlueValue = IntegerValue("Blue", 255, 0, 255)
    private val saturationValue = FloatValue("Saturation", 1f, 0f, 1f)
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)
    private val mixerSecondsValue = IntegerValue("Seconds", 2, 1, 10)

    val accentColor: Color
        get() {
            var c = Color(255, 255, 255, 255)
            when (colorModeValue.get().lowercase(Locale.getDefault())) {
                "custom" -> c = Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
                "rainbow" -> c = Color(RenderUtils.getRainbowOpaque(mixerSecondsValue.get(), saturationValue.get(), brightnessValue.get(), 0))
                "sky" -> c = RenderUtils.skyRainbow(0, saturationValue.get(), brightnessValue.get())
                "liquidslowly" -> c = ColorUtils.LiquidSlowly(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get())
                "fade" -> c = ColorUtils.fade(Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), 0, 100)
                "mixer" -> c = ColorMixer.getMixedColor(0, mixerSecondsValue.get())
            }
            return c
        }

    override fun onEnable() {
        mc.displayGuiScreen(clickGUIs.find { it.name.equals(style, true) }!!.instance)
    }

    fun dumpConfig(): JsonObject {
        val config = JsonObject()
        clickGUIs.map { style -> config.add(style.name, style.instance!!.dumpConfig()) }
        return config
    }

    fun loadConfig(json: JsonObject) {
        clickGUIs.map { style -> style.instance!!.loadConfig(json.get(style.name).asJsonObject) }
    }

    fun resetClickGUIs() {
        clickGUIs.map { style -> style.resetInstance() }
    }

    fun drawBackground(screen: GuiScreen) {
        when (backgroundValue.lowercase()) {
            "default" -> screen.drawDefaultBackground()
            "gradient" -> screen.drawGradientRect(
                0, 0, screen.width, screen.height,
                ColorUtils.reAlpha(accentColor, gradStartValue).rgb,
                ColorUtils.reAlpha(accentColor, gradEndValue).rgb
            )

            else -> { }
        }
    }
}