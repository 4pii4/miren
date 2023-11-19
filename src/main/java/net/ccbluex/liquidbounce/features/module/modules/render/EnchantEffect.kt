/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 * 
 * This code belongs to WYSI-Foundation. Please give credits when using this in your repository.
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue

@ModuleInfo(name = "EnchantEffect", spacedName = "Enchant Effect", description = "qwq", category = ModuleCategory.RENDER)
class EnchantEffect : Module() {
    var redValue = IntegerValue("Red", 255, 0, 255)
    var greenValue = IntegerValue("Green", 0, 0, 255)
    var blueValue = IntegerValue("Blue", 0, 0, 255)
    var modeValue = ListValue("Mode", arrayOf("Custom", "Rainbow", "Sky", "Mixer"), "Custom")
    var rainbowSpeedValue = IntegerValue("Seconds", 1, 1, 6)
    var rainbowDelayValue = IntegerValue("Delay", 5, 0, 10)
    var rainbowSatValue = FloatValue("Saturation", 1.0f, 0.0f, 1.0f)
    var rainbowBrgValue = FloatValue("Brightness", 1.0f, 0.0f, 1.0f)
}