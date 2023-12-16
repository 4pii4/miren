package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.utils.timer.TimerUtils
import net.minecraft.block.Block
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockBush
import net.minecraft.block.BlockLiquid
import net.minecraft.client.Minecraft
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3

class AttackParticle(@JvmField val position: Vec3) {
    private val removeTimer = TimerUtils()
    private val delta: Vec3

    init {
        delta = Vec3((Math.random() * 2.5 - 1.25) * 0.04 + 0, (Math.random() * 0.5 - 0.2) * 0.04 + 0, (Math.random() * 2.5 - 1.25) * 0.04 + 0)
        removeTimer.reset()
    }

    fun update() {
        var block3: Block?
        var block2: Block?
        val block1 = getBlock(position.xCoord, position.yCoord, position.zCoord + delta.zCoord)
        if (!(block1 is BlockAir || block1 is BlockBush || block1 is BlockLiquid)) {
            delta.zCoord *= -0.8
        }
        if (!(getBlock(position.xCoord, position.yCoord + delta.yCoord, position.zCoord).also { block2 = it } is BlockAir || block2 is BlockBush || block2 is BlockLiquid)) {
            delta.xCoord *= 0.99
            delta.zCoord *= 0.99
            delta.yCoord *= -0.5
        }
        if (!(getBlock(position.xCoord + delta.xCoord, position.yCoord, position.zCoord).also { block3 = it } is BlockAir || block3 is BlockBush || block3 is BlockLiquid)) {
            delta.xCoord *= -0.8
        }
        updateWithoutPhysics()
    }

    fun updateWithoutPhysics() {
        position.xCoord += delta.xCoord
        position.yCoord += delta.yCoord
        position.zCoord += delta.zCoord
        delta.xCoord *= 0.998
        delta.yCoord -= 3.1E-5
        delta.zCoord *= 0.998
    }

    companion object {
        fun getBlock(offsetX: Double, offsetY: Double, offsetZ: Double): Block {
            return Minecraft.getMinecraft().theWorld.getBlockState(BlockPos(offsetX, offsetY, offsetZ)).block
        }
    }
}