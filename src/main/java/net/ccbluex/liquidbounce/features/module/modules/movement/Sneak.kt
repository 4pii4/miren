/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.client.C0BPacketEntityAction
import java.util.*

@ModuleInfo(name = "Sneak", description = "Automatically sneaks all the time.", category = ModuleCategory.MOVEMENT)
class Sneak : Module() {
    val modeValue = ListValue("Mode", arrayOf("Legit", "Vanilla", "Switch", "MineSecure", "AAC3.6.4"), "MineSecure")
    val stopMoveValue = BoolValue("StopMove", false)
    private var sneaked = false
    override fun onEnable() {
        if (mc.thePlayer == null) return
        if ("vanilla".equals(modeValue.get(), ignoreCase = true)) {
            mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING))
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (stopMoveValue.get() && MovementUtils.isMoving()) {
            if (sneaked) {
                onDisable()
                sneaked = false
            }
            return
        }
        sneaked = true
        for (duh in arrayOf(modeValue))
        when (modeValue.get().lowercase(Locale.getDefault())) {
            "legit" -> mc.gameSettings.keyBindSneak.pressed = true
            "switch" -> when (event.eventState) {
                EventState.PRE -> {
                    if (!MovementUtils.isMoving()) return
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING))
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING))
                }

                EventState.POST -> {
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING))
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING))
                }
            }

            "minesecure" -> {
                if (event.eventState === EventState.PRE) break
                mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING))
            }

            "aac3.6.4" -> {
                mc.gameSettings.keyBindSneak.pressed = true
                if (mc.thePlayer.onGround) {
                    MovementUtils.strafe(MovementUtils.getSpeed() * 1.251f)
                } else {
                    MovementUtils.strafe(MovementUtils.getSpeed() * 1.03f)
                }
            }
        }
    }

    override fun onDisable() {
        if (mc.thePlayer == null) return
        when (modeValue.get().lowercase(Locale.getDefault())) {
            "legit", "vanilla", "switch", "aac3.6.4" -> if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) mc.gameSettings.keyBindSneak.pressed = false
            "minesecure" -> mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING))
        }
        super.onDisable()
    }
}
