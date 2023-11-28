/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.TextColorUtils.green
import net.ccbluex.liquidbounce.utils.TextColorUtils.red
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.*
import java.awt.Color
import java.util.*

@ElementInfo(name = "ModuleInfo")
class ModuleInfo(x: Double = -8.0, y: Double = 57.0, scale: Float = 1F, side: Side = Side(Side.Horizontal.MIDDLE, Side.Vertical.DOWN)) : Element(x, y, scale, side) {
    val modulesValue = TextValue("Modules", "killaura,criticals,speed,fly") { false }
    private val font by  FontValue("Font", Fonts.minecraftNativeFont)
    private val spacedName by BoolValue("SpacedName", false)
    private val case by ListValue("Case", arrayOf("Upper", "Lower", "Normal"), "Normal")
    private val coloredState by BoolValue("ColoredState", true)
    private val fontYOffset by FloatValue("FontYOffset", 0f, -10f, 10f)
    private val modules: List<Module>
        get() = modulesValue.get().split(",").mapNotNull { LiquidBounce.moduleManager.getModule(it) }

    override fun drawElement(): Border {
        val pairs = mutableListOf<Pair<String, String>>()

        if (modules.isEmpty())
            return Border(0f, 0f, 40f, font.FONT_HEIGHT.toFloat())

        pairs.add("Module Info" to "")

        for (module in modules) {
            val name = if (spacedName) module.spacedName else module.name
            var state = module.state.toString().capitalize()
            if (coloredState)
                state = if (module.state) green(state) else red(state)
            when (case) {
                "Upper" -> pairs.add(name.uppercase() to state.uppercase())
                "Lower" -> pairs.add(name.lowercase() to state.lowercase())
                "Normal" -> pairs.add(name to state)
            }
        }

        val maxWidth1 = pairs.maxOf { font.getStringWidth(it.first) + 0f}
        val maxWidth2 = pairs.maxOf { font.getStringWidth(it.second) + 4f}

        RenderUtils.drawRect(0f, 0f, maxWidth1 + maxWidth2, font.FONT_HEIGHT.toFloat(), Color(0, 0, 0, 180))
        font.drawStringWithShadow(pairs[0].first, 2f, 1f + fontYOffset, -1)
        pairs[0] = "" to ""

        for ((index, pair) in pairs.withIndex().drop(1)) {
            RenderUtils.drawRect(0, index * font.FONT_HEIGHT, (maxWidth1 + maxWidth2).toInt(), (index+1)*font.FONT_HEIGHT, Color(0,0,0, 140).rgb)
            font.drawStringWithShadow(pair.first, 2f, index * font.FONT_HEIGHT + 1f + fontYOffset, -1)
            font.drawStringWithShadow(pair.second, maxWidth1 + 2f, index * font.FONT_HEIGHT + 1f + fontYOffset, -1)
        }

        return Border(0f, 0f, maxWidth1 + maxWidth2, pairs.size * font.FONT_HEIGHT.toFloat())
    }
}