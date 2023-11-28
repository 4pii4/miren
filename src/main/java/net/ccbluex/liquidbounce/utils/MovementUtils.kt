/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.MoveEvent
import net.minecraft.block.BlockIce
import net.minecraft.block.BlockPackedIce
import net.minecraft.client.Minecraft
import net.minecraft.potion.Potion
import net.minecraft.util.BlockPos
import org.lwjgl.util.vector.Vector2f
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object MovementUtils : MinecraftInstance() {
    private var lastX = -999999.0
    private var lastY = -999999.0
    private var lastZ = -999999.0

    var speed
        get() = mc.thePlayer?.run { sqrt(motionX * motionX + motionZ * motionZ).toFloat() } ?: .0f
        set(value) { strafe(value) }

    fun getSpeed(motionX: Double, motionZ: Double): Double {
        return sqrt(motionX * motionX + motionZ * motionZ)
    }

    val isOnGround: Boolean
        get() = mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically

    val isOnIce: Boolean
        get() {
            val thePlayer = mc.thePlayer
            val blockUnder = mc.theWorld.getBlockState(BlockPos(thePlayer.posX, thePlayer.posY - 1.0, thePlayer.posZ)).block
            return blockUnder is BlockIce || blockUnder is BlockPackedIce
        }

    val isBlockUnder: Boolean
        get() {
            if (mc.thePlayer == null) return false
            if (mc.thePlayer.posY < 0.0) {
                return false
            }
            var off = 0
            while (off < mc.thePlayer.posY.toInt() + 2) {
                val bb = mc.thePlayer.entityBoundingBox.offset(0.0, -off.toDouble(), 0.0)
                if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                    return true
                }
                off += 2
            }
            return false
        }

    @JvmOverloads
    fun accelerate(speed: Float = this.speed) {
        if (!isMoving) return
        val yaw = direction
        mc.thePlayer.motionX += -sin(yaw) * speed
        mc.thePlayer.motionZ += cos(yaw) * speed
    }

    @JvmStatic
    val isMoving: Boolean
        get() = mc.thePlayer != null && (mc.thePlayer.movementInput.moveForward != 0f || mc.thePlayer.movementInput.moveStrafe != 0f)

    fun hasMotion(): Boolean {
        return mc.thePlayer.motionX != 0.0 && mc.thePlayer.motionZ != 0.0 && mc.thePlayer.motionY != 0.0
    }

    @JvmStatic
    @JvmOverloads
    fun strafe(speed: Float = this.speed) {
        if (!isMoving) return
        val yaw = direction
        mc.thePlayer.motionX = -sin(yaw) * speed
        mc.thePlayer.motionZ = cos(yaw) * speed
    }

    @JvmStatic
    fun strafe(speed: Double) = strafe(speed.toFloat())

    fun strafeCustom(speed: Float, cYaw: Float, strafe: Float, forward: Float) {
        if (!isMoving) return
        val yaw = getDirectionRotation(cYaw, strafe, forward)
        mc.thePlayer.motionX = -sin(yaw) * speed
        mc.thePlayer.motionZ = cos(yaw) * speed
    }

    fun forward(length: Double) {
        val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
        mc.thePlayer.setPosition(mc.thePlayer.posX + -sin(yaw) * length, mc.thePlayer.posY, mc.thePlayer.posZ + cos(yaw) * length)
    }

    val direction: Double
        get() {
            var rotationYaw = mc.thePlayer.rotationYaw
            if (mc.thePlayer.moveForward < 0f) rotationYaw += 180f
            var forward = 1f
            if (mc.thePlayer.moveForward < 0f) forward = -0.5f else if (mc.thePlayer.moveForward > 0f) forward = 0.5f
            if (mc.thePlayer.moveStrafing > 0f) rotationYaw -= 90f * forward
            if (mc.thePlayer.moveStrafing < 0f) rotationYaw += 90f * forward
            return Math.toRadians(rotationYaw.toDouble())
        }
    val rawDirection: Float
        get() = getRawDirectionRotation(mc.thePlayer.rotationYaw, mc.thePlayer.moveStrafing, mc.thePlayer.moveForward)

    fun getRawDirection(yaw: Float): Float {
        return getRawDirectionRotation(yaw, mc.thePlayer.moveStrafing, mc.thePlayer.moveForward)
    }

    fun getXZDist(speed: Float, cYaw: Float): DoubleArray {
        val arr = DoubleArray(2)
        val yaw = getDirectionRotation(cYaw, mc.thePlayer.moveStrafing, mc.thePlayer.moveForward)
        arr[0] = -sin(yaw) * speed
        arr[1] = cos(yaw) * speed
        return arr
    }

    fun getPredictionYaw(x: Double, z: Double): Float {
        if (mc.thePlayer == null) {
            lastX = -999999.0
            lastZ = -999999.0
            return 0f
        }
        if (lastX == -999999.0) lastX = mc.thePlayer.prevPosX
        if (lastZ == -999999.0) lastZ = mc.thePlayer.prevPosZ
        val returnValue = (atan2(z - lastZ, x - lastX) * 180f / Math.PI).toFloat()
        lastX = x
        lastZ = z
        return returnValue
    }

    fun getDirectionRotation(yaw: Float, pStrafe: Float, pForward: Float): Double {
        var rotationYaw = yaw
        if (pForward < 0f) rotationYaw += 180f
        var forward = 1f
        if (pForward < 0f) forward = -0.5f else if (pForward > 0f) forward = 0.5f
        if (pStrafe > 0f) rotationYaw -= 90f * forward
        if (pStrafe < 0f) rotationYaw += 90f * forward
        return Math.toRadians(rotationYaw.toDouble())
    }

    fun getRawDirectionRotation(yaw: Float, pStrafe: Float, pForward: Float): Float {
        var rotationYaw = yaw
        if (pForward < 0f) rotationYaw += 180f
        var forward = 1f
        if (pForward < 0f) forward = -0.5f else if (pForward > 0f) forward = 0.5f
        if (pStrafe > 0f) rotationYaw -= 90f * forward
        if (pStrafe < 0f) rotationYaw += 90f * forward
        return rotationYaw
    }

    fun getScaffoldRotation(yaw: Float, strafe: Float): Float {
        var rotationYaw = yaw
        rotationYaw += 180f
        val forward = -0.5f
        if (strafe < 0f) rotationYaw -= 90f * forward
        if (strafe > 0f) rotationYaw += 90f * forward
        return rotationYaw
    }

    val jumpEffect: Int
        get() = if (mc.thePlayer.isPotionActive(Potion.jump)) mc.thePlayer.getActivePotionEffect(Potion.jump).amplifier + 1 else 0

    fun isOnGround(height: Double): Boolean {
        return Minecraft.getMinecraft().theWorld.getCollidingBoundingBoxes(
            Minecraft.getMinecraft().thePlayer,
            Minecraft.getMinecraft().thePlayer.entityBoundingBox.offset(0.0, -height, 0.0)
        ).isNotEmpty()
    }

    val speedEffect: Int
        get() = if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1 else 0
    val baseMoveSpeed: Double
        get() {
            var baseSpeed = 0.2873
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                baseSpeed *= 1.0 + 0.2 * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1).toDouble()
            }
            return baseSpeed
        }

    fun getBaseMoveSpeed(customSpeed: Double): Double {
        var baseSpeed = if (isOnIce) 0.258977700006 else customSpeed
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            val amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1)
        }
        return baseSpeed
    }

    fun getJumpBoostModifier(baseJumpHeight: Double): Double {
        return getJumpBoostModifier(baseJumpHeight, true)
    }

    fun getJumpBoostModifier(baseJumpHeight: Double, potionJump: Boolean): Double {
        var baseJumpHeight = baseJumpHeight
        if (mc.thePlayer.isPotionActive(Potion.jump) && potionJump) {
            val amplifier = mc.thePlayer.getActivePotionEffect(Potion.jump).amplifier
            baseJumpHeight += ((amplifier + 1).toFloat() * 0.1f).toDouble()
        }
        return baseJumpHeight
    }

    fun setMotion(event: MoveEvent, speed: Double, motion: Double, smoothStrafe: Boolean) {
        var forward = mc.thePlayer.movementInput.moveForward.toDouble()
        var strafe = mc.thePlayer.movementInput.moveStrafe.toDouble()
        var yaw = mc.thePlayer.rotationYaw.toDouble()
        val direction = if (smoothStrafe) 45 else 90
        if (forward == 0.0 && strafe == 0.0) {
            event.x = 0.0
            event.z = 0.0
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (if (forward > 0.0) -direction else direction).toDouble()
                } else if (strafe < 0.0) {
                    yaw += (if (forward > 0.0) direction else -direction).toDouble()
                }
                strafe = 0.0
                if (forward > 0.0) {
                    forward = 1.0
                } else if (forward < 0.0) {
                    forward = -1.0
                }
            }
            val cos = cos(Math.toRadians(yaw + 90.0f))
            val sin = sin(Math.toRadians(yaw + 90.0f))
            event.x = (forward * speed * cos + strafe * speed * sin) * motion
            event.z = (forward * speed * sin - strafe * speed * cos) * motion
        }
    }

    fun setMotion(speed: Double, smoothStrafe: Boolean) {
        var forward = mc.thePlayer.movementInput.moveForward.toDouble()
        var strafe = mc.thePlayer.movementInput.moveStrafe.toDouble()
        var yaw = mc.thePlayer.rotationYaw
        val direction = if (smoothStrafe) 45 else 90
        if (forward == 0.0 && strafe == 0.0) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (if (forward > 0.0) -direction else direction).toFloat()
                } else if (strafe < 0.0) {
                    yaw += (if (forward > 0.0) direction else -direction).toFloat()
                }
                strafe = 0.0
                if (forward > 0.0) {
                    forward = 1.0
                } else if (forward < 0.0) {
                    forward = -1.0
                }
            }
            mc.thePlayer.motionX = forward * speed * -sin(Math.toRadians(yaw.toDouble())) + strafe * speed * cos(Math.toRadians(yaw.toDouble()))
            mc.thePlayer.motionZ = forward * speed * cos(Math.toRadians(yaw.toDouble())) - strafe * speed * -sin(Math.toRadians(yaw.toDouble()))
        }
    }

    fun setSpeed(moveEvent: MoveEvent, moveSpeed: Double) {
        setSpeed(moveEvent, moveSpeed, mc.thePlayer.rotationYaw, mc.thePlayer.movementInput.moveStrafe.toDouble(), mc.thePlayer.movementInput.moveForward.toDouble())
    }

    fun setSpeed(moveEvent: MoveEvent, moveSpeed: Double, pseudoYaw: Float, pseudoStrafe: Double, pseudoForward: Double) {
        var forward = pseudoForward
        var strafe = pseudoStrafe
        var yaw = pseudoYaw
        if (forward == 0.0 && strafe == 0.0) {
            moveEvent.z = 0.0
            moveEvent.x = 0.0
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (if (forward > 0.0) -45 else 45).toFloat()
                } else if (strafe < 0.0) {
                    yaw += (if (forward > 0.0) 45 else -45).toFloat()
                }
                strafe = 0.0
                if (forward > 0.0) {
                    forward = 1.0
                } else if (forward < 0.0) {
                    forward = -1.0
                }
            }
            if (strafe > 0.0) {
                strafe = 1.0
            } else if (strafe < 0.0) {
                strafe = -1.0
            }
            val cos = cos(Math.toRadians((yaw + 90.0f).toDouble()))
            val sin = sin(Math.toRadians((yaw + 90.0f).toDouble()))
            moveEvent.x = forward * moveSpeed * cos + strafe * moveSpeed * sin
            moveEvent.z = forward * moveSpeed * sin - strafe * moveSpeed * cos
        }
    }

    fun resetMotion(y: Boolean) {
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionZ = 0.0
        if (y) mc.thePlayer.motionY = 0.0
    }

    fun updateBlocksPerSecond() {
        var bps = 0.0
        if (mc.thePlayer == null || mc.thePlayer.ticksExisted < 1) {
            bps = 0.0
        }
        val distance = mc.thePlayer.getDistance(lastX, lastY, lastZ)
        lastX = mc.thePlayer.posX
        lastY = mc.thePlayer.posY
        lastZ = mc.thePlayer.posZ
        bps = distance * (20 * mc.timer.timerSpeed)
    }

    fun getMoveYaw(yaw: Float): Float {
        var yaw = yaw
        val from = Vector2f(mc.thePlayer.lastTickPosX.toFloat(), mc.thePlayer.lastTickPosZ.toFloat())
        val to = Vector2f(mc.thePlayer.posX.toFloat(), mc.thePlayer.posZ.toFloat())
        val diff = Vector2f(to.x - from.x, to.y - from.y)
        val x = diff.x.toDouble()
        val z = diff.y.toDouble()
        if (x != 0.0 && z != 0.0) {
            yaw = Math.toDegrees((atan2(-x, z) + Math.PI * 2f) % Math.PI * 2f).toFloat()
        }
        return yaw
    }
}