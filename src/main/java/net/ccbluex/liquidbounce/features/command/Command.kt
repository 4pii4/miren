/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.command

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import java.util.*

abstract class Command(val command: String, val alias: Array<String>) : MinecraftInstance() {

    constructor(command: String): this(command, emptyArray())
    /**
     * Execute commands with provided [args]
     */
    abstract fun execute(args: Array<String>)

    /**
     * Returns a list of command completions based on the provided [args].
     * If a command does not implement [tabComplete] an [EmptyList] is returned by default.
     *
     * @param args an array of command arguments that the player has passed to the command so far
     * @return a list of matching completions for the command the player is trying to autocomplete
     * @author NurMarvin
     */
    open fun tabComplete(args: Array<String>): List<String> {
        return emptyList()
    }

    /**
     * Print [syntaxes] of command to chat
     */
    protected fun chatSyntax(syntaxes: Array<String>) {
        ClientUtils.displayChatMessage("${LiquidBounce.CLIENT_NAME_COLORED} §cSyntax:")

        for (syntax in syntaxes)
            ClientUtils.displayChatMessage(
                "§8> §f${LiquidBounce.commandManager.prefix}$command ${
                    syntax.lowercase(
                        Locale.getDefault()
                    )
                }"
            )
    }

    companion object {
        /**
         * Print [msg] to chat
         */
        @JvmStatic
        protected fun chat(msg: String) = ClientUtils.displayChatMessage("${LiquidBounce.CLIENT_NAME_COLORED} §f$msg")

        /**
         * Print [syntax] of command to chat
         */
        @JvmStatic
        protected fun chatSyntax(syntax: String) =
            ClientUtils.displayChatMessage("${LiquidBounce.CLIENT_NAME_COLORED} §cSyntax: §7${LiquidBounce.commandManager.prefix}$syntax")

        /**
         * Print a syntax error to chat
         */
        @JvmStatic
        protected fun chatSyntaxError() =
            ClientUtils.displayChatMessage("${LiquidBounce.CLIENT_NAME_COLORED} §cSyntax error")

        /**
         * Play edit sound
         */
        @JvmStatic
        protected fun playEdit() {
            //mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("random.anvil_use"), 1F))
        }

        fun highlightModule(moduleName: String) = "§b§l${moduleName}§r"
        fun highlightModule(module: Module) = highlightModule(module.name)
    }
}