package net.ccbluex.liquidbounce.ui.client.clickgui.styles.astolfo

import net.ccbluex.liquidbounce.ui.font.Fonts
import net.minecraft.client.gui.FontRenderer
import net.minecraft.util.EnumChatFormatting.*
import java.awt.Color

object AstolfoConstants {
    const val PANEL_WIDTH = 100f
    const val PANEL_HEIGHT = 18f

    const val MODULE_HEIGHT = 18f

    const val VALUE_HEIGHT = 10f
    val FONT: FontRenderer
        get() = Fonts.fontMedium

    const val SLIDER_OFFSET = 0.02f

    val SELECTED_FORMAT = "$BOLD$UNDERLINE"

    val BACKGROUND_CATEGORY = Color(26, 26, 26).rgb
    val BACKGROUND_MODULE = Color(37, 37, 37).rgb
    val BACKGROUND_VALUE = Color(21, 21, 21).rgb
}

