/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.utils.ClientUtils
import org.lwjgl.input.Keyboard

class BindsCommand : Command("binds", emptyArray()) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size > 1) {
            if (args[1].equals("clear", true)) {
                for (module in LiquidBounce.moduleManager.modules)
                    module.keyBind = Keyboard.KEY_NONE

                chat("Removed all binds.")
                return
            }
        }

        chat("Binds:")
        val binds = LiquidBounce.moduleManager.modules
            .asSequence()
            .filter { it.keyBind != Keyboard.KEY_NONE }
            .map { it to Keyboard.getKeyName(it.keyBind) }
            .groupBy { it.second }
            .toList()
            .sortedBy { it.first }
            .toMap()

        for (bind in binds) {
            chat("${bind.key}: ${bind.value.joinToString(", ") { highlightModule(it.first.name) }}")
        }

        chatSyntax("binds clear")
    }
}