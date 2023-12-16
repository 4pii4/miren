/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUI
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiStaff
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.misc.sound.TipSoundManager

class ReloadCommand : Command("reload", arrayOf("configreload")) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        reload()
    }
    
    companion object {
        fun reload() {
            chat("Reloading...")
            chat("Reloading commands...")
            LiquidBounce.commandManager = CommandManager()
            LiquidBounce.commandManager.registerCommands()
            LiquidBounce.isStarting = true
            LiquidBounce.scriptManager.disableScripts()
            LiquidBounce.scriptManager.unloadScripts()
            for(module in LiquidBounce.moduleManager.modules)
                LiquidBounce.moduleManager.generateCommand(module)
            chat("Reloading scripts...")
            LiquidBounce.scriptManager.loadScripts()
            LiquidBounce.scriptManager.enableScripts()
            chat("Reloading fonts...")
            Fonts.loadFonts()
            chat("Reloading toggle audio files...")
            LiquidBounce.tipSoundManager = TipSoundManager()
            chat("Reloading modules...")
            LiquidBounce.fileManager.loadConfig(LiquidBounce.fileManager.modulesConfig)
            LiquidBounce.isStarting = false
            chat("Reloading values...")
            LiquidBounce.fileManager.loadConfig(LiquidBounce.fileManager.valuesConfig)
            chat("Reloading accounts...")
            LiquidBounce.fileManager.loadConfig(LiquidBounce.fileManager.accountsConfig)
            chat("Reloading friends...")
            LiquidBounce.fileManager.loadConfig(LiquidBounce.fileManager.friendsConfig)
            chat("Reloading xray...")
            LiquidBounce.fileManager.loadConfig(LiquidBounce.fileManager.xrayConfig)
            chat("Reloading HUD...")
            LiquidBounce.fileManager.loadConfig(LiquidBounce.fileManager.hudConfig)
            LiquidBounce.fileManager.loadConfig(LiquidBounce.fileManager.clickGuiConfig)
            chat("Reloading ClickGui...")
            ClickGUI.resetClickGUIs()
            LiquidBounce.isStarting = false
            chat("Reloading staff list")
            LiquidBounce.moduleManager.getModule(AntiStaff::class.java)!!.init()
            chat("Reloaded.")
        }
    }
}
