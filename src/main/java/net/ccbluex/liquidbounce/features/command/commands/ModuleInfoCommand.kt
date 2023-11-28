package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.ModuleInfo

class ModuleInfoCommand: Command("moduleinfo", arrayOf("mi", "modinfo")) {
    override fun execute(args: Array<String>) {
        val maybe = LiquidBounce.hud.elements.find { it is ModuleInfo }
        if (maybe == null) {
            chat("No ModuleInfo element found")
            return
        }

        val miElement = maybe as ModuleInfo

        when (args.size) {
            1 -> chat("Modules: ${miElement.modulesValue.get()}")

            2 -> {
                val validated = mutableListOf<String>()
                for (s in args[1].split(",")) {
                    val maybeModule = LiquidBounce.moduleManager.getModule(s)
                    if (maybeModule == null) {
                        chat("${highlightModule(s)} is not a valid module name")
                        return
                    }
                    validated.add(s)
                }
                val validatedStr = validated.joinToString(",")
                miElement.modulesValue.set(validatedStr)
                chat("Modules: ${miElement.modulesValue.get()}")
            }
        }
    }
}
