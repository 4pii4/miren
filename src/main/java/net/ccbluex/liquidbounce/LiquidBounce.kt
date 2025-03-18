/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce

import net.ccbluex.liquidbounce.discord.ClientRichPresence
import net.ccbluex.liquidbounce.event.ClientShutdownEvent
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.special.AntiForge
import net.ccbluex.liquidbounce.features.special.BungeeCordSpoof
import net.ccbluex.liquidbounce.features.special.CombatManager
import net.ccbluex.liquidbounce.features.special.MacroManager
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.script.ScriptManager
import net.ccbluex.liquidbounce.script.remapper.Remapper.loadSrg
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.client.hud.HUD.Companion.createDefault
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.TextColorUtils.darkGray
import net.ccbluex.liquidbounce.utils.TextColorUtils.red
import net.ccbluex.liquidbounce.utils.misc.sound.TipSoundManager
import net.ccbluex.liquidbounce.utils.render.ShaderUtils
import net.minecraft.util.ResourceLocation

object LiquidBounce {

    // Client information
    const val CLIENT_NAME = "Miren"
    val CLIENT_NAME_COLORED = "${darkGray("[")}${red(CLIENT_NAME)}${darkGray("]")}"
    const val CLIENT_VERSION = "b28112023"
    const val CLIENT_CREATOR = "CCBlueX, inf and pie"
    const val CLIENT_CLOUD = "https://mirenclient.github.io/cloud"
    const val CLIENT_REPO = "mirenclient/Miren"

    var isStarting = false

    var darkMode = false

    // Managers
    lateinit var moduleManager: ModuleManager
    lateinit var commandManager: CommandManager
    lateinit var eventManager: EventManager
    lateinit var fileManager: FileManager
    lateinit var scriptManager: ScriptManager
    lateinit var combatManager: CombatManager

    lateinit var tipSoundManager: TipSoundManager

    // HUD & ClickGUI
    lateinit var hud: HUD

    // Menu Background
    var background: ResourceLocation? = null

    // Discord RPC
    lateinit var clientRichPresence: ClientRichPresence

    var playTimeStart: Long = 0

    /**
     * Execute if client will be started
     */
    fun startClient() {
        isStarting = true

        ClientUtils.logger.info("Starting $CLIENT_NAME version $CLIENT_VERSION")
        val startTime = System.currentTimeMillis()
        playTimeStart = System.currentTimeMillis()

        // Create file manager
        fileManager = FileManager()

        DictUtils.init()
        ChangelogUtils.update()

        // Crate event manager
        eventManager = EventManager()

        combatManager = CombatManager()

        // Register listeners
        eventManager.registerListener(RotationUtils())
        eventManager.registerListener(AntiForge())
        eventManager.registerListener(BungeeCordSpoof())
        eventManager.registerListener(InventoryUtils())
        eventManager.registerListener(InventoryHelper)
        eventManager.registerListener(PacketUtils)
        eventManager.registerListener(SessionUtils())
        eventManager.registerListener(MacroManager)
        eventManager.registerListener(combatManager)

        // Init Discord RPC
        clientRichPresence = ClientRichPresence()

        // Create command manager
        commandManager = CommandManager()

        // Load client fonts
        Fonts.loadFonts()

        // Init SoundManager
        tipSoundManager = TipSoundManager()

        // Setup module manager and register modules
        moduleManager = ModuleManager()
        moduleManager.registerModules()

//         Remapper
        try {
            loadSrg()

            // ScriptManager
            scriptManager = ScriptManager()
            scriptManager.loadScripts()
            scriptManager.enableScripts()
        } catch (throwable: Throwable) {
            ClientUtils.logger.error("Failed to load scripts.", throwable)
        }

        // Register commands
        commandManager.registerCommands()

        // Load configs
        fileManager.loadConfigs(fileManager.modulesConfig, fileManager.valuesConfig, fileManager.accountsConfig,
            fileManager.friendsConfig, fileManager.xrayConfig)

        fileManager.loadConfig(fileManager.clickGuiConfig)

        // Set HUD
        hud = createDefault()
        fileManager.loadConfig(fileManager.hudConfig)

        // Setup Discord RPC
//        if (clientRichPresence.showRichPresenceValue) {
//            thread {
//                try {
//                    clientRichPresence.setup()
//                } catch (throwable: Throwable) {
//                    ClientUtils.getLogger().error("Failed to setup Discord RPC.", throwable)
//                }
//            }
//        }

        moduleManager.onClientLoaded()
        ClientUtils.logger.info("Finished loading $CLIENT_NAME version $CLIENT_VERSION in ${System.currentTimeMillis() - startTime}ms.")
        isStarting = false
    }

    /**
     * Execute if client will be stopped
     */
    fun stopClient() {
        // Call client shutdown
        eventManager.callEvent(ClientShutdownEvent())

        // Save all available configs
        fileManager.saveAllConfigs()

        // Shutdown discord rpc
        clientRichPresence.shutdown()
    }
}
