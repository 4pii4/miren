/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.block.BlockUtils.collideBlock
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.block.Block
import net.minecraft.block.BlockLiquid
import net.minecraft.util.AxisAlignedBB

@ModuleInfo(name = "ReverseStep", spacedName = "Reverse Step", description = "Allows you to step down blocks faster.", category = ModuleCategory.MOVEMENT)
class ReverseStep : Module() {
    private val motionValue = FloatValue("Motion", 1f, 0.21f, 1f)
    private var jumped = false
    @EventTarget(ignoreCondition = true)
    fun onUpdate(event: UpdateEvent?) {
        if (mc.thePlayer.onGround) jumped = false
        if (mc.thePlayer.motionY > 0) jumped = true
        if (!state) return
        if (collideBlock(mc.thePlayer.entityBoundingBox) { block: Block? -> block is BlockLiquid } || collideBlock(AxisAlignedBB(mc.thePlayer.entityBoundingBox.maxX, mc.thePlayer.entityBoundingBox.maxY, mc.thePlayer.entityBoundingBox.maxZ, mc.thePlayer.entityBoundingBox.minX, mc.thePlayer.entityBoundingBox.minY - 0.01, mc.thePlayer.entityBoundingBox.minZ)) { block: Block? -> block is BlockLiquid }) return
        if (!mc.gameSettings.keyBindJump.isKeyDown && !mc.thePlayer.onGround && !mc.thePlayer.movementInput.jump && mc.thePlayer.motionY <= 0.0 && mc.thePlayer.fallDistance <= 1f && !jumped) mc.thePlayer.motionY = -motionValue.get().toDouble()
    }

    @EventTarget(ignoreCondition = true)
    fun onJump(event: JumpEvent?) {
        jumped = true
    }
}