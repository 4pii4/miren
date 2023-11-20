package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.BowAimbot
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.exploit.Disabler
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly
import net.ccbluex.liquidbounce.features.module.modules.world.Breaker
import net.ccbluex.liquidbounce.features.module.modules.world.ChestAura
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue

@ModuleInfo(name = "Rotations", description = "Allows you to see server-sided head and body rotations.", category = ModuleCategory.RENDER)
class Rotations : Module() {

    val headValue = BoolValue("Head", true)
    val bodyValue = BoolValue("Body", true)
    val fakeValue = BoolValue("Ghost", true)
    var R = FloatValue("R", 255f, 0f, 255f)
    var G = FloatValue("G", 255f, 0f, 255f)
    var B = FloatValue("B", 255f, 0f, 255f)
    var Alpha = FloatValue("Alpha", 100f, 0f, 255f)

    var playerYaw: Float? = null

    companion object {
        @JvmStatic
        var prevHeadPitch = 0f

        @JvmStatic
        var headPitch = 0f

        @JvmStatic
        fun lerp(tickDelta: Float, old: Float, new: Float): Float {
            return old + (new - old) * tickDelta
        }

        private fun getState(module: Class<out Module>) = LiquidBounce.moduleManager[module]!!.state

        @JvmStatic
        fun shouldRotate(): Boolean {
            val killAura = LiquidBounce.moduleManager.getModule(KillAura::class.java) as KillAura
            val scaffold = LiquidBounce.moduleManager.getModule(Scaffold::class.java) as Scaffold
            val disabler = LiquidBounce.moduleManager.getModule(Disabler::class.java) as Disabler
            val breaker = LiquidBounce.moduleManager.getModule(Breaker::class.java) as Breaker
            val bowAimbot = LiquidBounce.moduleManager.getModule(BowAimbot::class.java) as BowAimbot
            return (scaffold.state && scaffold.rotationsValue.get()) ||
                    (killAura.state && killAura.target != null) ||
                    (disabler.state && disabler.canRenderInto3D) ||
                    (bowAimbot.state && bowAimbot.target != null) ||
                    (breaker.state && breaker.rotationsValue.get()) ||
                    getState(ChestAura::class.java)
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        prevHeadPitch = headPitch
        headPitch = RotationUtils.serverRotation.pitch
        val thePlayer = mc.thePlayer

        if (thePlayer == null) {
            playerYaw = null
            return
        }

            playerYaw = RotationUtils.serverRotation.yaw

            if (headValue.get())
                thePlayer.rotationYawHead = RotationUtils.serverRotation.yaw
    }
}
