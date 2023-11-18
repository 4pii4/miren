/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.render.ColorMixer
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo.AstolfoClickGui
import net.ccbluex.liquidbounce.ui.client.clickgui.styles.newVer.NewUi
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import java.awt.Color
import java.util.*

@ModuleInfo(name = "ClickGUI", description = "Opens the ClickGUI.", category = ModuleCategory.CLIENT, forceNoSound = true, onlyEnable = true)
object ClickGUI : Module() {
    val style by ListValue("Style", arrayOf("Inf", "Astolfo"), "Inf")
    val scale by FloatValue("Scale", 1f, 0f, 10f) { style.equals("Astolfo", true) }
    val scroll by FloatValue("Scroll", 20f, 0f, 200f) { style.equals("Astolfo", true) }

    val fastRenderValue = BoolValue("FastRender", false) { style.equals("inf", true) }
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Sky", "Rainbow", "LiquidSlowly", "Fade", "Mixer"), "Custom") { style.equals("inf", true) }
    private val colorRedValue = IntegerValue("Red", 0, 0, 255) { style.equals("inf", true) }
    private val colorGreenValue = IntegerValue("Green", 140, 0, 255) { style.equals("inf", true) }
    private val colorBlueValue = IntegerValue("Blue", 255, 0, 255) { style.equals("inf", true) }
    private val saturationValue = FloatValue("Saturation", 1f, 0f, 1f) { style.equals("inf", true) }
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f) { style.equals("inf", true) }
    private val mixerSecondsValue = IntegerValue("Seconds", 2, 1, 10) { style.equals("inf", true) }

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
        when (style) {
            "Astolfo" -> mc.displayGuiScreen(AstolfoClickGui.getInstance())
            "Inf" -> mc.displayGuiScreen(NewUi.getInstance())
        }
    }
}