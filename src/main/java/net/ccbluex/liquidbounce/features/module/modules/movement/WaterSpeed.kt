/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.block.BlockLiquid

@ModuleInfo(name = "WaterSpeed", spacedName = "Water Speed", description = "Allows you to swim faster.", category = ModuleCategory.MOVEMENT)
class WaterSpeed : Module() {
    private val speedValue = FloatValue("Speed", 1.2f, 1.1f, 1.5f)
    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (mc.thePlayer.isInWater && getBlock(mc.thePlayer.position) is BlockLiquid) {
            val speed = speedValue.get()
            mc.thePlayer.motionX *= speed.toDouble()
            mc.thePlayer.motionZ *= speed.toDouble()
        }
    }
}