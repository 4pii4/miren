/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.TickEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.FastBow
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.projectile.EntityEgg
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.*
import java.util.*
import kotlin.math.*


class RotationUtils : MinecraftInstance(), Listenable {
    /**
     * Handle minecraft tick
     *
     * @param event Tick event
     */
    @EventTarget
    fun onTick(event: TickEvent?) {
        if (targetRotation != null) {
            keepLength--
            if (keepLength <= 0) reset()
        }
        if (random.nextGaussian() > 0.8) x = Math.random()
        if (random.nextGaussian() > 0.8) y = Math.random()
        if (random.nextGaussian() > 0.8) z = Math.random()
    }

    /**
     * Handle packet
     *
     * @param event Packet Event
     */
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer) {
            val packetPlayer = packet
            if (targetRotation != null && !keepCurrentRotation && (targetRotation!!.yaw != serverRotation.yaw || targetRotation!!.pitch != serverRotation.pitch)) {
                packetPlayer.yaw = targetRotation!!.yaw
                packetPlayer.pitch = targetRotation!!.pitch
                packetPlayer.rotating = true
            }
            if (packetPlayer.rotating) serverRotation = Rotation(packetPlayer.yaw, packetPlayer.pitch)
        }
    }

    /**
     * @return YESSSS!!!
     */
    override fun handleEvents(): Boolean {
        return true
    }

    companion object {
        private val random = Random()
        private var keepLength = 0
        @JvmField
        var targetRotation: Rotation? = null
        @JvmField
        var serverRotation: Rotation = Rotation(0f, 0f)
        var keepCurrentRotation = false
        private var x = random.nextDouble()
        private var y = random.nextDouble()
        private var z = random.nextDouble()

        /**
         * @author aquavit
         *
         * epic skid moment
         */
        fun OtherRotation(bb: AxisAlignedBB?, vec: Vec3, predict: Boolean, throughWalls: Boolean, distance: Float): Rotation? {
            val eyesPos = Vec3(
                mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY +
                        mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ
            )
            /*
        final Vec3 eyes = mc.thePlayer.getPositionEyes(1F);
        VecRotation vecRotation = null;
        for(double xSearch = 0.15D; xSearch < 0.85D; xSearch += 0.1D) {
            for (double ySearch = 0.15D; ySearch < 1D; ySearch += 0.1D) {
                for (double zSearch = 0.15D; zSearch < 0.85D; zSearch += 0.1D) {
                    final Vec3 vec3 = new Vec3(bb.minX + (bb.maxX - bb.minX) * xSearch,
                            bb.minY + (bb.maxY - bb.minY) * ySearch, bb.minZ + (bb.maxZ - bb.minZ) * zSearch);
                    final Rotation rotation = toRotation(vec3, predict);
                    final double vecDist = eyes.distanceTo(vec3);

                    if (vecDist > distance)
                        continue;

                    if(throughWalls || isVisible(vec3)) {
                        final VecRotation currentVec = new VecRotation(vec3, rotation);

                        if (vecRotation == null)
                            vecRotation = currentVec;
                    }
                }
            }
        }
        */if (predict) eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ)
            if (!throughWalls && !isVisible(vec)) {
                return null
            }
            val diffX = vec.xCoord - eyesPos.xCoord
            val diffY = vec.yCoord - eyesPos.yCoord
            val diffZ = vec.zCoord - eyesPos.zCoord
            return Rotation(
                MathHelper.wrapAngleTo180_float(
                    Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f
                ), MathHelper.wrapAngleTo180_float((-Math.toDegrees(atan2(diffY, sqrt(diffX * diffX + diffZ * diffZ)))).toFloat())
            )
        }

        fun toDownRotation(vec: Vec3, predict: Boolean): Rotation {
            val eyesPos = Vec3(mc.thePlayer.posX, 90.0, mc.thePlayer.posZ)

            if (predict) eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ)

            val diffX = vec.xCoord - eyesPos.xCoord
            val diffY = vec.yCoord - eyesPos.yCoord
            val diffZ = vec.zCoord - eyesPos.zCoord

            return Rotation(
                MathHelper.wrapAngleTo180_float(
                    Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f
                ), MathHelper.wrapAngleTo180_float(
                    (-Math.toDegrees(atan2(diffY, sqrt(diffX * diffX + diffZ * diffZ)))).toFloat()
                )
            )
        }

        fun downRot(
            bb: AxisAlignedBB, outborder: Boolean, random: Boolean,
            predict: Boolean, throughWalls: Boolean, distance: Float, randomMultiply: Float, newRandom: Boolean
        ): VecRotation? {
            if (outborder) {
                val vec3 = Vec3(
                    bb.minX + (bb.maxX - bb.minX) * (x * 0.3 + 1.0),
                    bb.minY + (bb.maxY - bb.minY) * (y * 0.3 + 1.0),
                    bb.minZ + (bb.maxZ - bb.minZ) * (z * 0.3 + 1.0)
                )
                return VecRotation(vec3, toRotation(vec3, predict))
            }

            val randomVec = Vec3(
                bb.minX + (bb.maxX - bb.minX) * x * randomMultiply * (if (newRandom) Math.random() else 1.0),
                bb.minY + (bb.maxY - bb.minY) * y * randomMultiply * (if (newRandom) Math.random() else 1.0),
                bb.minZ + (bb.maxZ - bb.minZ) * z * randomMultiply * (if (newRandom) Math.random() else 1.0)
            )
            val randomRotation = toRotation(randomVec, predict)

            val eyes = mc.thePlayer.getPositionEyes(1f)

            var vecRotation: VecRotation? = null

            var xSearch = 0.15
            while (xSearch < 0.85) {
                var ySearch = 0.15
                while (ySearch < 1.0) {
                    var zSearch = 0.15
                    while (zSearch < 0.85) {
                        val vec3 = Vec3(
                            bb.minX + (bb.maxX - bb.minX) * xSearch,
                            bb.minY + (bb.maxY - bb.minY) * ySearch, bb.minZ + (bb.maxZ - bb.minZ) * zSearch
                        )
                        val rotation: Rotation = toDownRotation(vec3, predict)
                        val vecDist = eyes.distanceTo(vec3)

                        if (vecDist > distance) {
                            zSearch += 0.1
                            continue
                        }

                        if (throughWalls || isVisible(vec3)) {
                            val currentVec = VecRotation(vec3, rotation)

                            if (vecRotation == null || (if (random) getRotationDifference(
                                    currentVec.rotation,
                                    randomRotation
                                ) < getRotationDifference(
                                    vecRotation.rotation,
                                    randomRotation
                                ) else getRotationDifference(currentVec.rotation) < getRotationDifference(vecRotation.rotation))
                            ) vecRotation = currentVec
                        }
                        zSearch += 0.1
                    }
                    ySearch += 0.1
                }
                xSearch += 0.1
            }

            return vecRotation
        }

        /**
         * Face block
         *
         * @param blockPos target block
         */
        fun faceBlock(blockPos: BlockPos?): VecRotation? {
            if (blockPos == null) return null
            var vecRotation: VecRotation? = null
            var xSearch = 0.1
            while (xSearch < 0.9) {
                var ySearch = 0.1
                while (ySearch < 0.9) {
                    var zSearch = 0.1
                    while (zSearch < 0.9) {
                        val eyesPos = Vec3(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ)
                        val posVec = Vec3(blockPos).addVector(xSearch, ySearch, zSearch)
                        val dist = eyesPos.distanceTo(posVec)
                        val diffX = posVec.xCoord - eyesPos.xCoord
                        val diffY = posVec.yCoord - eyesPos.yCoord
                        val diffZ = posVec.zCoord - eyesPos.zCoord
                        val diffXZ = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ).toDouble()
                        val rotation = Rotation(
                            MathHelper.wrapAngleTo180_float(Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f),
                            MathHelper.wrapAngleTo180_float(-Math.toDegrees(atan2(diffY, diffXZ)).toFloat())
                        )
                        val rotationVector = getVectorForRotation(rotation)
                        val vector = eyesPos.addVector(
                            rotationVector.xCoord * dist, rotationVector.yCoord * dist,
                            rotationVector.zCoord * dist
                        )
                        val obj = mc.theWorld.rayTraceBlocks(
                            eyesPos, vector, false,
                            false, true
                        )
                        if (obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                            val currentVec = VecRotation(posVec, rotation)
                            if (vecRotation == null || getRotationDifference(currentVec.rotation) < getRotationDifference(vecRotation.rotation)) vecRotation = currentVec
                        }
                        zSearch += 0.1
                    }
                    ySearch += 0.1
                }
                xSearch += 0.1
            }
            return vecRotation
        }

        /**
         * Face target with bow
         *
         * @param target your enemy
         * @param silent client side rotations
         * @param predict predict new enemy position
         * @param predictSize predict size of predict
         */
        fun faceBow(target: Entity, silent: Boolean, predict: Boolean, predictSize: Float) {
            val player = mc.thePlayer
            val posX: Double = target.posX + (if (predict) (target.posX - target.prevPosX) * predictSize else 0.0) - (player.posX + if (predict) player.posX - player.prevPosX else 0.0)
            val posY: Double = target.entityBoundingBox.minY + (if (predict) (target.entityBoundingBox.minY - target.prevPosY) * predictSize else 0.0) + target.eyeHeight - 0.15 - (player.entityBoundingBox.minY + if (predict) player.posY - player.prevPosY else 0.0) - player.getEyeHeight()
            val posZ: Double = target.posZ + (if (predict) (target.posZ - target.prevPosZ) * predictSize else 0.0) - (player.posZ + if (predict) player.posZ - player.prevPosZ else 0.0)
            val posSqrt = sqrt(posX * posX + posZ * posZ)
            var velocity = if (LiquidBounce.moduleManager.getModule(FastBow::class.java)!!.state) 1f else player.itemInUseDuration / 20f
            velocity = (velocity * velocity + velocity * 2) / 3
            if (velocity > 1) velocity = 1f
            val rotation = Rotation(
                (atan2(posZ, posX) * 180 / Math.PI).toFloat() - 90, -Math.toDegrees(atan((velocity * velocity - sqrt(velocity * velocity * velocity * velocity - 0.006f * (0.006f * (posSqrt * posSqrt) + 2 * posY * (velocity * velocity)))) / (0.006f * posSqrt))).toFloat()
            )
            if (silent) setTargetRotation(rotation) else limitAngleChange(
                Rotation(player.rotationYaw, player.rotationPitch), rotation, (10 +
                        Random().nextInt(6)).toFloat()
            ).toPlayer(mc.thePlayer)
        }

        /**
         * Translate vec to rotation
         *
         * @param vec target vec
         * @param predict predict new location of your body
         * @return rotation
         */
        fun toRotation(vec: Vec3, predict: Boolean): Rotation {
            val eyesPos = Vec3(
                mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY +
                        mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ
            )
            if (predict) eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ)
            val diffX = vec.xCoord - eyesPos.xCoord
            val diffY = vec.yCoord - eyesPos.yCoord
            val diffZ = vec.zCoord - eyesPos.zCoord
            return Rotation(
                MathHelper.wrapAngleTo180_float(
                    Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f
                ), MathHelper.wrapAngleTo180_float((-Math.toDegrees(atan2(diffY, sqrt(diffX * diffX + diffZ * diffZ)))).toFloat())
            )
        }

        /**
         * Get the center of a box
         *
         * @param bb your box
         * @return center of box
         */
        fun getCenter(bb: AxisAlignedBB): Vec3 {
            return Vec3(bb.minX + (bb.maxX - bb.minX) * 0.5, bb.minY + (bb.maxY - bb.minY) * 0.5, bb.minZ + (bb.maxZ - bb.minZ) * 0.5)
        }

        fun roundRotation(yaw: Float, strength: Int): Float {
            return (Math.round(yaw / strength) * strength).toFloat()
        }

        /**
         * Search good center
         *
         * @param bb enemy box
         * @param outborder outborder option
         * @param random random option
         * @param predict predict option
         * @param throughWalls throughWalls option
         * @return center
         */
        @JvmOverloads
        fun searchCenter(
            bb: AxisAlignedBB, outborder: Boolean, random: Boolean,
            predict: Boolean, throughWalls: Boolean, distance: Float, randomMultiply: Float = 0f, newRandom: Boolean = false
        ): VecRotation {
            if (outborder) {
                val vec3 = Vec3(bb.minX + (bb.maxX - bb.minX) * (x * 0.3 + 1.0), bb.minY + (bb.maxY - bb.minY) * (y * 0.3 + 1.0), bb.minZ + (bb.maxZ - bb.minZ) * (z * 0.3 + 1.0))
                return VecRotation(vec3, toRotation(vec3, predict))
            }
            val randomVec = Vec3(bb.minX + (bb.maxX - bb.minX) * x * randomMultiply * if (newRandom) Math.random() else 1.0, bb.minY + (bb.maxY - bb.minY) * y * randomMultiply * if (newRandom) Math.random() else 1.0, bb.minZ + (bb.maxZ - bb.minZ) * z * randomMultiply * if (newRandom) Math.random() else 1.0)
            val randomRotation = toRotation(randomVec, predict)
            val eyes = mc.thePlayer.getPositionEyes(1f)
            var vecRotation: VecRotation? = null
            var xSearch = 0.15
            while (xSearch < 0.85) {
                var ySearch = 0.15
                while (ySearch < 1.0) {
                    var zSearch = 0.15
                    while (zSearch < 0.85) {
                        val vec3 = Vec3(
                            bb.minX + (bb.maxX - bb.minX) * xSearch,
                            bb.minY + (bb.maxY - bb.minY) * ySearch, bb.minZ + (bb.maxZ - bb.minZ) * zSearch
                        )
                        val rotation = toRotation(vec3, predict)
                        val vecDist = eyes.distanceTo(vec3)
                        if (vecDist > distance) {
                            zSearch += 0.1
                            continue
                        }
                        if (throughWalls || isVisible(vec3)) {
                            val currentVec = VecRotation(vec3, rotation)
                            if (vecRotation == null || (if (random) getRotationDifference(currentVec.rotation, randomRotation) < getRotationDifference(vecRotation.rotation, randomRotation) else getRotationDifference(currentVec.rotation) < getRotationDifference(vecRotation.rotation))) vecRotation = currentVec
                        }
                        zSearch += 0.1
                    }
                    ySearch += 0.1
                }
                xSearch += 0.1
            }
            return vecRotation!!
        }

        /**
         * Calculate difference between the client rotation and your entity
         *
         * @param entity your entity
         * @return difference between rotation
         */
        fun getRotationDifference(entity: Entity): Double {
            val rotation = toRotation(getCenter(entity.entityBoundingBox), true)
            return getRotationDifference(rotation, Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch))
        }

        /**
         * Calculate difference between the client rotation and your entity's back
         *
         * @param entity your entity
         * @return difference between rotation
         */
        fun getRotationBackDifference(entity: Entity): Double {
            val rotation = toRotation(getCenter(entity.entityBoundingBox), true)
            return getRotationDifference(rotation, Rotation(mc.thePlayer.rotationYaw - 180, mc.thePlayer.rotationPitch))
        }

        /**
         * Calculate difference between the server rotation and your rotation
         *
         * @param rotation your rotation
         * @return difference between rotation
         */
        @JvmStatic
        fun getRotationDifference(rotation: Rotation): Double {
            return if (serverRotation == null) 0.0 else getRotationDifference(rotation, serverRotation)
        }

        /**
         * Calculate difference between two rotations
         *
         * @param a rotation
         * @param b rotation
         * @return difference between rotation
         */
        fun getRotationDifference(a: Rotation, b: Rotation?): Double {
            return hypot(getAngleDifference(a.yaw, b!!.yaw).toDouble(), (a.pitch - b.pitch).toDouble())
        }

        /**
         * Limit your rotation using a turn speed
         *
         * @param currentRotation your current rotation
         * @param targetRotation your goal rotation
         * @param turnSpeed your turn speed
         * @return limited rotation
         */
        fun limitAngleChange(currentRotation: Rotation, targetRotation: Rotation, turnSpeed: Float): Rotation {
            val yawDifference = getAngleDifference(targetRotation.yaw, currentRotation.yaw)
            val pitchDifference = getAngleDifference(targetRotation.pitch, currentRotation.pitch)
            return Rotation(
                currentRotation.yaw + if (yawDifference > turnSpeed) turnSpeed else max(yawDifference, -turnSpeed),
                currentRotation.pitch + if (pitchDifference > turnSpeed) turnSpeed else max(pitchDifference, -turnSpeed)
            )
        }

        fun limitAngleChange(currentRotation: Rotation, targetRotation: Rotation, horizontalSpeed: Float, verticalSpeed: Float): Rotation {
            val yawDifference = getAngleDifference(targetRotation.yaw, currentRotation.yaw)
            val pitchDifference = getAngleDifference(targetRotation.pitch, currentRotation.pitch)
            return Rotation(
                currentRotation.yaw + if (yawDifference > horizontalSpeed) horizontalSpeed else max(yawDifference, -horizontalSpeed),
                currentRotation.pitch + if (pitchDifference > verticalSpeed) verticalSpeed else max(pitchDifference, -verticalSpeed)
            )
        }

        /**
         * Calculate difference between two angle points
         *
         * @param a angle point
         * @param b angle point
         * @return difference between angle points
         */
        private fun getAngleDifference(a: Float, b: Float): Float {
            return ((a - b) % 360f + 540f) % 360f - 180f
        }

        /**
         * Calculate rotation to vector
         *
         * @param rotation your rotation
         * @return target vector
         */
        /*public static Vec3 getVectorForRotation(final Rotation rotation) {
        float yawCos = MathHelper.cos(-rotation.getYaw() * 0.017453292F - (float) Math.PI);
        float yawSin = MathHelper.sin(-rotation.getYaw() * 0.017453292F - (float) Math.PI);
        float pitchCos = -MathHelper.cos(-rotation.getPitch() * 0.017453292F);
        float pitchSin = MathHelper.sin(-rotation.getPitch() * 0.017453292F);
        return new Vec3(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
    }*/
        fun getVectorForRotation(rotation: Rotation): Vec3 {
            val f = MathHelper.cos((-rotation.yaw * 0.017453292f - Math.PI).toFloat())
            val f1 = MathHelper.sin((-rotation.yaw * 0.017453292f - Math.PI).toFloat())
            val f2 = -MathHelper.cos(-rotation.pitch * 0.017453292f)
            val f3 = MathHelper.sin(-rotation.pitch * 0.017453292f)
            return Vec3((f1 * f2).toDouble(), f3.toDouble(), (f * f2).toDouble())
        }

        /**
         * Allows you to check if your crosshair is over your target entity
         *
         * @param targetEntity your target entity
         * @param blockReachDistance your reach
         * @return if crosshair is over target
         */
        fun isFaced(targetEntity: Entity, blockReachDistance: Double): Boolean {
            val filter = RaycastUtils.IEntityFilter { it === targetEntity }
            return RaycastUtils.raycastEntity(blockReachDistance, filter) != null
        }

        /**
         * Allows you to check if your enemy is behind a wall
         */
        fun isVisible(vec3: Vec3?): Boolean {
            val eyesPos = Vec3(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ)
            return mc.theWorld.rayTraceBlocks(eyesPos, vec3) == null
        }

        /**
         * Set your target rotation
         *
         * @param rotation your target rotation
         */
        fun setTargetRotation(rotation: Rotation) {
            setTargetRotation(rotation, 0)
        }

        /**
         * Set your target rotation
         *
         * @param rotation your target rotation
         */
        fun setTargetRotation(rotation: Rotation, keepLength: Int) {
            try {
                if (java.lang.Double.isNaN(rotation.yaw.toDouble()) || java.lang.Double.isNaN(rotation.pitch.toDouble()) || rotation.pitch > 90 || rotation.pitch < -90) return
            } catch (ignored: Exception) {
                return
            }
            rotation.fixedSensitivity(mc.gameSettings.mouseSensitivity)
            targetRotation = rotation
            Companion.keepLength = keepLength
        }

        /**
         * Reset your target rotation
         */
        fun reset() {
            keepLength = 0
            targetRotation = null
        }

        fun getRotationsEntity(entity: EntityLivingBase): Rotation {
            return getRotations(entity.posX, entity.posY + entity.eyeHeight - 0.4, entity.posZ)
        }

        fun getRotations(posX: Double, posY: Double, posZ: Double): Rotation {
            val player = mc.thePlayer
            val x = posX - player.posX
            val y = posY - (player.posY + player.getEyeHeight().toDouble())
            val z = posZ - player.posZ
            val dist = MathHelper.sqrt_double(x * x + z * z).toDouble()
            val yaw = (atan2(z, x) * 180.0 / 3.141592653589793).toFloat() - 90.0f
            val pitch = (-(atan2(y, dist) * 180.0 / 3.141592653589793)).toFloat()
            return Rotation(yaw, pitch)
        }

        fun getRotations(ent: Entity): Rotation {
            val x = ent.posX
            val z = ent.posZ
            val y = ent.posY + (ent.eyeHeight / 2.0f).toDouble()
            return getRotationFromPosition(x, z, y)
        }

        fun getRotationFromPosition(x: Double, z: Double, y: Double): Rotation {
            val xDiff = x - mc.thePlayer.posX
            val zDiff = z - mc.thePlayer.posZ
            val yDiff = y - mc.thePlayer.posY - 1.2
            val dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff).toDouble()
            val yaw = (atan2(zDiff, xDiff) * 180.0 / Math.PI).toFloat() - 90.0f
            val pitch = (-atan2(yDiff, dist) * 180.0 / Math.PI).toFloat()
            return Rotation(yaw, pitch)
        }

        fun calculate(from: Vec3?, to: Vec3): Rotation {
            val diff = to.subtract(from)
            val distance = hypot(diff.xCoord, diff.zCoord)
            val yaw = (MathHelper.atan2(diff.zCoord, diff.xCoord) * (180f / Math.PI)).toFloat() - 90.0f
            val pitch = (-(MathHelper.atan2(diff.yCoord, distance) * (180f / Math.PI))).toFloat()
            return Rotation(yaw, pitch)
        }

        fun calculate(position: Vec3, enumFacing: EnumFacing): Rotation {
            var x = position.xCoord + 0.5
            var y = position.yCoord + 0.5
            var z = position.zCoord + 0.5
            x += enumFacing.directionVec.x.toDouble() * 0.5
            y += enumFacing.directionVec.y.toDouble() * 0.5
            z += enumFacing.directionVec.z.toDouble() * 0.5
            return calculate(Vec3(x, y, z))
        }

        fun calculate(to: Vec3): Rotation {
            return calculate(mc.thePlayer.positionVector.add(Vec3(0.0, mc.thePlayer.getEyeHeight().toDouble(), 0.0)), Vec3(to.xCoord, to.yCoord, to.zCoord))
        }

        fun getAngles(entity: Entity?): Rotation? {
            if (entity == null) return null
            val thePlayer = mc.thePlayer
            val diffX = entity.posX - thePlayer.posX
            val diffY = entity.posY + entity.eyeHeight * 0.9 - (thePlayer.posY + thePlayer.getEyeHeight())
            val diffZ = entity.posZ - thePlayer.posZ
            val dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ).toDouble() // @on
            val yaw = (atan2(diffZ, diffX) * 180.0 / Math.PI).toFloat() - 90.0f
            val pitch = -(atan2(diffY, dist) * 180.0 / Math.PI).toFloat()
            return Rotation(
                thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - thePlayer.rotationYaw),
                thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - thePlayer.rotationPitch)
            )
        }

        fun getDirectionToBlock(x: Double, y: Double, z: Double, enumfacing: EnumFacing): Rotation {
            val var4 = EntityEgg(mc.theWorld)
            var4.posX = x + 0.5
            var4.posY = y + 0.5
            var4.posZ = z + 0.5
            var4.posX += enumfacing.directionVec.x.toDouble() * 0.5
            var4.posY += enumfacing.directionVec.y.toDouble() * 0.5
            var4.posZ += enumfacing.directionVec.z.toDouble() * 0.5
            return getRotations(var4.posX, var4.posY, var4.posZ)
        }
    }
}