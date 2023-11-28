package net.ccbluex.liquidbounce.features.module.modules.player

/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */

import net.ccbluex.liquidbounce.event.EntityDamageEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue

@ModuleInfo(name = "OnDamage", description = "change shit when u get damaged", category = ModuleCategory.PLAYER)
class OnDamage : Module() {
    private val timer = BoolValue("Timer", true)
    private val timerMS = IntegerValue("TimerMS", 5, 0, 1000) { timer.get() }
    private val timerSpeed = FloatValue("TimerSpeed", 1f, 0f, 4f) { timer.get() }

    private val strafe = BoolValue("Strafe", true)
    private val strafeMode = ListValue("StrafeMode", arrayOf("Custom", "Current", "Add", "Multiply"), "Current") { strafe.get() }
    private val strafeAdd = FloatValue("StrafeAdd", 0.1f, 0f, 1f) { strafe.get() && strafeMode.isMode("add") }
    private val strafeMultiply = FloatValue("StrafeMultiply", 1.1f, 0f, 2f) { strafe.get() && strafeMode.isMode("multiply") }
    private val strafeCutom = FloatValue("StrafeCustom", 0.3f, 0f, 2f) { strafe.get() && strafeMode.isMode("custom") }
    private val strafeMS = IntegerValue("StrafeMS", 100, 0, 1000) { strafe.get() }

    private var timerActivated = false
    private var strafeActivated = false
    private val setTimerTimer = MSTimer()
    private val strafeTimer = MSTimer()

    override fun onDisable() {
        mc.timer.timerSpeed = 1F
    }

    @EventTarget
    fun onEntityDamage(event: EntityDamageEvent) {
        val entity = event.damagedEntity
        if (entity != mc.thePlayer) return

        if (timer.get()) {
            setTimerTimer.reset()
            mc.timer.timerSpeed = timerSpeed.get()
            timerActivated = true
        }

        if (strafe.get()) {
            strafeTimer.reset()
            strafeActivated = true
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (setTimerTimer.hasTimePassed(timerMS.get()) && timerActivated) {
            timerActivated = false
            setTimerTimer.reset()
            mc.timer.timerSpeed = 1f
        } else if (timerActivated) {
            mc.timer.timerSpeed = timerSpeed.get()
        }

        if (strafeTimer.hasTimePassed(strafeMS.get()) && strafeActivated) {
            strafeActivated = false
            strafeTimer.reset()
            return
        } else if (strafeActivated) {
            when (strafeMode.get().lowercase()) {
                "current" -> MovementUtils.strafe()
                "add" -> MovementUtils.strafe(MovementUtils.speed + strafeAdd.get())
                "multiply" -> MovementUtils.strafe(MovementUtils.speed * strafeMultiply.get())
                "custom" -> MovementUtils.strafe(strafeCutom.get())
            }
        }
    }

    override val tag: String
        get() = "${if (timer.get()) "Timer," else ""}${if (strafe.get()) "Strafe" else ""}"
}