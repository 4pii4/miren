/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils

import net.minecraft.client.gui.GuiTextField

object TabUtils {
    fun tab(vararg textFields: GuiTextField) {
        var i = 0
        while (i < textFields.size) {
            val textField = textFields[i]
            if (textField.isFocused) {
                textField.isFocused = false
                i++
                if (i >= textFields.size) i = 0
                textFields[i].isFocused = true
                break
            }
            i++
        }
    }
}
