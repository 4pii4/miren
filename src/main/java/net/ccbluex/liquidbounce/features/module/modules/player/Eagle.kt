package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.client.settings.GameSettings
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

@ModuleInfo(name = "Eagle", description = "Makes you eagle (aka. FastBridge).", category = ModuleCategory.PLAYER)
class Eagle : Module() {
    private val minOffset = FloatValue("MinOffset", 0.1f, 0f, 1f)
    private val maxOffset = FloatValue("MaxOffset", 0.15f, 0f, 1f)
    private val groundOnly = BoolValue("GroundOnly", true)

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val x = mc.thePlayer.posX
        val z = mc.thePlayer.posZ
        val min = minOffset.get()
        val max = maxOffset.get()
        val start = minOf(min, max)
        val end = maxOf(min, max)
        val o = RandomUtils.nextFloat(start, end)

        var sneak = false
        for (xo in floatArrayOf(-o, 0f, o)) {
            for (zo in floatArrayOf(-o, 0f, o)) {
                val isAir = mc.theWorld.getBlockState(BlockPos(x + xo, mc.thePlayer.posY - 1, z + zo)).block === Blocks.air
                sneak = sneak || isAir
            }
        }

        if (groundOnly.get() && mc.thePlayer.onGround) mc.gameSettings.keyBindSneak.pressed = sneak
        else mc.gameSettings.keyBindSneak.pressed = sneak
    }

    override fun onDisable() {
        if (mc.thePlayer == null) return

        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) mc.gameSettings.keyBindSneak.pressed = false
    }
}