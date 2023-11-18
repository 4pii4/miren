/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.TickEvent
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.network.NetworkManager
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.server.S32PacketConfirmTransaction

object PacketUtils : MinecraftInstance(), Listenable {
    @EventTarget
    fun onPacket(event: PacketEvent) {
        handlePacket(event.packet)
    }

    @EventTarget
    fun onTick(event: TickEvent?) {
        if (packetTimer.hasTimePassed(1000L)) {
            avgInBound = inBound
            avgOutBound = outBound
            outBound = 0
            inBound = outBound
            packetTimer.reset()
        }
        if (mc.thePlayer == null || mc.theWorld == null) {
            //reset all checks
            wdVL = 0
            transCount = 0
            wdTimer.reset()
        } else if (wdTimer.hasTimePassed(100L)) { // watchdog active when the transaction poll rate reaches about 100ms/packet.
            wdVL += if (transCount > 0) 1 else -1
            transCount = 0
            if (wdVL > 10) wdVL = 10
            if (wdVL < 0) wdVL = 0
            wdTimer.reset()
        }
    }

    // TODO: Remove annotations once all modules are converted to kotlin.
    @JvmStatic
    @JvmOverloads
    fun sendPacket(packet: Packet<*>, triggerEvent: Boolean = true) {
        if (triggerEvent) {
            mc.netHandler?.addToSendQueue(packet)
            return
        }

        val netManager = mc.netHandler?.networkManager ?: return
        if (netManager.isChannelOpen) {
            netManager.flushOutboundQueue()
            netManager.dispatchPacket(packet, null)
        } else {
            netManager.readWriteLock.writeLock().lock()
            try {
                netManager.outboundPacketsQueue += NetworkManager.InboundHandlerTuplePacketListener(packet, null)
            } finally {
                netManager.readWriteLock.writeLock().unlock()
            }
        }
    }

    @JvmStatic
    @JvmOverloads
    fun sendPackets(vararg packets: Packet<*>, triggerEvents: Boolean = true) =
        packets.forEach { sendPacket(it, triggerEvents) }

    fun handlePackets(vararg packets: Packet<*>) =
        packets.forEach { handlePacket(it) }

    fun handlePacket(packet: Packet<*>?) =
        runCatching { (packet as Packet<INetHandlerPlayClient>).processPacket(mc.netHandler) }


    /**
     * @return wow
     */
    override fun handleEvents(): Boolean {
        return true
    }

    var inBound = 0
    var outBound = 0
    var avgInBound = 0
    var avgOutBound = 0
    var packets = ArrayList<Packet<*>>()
    private val packetTimer = MSTimer()
    private val wdTimer = MSTimer()
    private var transCount = 0
    private var wdVL = 0
    private fun isInventoryAction(action: Short): Boolean {
        return action in 1..99
    }

    val isWatchdogActive: Boolean
        get() = wdVL >= 8

    private fun handlePacket(packet: Packet<*>) {
        if (packet.javaClass.getSimpleName().startsWith("C")) outBound++ else if (packet.javaClass.getSimpleName().startsWith("S")) inBound++
        if (packet is S32PacketConfirmTransaction) {
            if (!isInventoryAction(packet.actionNumber)) transCount++
        }
    }

    /*
     * This code is from UnlegitMC/FDPClient. Please credit them when using this code in your repository.
     */
    @JvmStatic
    fun sendPacketNoEvent(packet: Packet<INetHandlerPlayServer>) {
        packets.add(packet)
        mc.netHandler.addToSendQueue(packet)
    }

    @JvmStatic
    fun handleSendPacket(packet: Packet<*>): Boolean {
        if (packets.contains(packet)) {
            packets.remove(packet)
            handlePacket(packet) // make sure not to skip silent packets.
            return true
        }
        return false
    }
}