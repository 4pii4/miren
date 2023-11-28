/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.combat.NoFriends
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot.Companion.isBot
import net.ccbluex.liquidbounce.features.module.modules.world.Teams
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.monster.EntityGhast
import net.minecraft.entity.monster.EntityGolem
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.monster.EntitySlime
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntityBat
import net.minecraft.entity.passive.EntitySquid
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.GLU
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.vecmath.Vector3d
import javax.vecmath.Vector4d
import kotlin.math.max
import kotlin.math.min

object EntityUtils : MinecraftInstance() {
    @JvmField
    var targetInvisible = false
    @JvmField
    var targetPlayer = true
    @JvmField
    var targetMobs = true
    @JvmField
    var targetAnimals = false
    @JvmField
    var targetDead = false
    @JvmStatic
    fun isSelected(entity: Entity, canAttackCheck: Boolean): Boolean {
        if (entity is EntityLivingBase && (targetDead || entity.isEntityAlive()) && entity !== mc.thePlayer) {
            if (targetInvisible || !entity.isInvisible()) {
                if (targetPlayer && entity is EntityPlayer) {
                    val entityPlayer = entity
                    if (canAttackCheck) {
                        if (isBot(entityPlayer)) return false
                        if (isFriend(entityPlayer) && !LiquidBounce.moduleManager.getModule(NoFriends::class.java)!!.state) return false
                        if (entityPlayer.isSpectator) return false
                        val teams = LiquidBounce.moduleManager.getModule(Teams::class.java)
                        return !teams!!.state || !teams.isInYourTeam(entityPlayer)
                    }
                    return true
                }
                return targetMobs && isMob(entity) || targetAnimals && isAnimal(entity)
            }
        }
        return false
    }

    fun isFriend(entity: Entity): Boolean {
        return entity is EntityPlayer && entity.getName() != null &&
                LiquidBounce.fileManager.friendsConfig.isFriend(stripColor(entity.getName()))
    }

    fun isAnimal(entity: Entity?): Boolean {
        return entity is EntityAnimal || entity is EntitySquid || entity is EntityGolem ||
                entity is EntityBat
    }

    fun isMob(entity: Entity?): Boolean {
        return entity is EntityMob || entity is EntityVillager || entity is EntitySlime ||
                entity is EntityGhast || entity is EntityDragon
    }

    fun getName(networkPlayerInfoIn: NetworkPlayerInfo): String {
        return if (networkPlayerInfoIn.displayName != null) networkPlayerInfoIn.displayName.formattedText else ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.playerTeam, networkPlayerInfoIn.gameProfile.name)
    }

    fun getPing(entityPlayer: EntityPlayer?): Int {
        if (entityPlayer == null) return 0
        val networkPlayerInfo = mc.netHandler.getPlayerInfo(entityPlayer.uniqueID)
        return networkPlayerInfo?.responseTime ?: 0
    }

    fun isRendered(entityToCheck: Entity?): Boolean {
        return mc.theWorld != null && mc.theWorld.getLoadedEntityList().contains(entityToCheck)
    }

    fun projectEntity2d(entity: Entity, partialTicks: Float): Vector4d? {
        val viewport: IntBuffer = GLAllocation.createDirectIntBuffer(16)
        val modelview: FloatBuffer = GLAllocation.createDirectFloatBuffer(16)
        val projection: FloatBuffer = GLAllocation.createDirectFloatBuffer(16)
        val vector: FloatBuffer = GLAllocation.createDirectFloatBuffer(4)

        fun project2D(scaleFactor: Int, x: Double, y: Double, z: Double): Vector3d? {
            GL11.glGetFloat(2982, modelview)
            GL11.glGetFloat(2983, projection)
            GL11.glGetInteger(2978, viewport)
            return if (GLU.gluProject(
                    x.toFloat(),
                    y.toFloat(),
                    z.toFloat(),
                    modelview,
                    projection,
                    viewport,
                    vector
                )
            ) Vector3d(
                (vector[0] / scaleFactor.toFloat()).toDouble(),
                ((Display.getHeight().toFloat() - vector[1]) / scaleFactor.toFloat()).toDouble(),
                vector[2].toDouble()
            ) else null
        }

        if (!RenderUtils.isInViewFrustrum(entity))
            return null

        val scaledResolution = ScaledResolution(mc)
        val scaleFactor = scaledResolution.scaleFactor
        val renderMng = mc.renderManager
        val x = RenderUtils.interpolate(entity.posX, entity.lastTickPosX, partialTicks.toDouble())
        val y = RenderUtils.interpolate(entity.posY, entity.lastTickPosY, partialTicks.toDouble())
        val z = RenderUtils.interpolate(entity.posZ, entity.lastTickPosZ, partialTicks.toDouble())
        val width = entity.width.toDouble() / 1.5
        val height = entity.height.toDouble() + if (entity.isSneaking) -0.3 else 0.2
        val aabb = AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width)
        val vectors: List<*> = listOf(
            Vector3d(aabb.minX, aabb.minY, aabb.minZ),
            Vector3d(aabb.minX, aabb.maxY, aabb.minZ),
            Vector3d(aabb.maxX, aabb.minY, aabb.minZ),
            Vector3d(aabb.maxX, aabb.maxY, aabb.minZ),
            Vector3d(aabb.minX, aabb.minY, aabb.maxZ),
            Vector3d(aabb.minX, aabb.maxY, aabb.maxZ),
            Vector3d(aabb.maxX, aabb.minY, aabb.maxZ),
            Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ)
        )

        mc.entityRenderer.setupCameraTransform(partialTicks, 0)

        var position: Vector4d? = null
        val var38 = vectors.iterator()
        while (var38.hasNext()) {
            var vector = var38.next() as Vector3d?
            vector = project2D(
                scaleFactor,
                vector!!.x - renderMng.viewerPosX,
                vector.y - renderMng.viewerPosY,
                vector.z - renderMng.viewerPosZ
            )
            if (vector != null && vector.z >= 0.0 && vector.z < 1.0) {
                if (position == null) {
                    position = Vector4d(vector.x, vector.y, vector.z, 0.0)
                }
                position.x = min(vector.x, position.x)
                position.y = min(vector.y, position.y)
                position.z = max(vector.x, position.z)
                position.w = max(vector.y, position.w)
            }
        }

        return position
    }
}
