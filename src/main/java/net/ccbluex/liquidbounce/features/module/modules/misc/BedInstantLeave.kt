package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.item.ItemBed
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement

@ModuleInfo(name = "BedInstantLeave", description = "Leave bedwars instantly on some servers", category = ModuleCategory.MISC)
class BedInstantLeave : Module() {
    private val command = TextValue("Command", "/bw leave")

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C08PacketPlayerBlockPlacement && packet.stack != null && packet.stack.item != null && packet.stack.item is ItemBed) {
            event.cancelEvent()
            PacketUtils.sendPacketNoEvent(C01PacketChatMessage(command.get()))
        }
    }
}