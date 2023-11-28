/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 * 
 * This code belongs to WYSI-Foundation. Please give credits when using this in your repository.
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.render.BlendUtils
import net.ccbluex.liquidbounce.value.ColorElement
import net.ccbluex.liquidbounce.value.IntegerValue
import java.awt.Color

@ModuleInfo(name = "ColorMixer", description = "Mix two colors together.", category = ModuleCategory.RENDER, canEnable = false)
object ColorMixer : Module() {

    val blendAmount: IntegerValue = object : IntegerValue("Mixer-Amount", 2, 2, 10) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            regenerateColors(oldValue !== newValue)
        }
    }

    /*
    @Override
    public void onInitialize() {
        regenerateColors();
    }
    */
    val col1RedValue: ColorElement = ColorElement(1, ColorElement.Material.RED)
    val col1GreenValue: ColorElement = ColorElement(1, ColorElement.Material.GREEN)
    val col1BlueValue: ColorElement = ColorElement(1, ColorElement.Material.BLUE)
    val col2RedValue: ColorElement = ColorElement(2, ColorElement.Material.RED)
    val col2GreenValue: ColorElement = ColorElement(2, ColorElement.Material.GREEN)
    val col2BlueValue: ColorElement = ColorElement(2, ColorElement.Material.BLUE)
    val col3RedValue: ColorElement = ColorElement(3, ColorElement.Material.RED, blendAmount)
    val col3GreenValue: ColorElement = ColorElement(3, ColorElement.Material.GREEN, blendAmount)
    val col3BlueValue: ColorElement = ColorElement(3, ColorElement.Material.BLUE, blendAmount)
    val col4RedValue: ColorElement = ColorElement(4, ColorElement.Material.RED, blendAmount)
    val col4GreenValue: ColorElement = ColorElement(4, ColorElement.Material.GREEN, blendAmount)
    val col4BlueValue: ColorElement = ColorElement(4, ColorElement.Material.BLUE, blendAmount)
    val col5RedValue: ColorElement = ColorElement(5, ColorElement.Material.RED, blendAmount)
    val col5GreenValue: ColorElement = ColorElement(5, ColorElement.Material.GREEN, blendAmount)
    val col5BlueValue: ColorElement = ColorElement(5, ColorElement.Material.BLUE, blendAmount)
    val col6RedValue: ColorElement = ColorElement(6, ColorElement.Material.RED, blendAmount)
    val col6GreenValue: ColorElement = ColorElement(6, ColorElement.Material.GREEN, blendAmount)
    val col6BlueValue: ColorElement = ColorElement(6, ColorElement.Material.BLUE, blendAmount)
    val col7RedValue: ColorElement = ColorElement(7, ColorElement.Material.RED, blendAmount)
    val col7GreenValue: ColorElement = ColorElement(7, ColorElement.Material.GREEN, blendAmount)
    val col7BlueValue: ColorElement = ColorElement(7, ColorElement.Material.BLUE, blendAmount)
    val col8RedValue: ColorElement = ColorElement(8, ColorElement.Material.RED, blendAmount)
    val col8GreenValue: ColorElement = ColorElement(8, ColorElement.Material.GREEN, blendAmount)
    val col8BlueValue: ColorElement = ColorElement(8, ColorElement.Material.BLUE, blendAmount)
    val col9RedValue: ColorElement = ColorElement(9, ColorElement.Material.RED, blendAmount)
    val col9GreenValue: ColorElement = ColorElement(9, ColorElement.Material.GREEN, blendAmount)
    val col9BlueValue: ColorElement = ColorElement(9, ColorElement.Material.BLUE, blendAmount)
    val col10RedValue: ColorElement = ColorElement(10, ColorElement.Material.RED, blendAmount)
    val col10GreenValue: ColorElement = ColorElement(10, ColorElement.Material.GREEN, blendAmount)
    val col10BlueValue: ColorElement = ColorElement(10, ColorElement.Material.BLUE, blendAmount)

    private var lastFraction = floatArrayOf()
    var lastColors = arrayOf<Color?>()

    @JvmStatic
    fun getMixedColor(index: Int, seconds: Int): Color {
        val colMixer = LiquidBounce.moduleManager.getModule(ColorMixer::class.java) ?: return Color.white
        if (lastColors.isEmpty() || lastFraction.size <= 0) regenerateColors(true) // just to make sure it won't go white
        return BlendUtils.blendColors(lastFraction, lastColors, (System.currentTimeMillis() + index) % (seconds * 1000) / (seconds * 1000).toFloat())
    }

    fun regenerateColors(forceValue: Boolean) {
        val colMixer = LiquidBounce.moduleManager.getModule(ColorMixer::class.java) ?: return

        // color generation
        if (forceValue || lastColors.isEmpty() || lastColors.size != colMixer.blendAmount.get() * 2 - 1) {
            val generator = arrayOfNulls<Color>(colMixer.blendAmount.get() * 2 - 1)

            // reflection is cool
            for (i in 1..colMixer.blendAmount.get()) {
                var result = Color.white
                try {
                    val red = ColorMixer::class.java.getField("col" + i + "RedValue")
                    val green = ColorMixer::class.java.getField("col" + i + "GreenValue")
                    val blue = ColorMixer::class.java.getField("col" + i + "BlueValue")
                    val r: Int = (red[colMixer] as ColorElement).get()
                    val g: Int = (green[colMixer] as ColorElement).get()
                    val b: Int = (blue[colMixer] as ColorElement).get()
                    result = Color(r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                generator[i - 1] = result
            }
            var h = colMixer.blendAmount.get()
            for (z in colMixer.blendAmount.get() - 2 downTo 0) {
                generator[h] = generator[z]
                h++
            }
            lastColors = generator
        }

        // cache thingy
        if (forceValue || lastFraction.isEmpty() || lastFraction.size != colMixer.blendAmount.get() * 2 - 1) {
            // color frac regenerate if necessary
            val colorFraction = FloatArray(colMixer.blendAmount.get() * 2 - 1)
            for (i in 0..colMixer.blendAmount.get() * 2 - 2) {
                colorFraction[i] = i.toFloat() / (colMixer.blendAmount.get() * 2 - 2).toFloat()
            }
            lastFraction = colorFraction
        }
    }

}