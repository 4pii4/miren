/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.ui.client.GuiMainMenu
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.multiplayer.GuiConnecting
import net.minecraft.client.multiplayer.ServerData

object ServerUtils : MinecraftInstance() {
    var serverData: ServerData? = null

    fun connectToLastServer() {
        if (serverData == null) return
        mc.displayGuiScreen(GuiConnecting(GuiMultiplayer(GuiMainMenu()), mc, serverData))
    }

    val remoteIp: String
        get() {
            if (mc.theWorld == null) return "Undefined"
            var serverIp = "Singleplayer"
            if (mc.theWorld.isRemote) {
                val serverData = mc.currentServerData
                if (serverData != null) serverIp = serverData.serverIP
            }
            return serverIp
        }
    val isHypixelLobby: Boolean
        get() {
            if (mc.theWorld == null) return false
            val target = "CLICK TO PLAY"
            for (entity in mc.theWorld.loadedEntityList) {
                if (entity.name.startsWith("§e§l")) {
                    if (entity.name == "§e§l$target") {
                        return true
                    }
                }
            }
            return false
        }

    fun isHypixelDomain(s1: String): Boolean {
        var chars = 0
        val str = "www.hypixel.net"
        for (c in str.toCharArray()) {
            if (s1.contains(c.toString())) chars++
        }
        return chars == str.length
    }
}