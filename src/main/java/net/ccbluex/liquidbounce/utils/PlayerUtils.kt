package net.ccbluex.liquidbounce.utils

import com.google.common.collect.Multimap
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.minecraft.block.*
import net.minecraft.block.material.Material
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.potion.Potion
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import javax.vecmath.Vector3f
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PlayerUtils {
    val entity: EntityLivingBase?
        get() = null

    companion object {
        private val mc = Minecraft.getMinecraft()
        fun getRotations(ent: Entity): FloatArray {
            val x = ent.posX
            val z = ent.posZ
            val y = ent.posY + ent.eyeHeight / 4.0f
            return getRotationFromPosition(x, z, y)
        }

        fun damagePlayer(damage: Int) {
            var damage = damage
            if (damage < 1) damage = 1
            if (damage > MathHelper.floor_double(mc.thePlayer.maxHealth.toDouble())) damage = MathHelper.floor_double(mc.thePlayer.maxHealth.toDouble())
            val offset = 0.0625
            if (mc.thePlayer != null && mc.netHandler != null && mc.thePlayer.onGround) {
                var i = 0
                while (i <= (3 + damage) / offset) {
                    mc.netHandler.addToSendQueue(
                        C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + offset, mc.thePlayer.posZ, false
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY, mc.thePlayer.posZ, i.toDouble() == (3 + damage) / offset
                        )
                    )
                    i++
                }
            }
        }

        fun isOnGround(height: Double): Boolean {
            return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, -height, 0.0)).isEmpty()
        }

        fun MovementInput(): Boolean {
            return mc.gameSettings.keyBindForward.pressed || mc.gameSettings.keyBindLeft.pressed || mc.gameSettings.keyBindRight.pressed || mc.gameSettings.keyBindBack.pressed
        }

        private fun getRotationFromPosition(x: Double, z: Double, y: Double): FloatArray {
            val xDiff = x - mc.thePlayer.posX
            val zDiff = z - mc.thePlayer.posZ
            val yDiff = y - mc.thePlayer.posY - 0.6
            val dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff).toDouble()
            val yaw = (atan2(zDiff, xDiff) * 180.0 / Math.PI).toFloat() - 90.0f
            val pitch = -(atan2(yDiff, dist) * 180.0 / Math.PI).toFloat()
            return floatArrayOf(yaw, pitch)
        }

        val isMoving: Boolean
            get() = if (!mc.thePlayer.isCollidedHorizontally && !mc.thePlayer.isSneaking) {
                mc.thePlayer.movementInput.moveForward != 0.0f || mc.thePlayer.movementInput.moveStrafe != 0.0f
            } else false
        val baseMoveSpeed: Double
            get() {
                var baseSpeed = 0.2873
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    val amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier
                    baseSpeed *= 1.0 + 0.2 * (amplifier + 1).toDouble()
                }
                return baseSpeed
            }
        val maxFallDist: Float
            get() {
                val potioneffect = mc.thePlayer.getActivePotionEffect(Potion.jump)
                val f2 = if (potioneffect != null) potioneffect.amplifier + 1 else 0
                return (mc.thePlayer.maxFallHeight + f2).toFloat()
                //  int f = potioneffect != null ? potioneffect.getAmplifier() + 1 : 0;
                //return mc.thePlayer.getMaxFallHeight() + f;
            }
        val direction: Float
            get() {
                var yaw = mc.thePlayer.rotationYawHead
                val forward = mc.thePlayer.moveForward
                val strafe = mc.thePlayer.moveStrafing
                yaw += (if (forward < 0.0f) 180 else 0).toFloat()
                if (strafe < 0.0f) {
                    yaw += (if (forward < 0.0f) -45 else if (forward == 0.0f) 90 else 45).toFloat()
                }
                if (strafe > 0.0f) {
                    yaw -= (if (forward < 0.0f) -45 else if (forward == 0.0f) 90 else 45).toFloat()
                }
                return yaw * 0.017453292f
            }
        val isInWater: Boolean
            get() = mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)).block.material === Material.water
        val isMoving2: Boolean
            get() = mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f

        fun getBlock(pos: BlockPos?): Block {
            return mc.theWorld.getBlockState(pos).block
        }

        fun getBlockAtPosC(inPlayer: EntityPlayer, x: Double, y: Double, z: Double): Block {
            return getBlock(BlockPos(inPlayer.posX - x, inPlayer.posY - y, inPlayer.posZ - z))
        }

        fun vanillaTeleportPositions(tpX: Double, tpY: Double, tpZ: Double, speed: Double): ArrayList<Vector3f> {
            var d: Double
            val positions: ArrayList<Vector3f> = arrayListOf()
            val posX = tpX - mc.thePlayer.posX
            val posY = tpY - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight().toDouble() + 1.1)
            val posZ = tpZ - mc.thePlayer.posZ
            val yaw = (atan2(posZ, posX) * 180.0 / 3.141592653589793 - 90.0).toFloat()
            val pitch = (-atan2(posY, sqrt(posX * posX + posZ * posZ)) * 180.0 / 3.141592653589793).toFloat()
            var tmpX = mc.thePlayer.posX
            var tmpY = mc.thePlayer.posY
            var tmpZ = mc.thePlayer.posZ
            var steps = 1.0
            d = speed
            while (d < getDistance(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, tpX, tpY, tpZ)) {
                steps += 1.0
                d += speed
            }
            d = speed
            while (d < getDistance(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, tpX, tpY, tpZ)) {
                tmpX = mc.thePlayer.posX - sin(getDirection(yaw).toDouble()) * d
                tmpZ = mc.thePlayer.posZ + cos(getDirection(yaw).toDouble()) * d
                positions.add(Vector3f(tmpX.toFloat(), ((mc.thePlayer.posY - tpY) / steps).let { tmpY -= it; tmpY }.toFloat(), tmpZ.toFloat()))
                d += speed
            }
            positions.add(Vector3f(tpX.toFloat(), tpY.toFloat(), tpZ.toFloat()))
            return positions
        }

        fun getDirection(yaw: Float): Float {
            var yaw = yaw
            if (mc.thePlayer.moveForward < 0.0f) {
                yaw += 180.0f
            }
            var forward = 1.0f
            if (mc.thePlayer.moveForward < 0.0f) {
                forward = -0.5f
            } else if (mc.thePlayer.moveForward > 0.0f) {
                forward = 0.5f
            }
            if (mc.thePlayer.moveStrafing > 0.0f) {
                yaw -= 90.0f * forward
            }
            if (mc.thePlayer.moveStrafing < 0.0f) {
                yaw += 90.0f * forward
            }
            return 0.017453292f.let { yaw *= it; yaw }
        }

        fun getDistance(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Double {
            val d0 = x1 - x2
            val d2 = y1 - y2
            val d3 = z1 - z2
            return MathHelper.sqrt_double(d0 * d0 + d2 * d2 + d3 * d3).toDouble()
        }

        fun blockHit(en: Entity?, value: Boolean) {
            val stack = mc.thePlayer.currentEquippedItem
            if (mc.thePlayer.currentEquippedItem != null && en != null && value && stack.item is ItemSword && mc.thePlayer.swingProgress.toDouble() > 0.2) {
                mc.thePlayer.currentEquippedItem.useItemRightClick(mc.theWorld, mc.thePlayer)
            }
        }

        fun getItemAtkDamage(itemStack: ItemStack): Float {
            val multimap: Multimap<*, *> = itemStack.attributeModifiers
            var iterator: Iterator<*> = multimap.entries().iterator()
                if (!multimap.isEmpty && multimap.entries().iterator().also { iterator = it }.hasNext()) {
                val damage: Double
                val (_, value) = iterator.next() as Map.Entry<*, *>
                val attributeModifier = value as AttributeModifier
                damage = if (attributeModifier.operation != 1 && attributeModifier.operation != 2) attributeModifier.amount else attributeModifier.amount * 100.0
                val d = damage
                return if (attributeModifier.amount > 1.0) {
                    1.0f + damage.toFloat()
                } else 1.0f
            }
            return 1.0f
        }

        fun bestWeapon(target: Entity?): Int {
            mc.thePlayer.inventory.currentItem = 0
            val firstSlot = 0
            var bestWeapon = -1
            var j = 1
            var i = 0
            while (i < 9) {
                mc.thePlayer.inventory.currentItem = i
                val itemStack = mc.thePlayer.heldItem
                if (itemStack == null) {
                    i = (i + 1).toByte().toInt()
                    continue
                }
                val itemAtkDamage = getItemAtkDamage(itemStack).toInt()
                //   if ((itemAtkDamage = (int)((float)itemAtkDamage + EnchantmentHelper.getEnchantedItem((ItemStack)itemStack, (EnumCreatureAttribute)EnumCreatureAttribute.UNDEFINED))) <= j) continue;
                j = itemAtkDamage
                bestWeapon = i
                i = (i + 1).toByte().toInt()
            }
            return if (bestWeapon != -1) {
                bestWeapon
            } else firstSlot
        }

        fun shiftClick(i: Item) {
            for (i1 in 9..36) {
                val itemstack = mc.thePlayer.inventoryContainer.getSlot(i1).stack
                if (itemstack == null || itemstack.item !== i) continue
                mc.playerController.windowClick(0, i1, 0, 1, mc.thePlayer)
                break
            }
        }

        fun hotbarIsFull(): Boolean {
            for (i in 0..36) {
                val itemstack = mc.thePlayer.inventory.getStackInSlot(i)
                if (itemstack != null) continue
                return false
            }
            return true
        }

        fun getLook(p_174806_1_: Float, p_174806_2_: Float): Vec3 {
            val var3 = MathHelper.cos(-p_174806_2_ * 0.017453292f - 3.1415927f)
            val var4 = MathHelper.sin(-p_174806_2_ * 0.017453292f - 3.1415927f)
            val var5 = -MathHelper.cos(-p_174806_1_ * 0.017453292f)
            val var6 = MathHelper.sin(-p_174806_1_ * 0.017453292f)
            return Vec3((var4 * var5).toDouble(), var6.toDouble(), (var3 * var5).toDouble())
        }

        fun tellPlayer(string: String?) {
            mc.thePlayer.addChatMessage(ChatComponentText(string))
        }

        fun getIncremental(`val`: Double, inc: Double): Double {
            val one = 1.0 / inc
            return Math.round(`val` * one) / one
        }

        val distanceToFall: Double
            get() {
                var distance = 0.0
                var i2 = mc.thePlayer.posY
                while (i2 > 0.0) {
                    if (i2 < 0.0) break
                    val block = BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, i2, mc.thePlayer.posZ))
                    if (block!!.material !== Material.air && block!!.isCollidable && (block.isFullBlock || block is BlockSlab || block is BlockBarrier || block is BlockStairs || block is BlockGlass || block is BlockStainedGlass)) {
                        if (block is BlockSlab) {
                            i2 -= 0.5
                        }
                        distance = i2
                        break
                    }
                    i2 -= 0.1
                }
                return mc.thePlayer.posY - distance
            }

        fun isAirUnder(ent: Entity): Boolean {
            return mc.theWorld.getBlockState(BlockPos(ent.posX, ent.posY - 1, ent.posZ)).block === Blocks.air
        }
    }
}
