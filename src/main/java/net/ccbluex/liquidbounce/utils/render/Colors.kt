package net.ccbluex.liquidbounce.utils.render

import net.minecraft.entity.EntityLivingBase
import java.awt.Color

enum class Colors(var c: Int) {
    BLACK(-16711423),
    BLUE(-12028161),
    DARKBLUE(-12621684),
    GREEN(-9830551),
    DARKGREEN(-9320847),
    WHITE(-65794),
    AQUA(-7820064),
    DARKAQUA(-12621684),
    GREY(-9868951),
    DARKGREY(-14342875),
    RED(-65536),
    DARKRED(-8388608),
    ORANGE(-29696),
    DARKORANGE(-2263808),
    YELLOW(-256),
    DARKYELLOW(-2702025),
    MAGENTA(-18751),
    DARKMAGENTA(-2252579);

    companion object {
        fun getColor(color: Color): Int {
            return getColor(color.red, color.green, color.blue, color.alpha)
        }

        fun getColor(brightness: Int): Int {
            return getColor(brightness, brightness, brightness, 255)
        }

        fun getColor(brightness: Int, alpha: Int): Int {
            return getColor(brightness, brightness, brightness, alpha)
        }

        fun getColor(red: Int, green: Int, blue: Int): Int {
            return getColor(red, green, blue, 255)
        }

        fun getColor(red: Int, green: Int, blue: Int, alpha: Int): Int {
            val color = 0
            var color1 = color or (alpha shl 24)
            color1 = color1 or (red shl 16)
            color1 = color1 or (green shl 8)
            return blue.let { color1 = color1 or it; color1 }
        }

        fun getHealthColor(entityLivingBase: EntityLivingBase): Color {
            val health = entityLivingBase.health
            val fractions = floatArrayOf(0.0f, 0.15f, 0.55f, 0.7f, 0.9f)
            val colors = arrayOf(Color(133, 0, 0), Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN)
            val progress = health / entityLivingBase.maxHealth
            return if (health >= 0.0f) BlendUtils.Companion.blendColors(fractions, colors, progress)!!.brighter() else colors[0]
        }
    }
}
