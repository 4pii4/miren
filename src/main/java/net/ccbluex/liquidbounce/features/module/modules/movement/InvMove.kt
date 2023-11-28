/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiIngameMenu
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C16PacketClientStatus
import java.util.*

@ModuleInfo(name = "InvMove", spacedName = "Inv Move", description = "Allows you to walk while an inventory is opened.", category = ModuleCategory.MOVEMENT)
class InvMove : Module() {

    val whenMove = ListValue("WhenMove", arrayOf("Inventory", "Chest", "All"), "Vanilla")
    val modeValue = ListValue("Mode", arrayOf("Vanilla", "Silent", "Blink"), "Vanilla")
    val noMoveClicksValue = BoolValue("NoMoveClicks", false)

    private val playerPackets = mutableListOf<C03PacketPlayer>()

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.PRE && playerPackets.size > 0 && (mc.currentScreen == null || mc.currentScreen is GuiChat || mc.currentScreen is GuiIngameMenu)) {
            playerPackets.forEach { mc.netHandler.addToSendQueue(it) }
            playerPackets.clear()
        }
    }

    @EventTarget
    fun onClick(event: ClickWindowEvent) {
        if (noMoveClicksValue.get() && MovementUtils.isMoving)
            event.cancelEvent()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        when (modeValue.get().lowercase(Locale.getDefault())) {
            "silent" -> if (packet is C16PacketClientStatus && packet.status == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT) event.cancelEvent()
            "blink" -> if (mc.currentScreen != null && mc.currentScreen !is GuiChat && mc.currentScreen !is GuiIngameMenu && packet is C03PacketPlayer) {
                event.cancelEvent()
                playerPackets.add(packet)
            }
        }
    }
}
