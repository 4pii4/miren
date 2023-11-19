/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PathUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockFence
import net.minecraft.block.BlockSnow
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import java.util.function.Consumer
import javax.vecmath.Vector3d

@ModuleInfo(name = "Teleport", description = "Allows you to teleport around.", category = ModuleCategory.MOVEMENT)
class Teleport : Module() {
    private val ignoreNoCollision = BoolValue("IgnoreNoCollision", true)
    private val modeValue = ListValue("Mode", arrayOf("Blink", "Flag", "Rewinside", "OldRewinside", "Spoof", "Minesucht", "AAC3.5.0"), "Blink")
    private val buttonValue = ListValue("Button", arrayOf("Left", "Right", "Middle"), "Middle")
    private val requireSneak = BoolValue("RequireSneak", true) { modeValue.get().equals("blink", ignoreCase = true) || modeValue.get().equals("flag", ignoreCase = true) }
    private val flyTimer = TickTimer()
    private var hadGround = false
    private var fixedY = 0.0
    private val packets: MutableList<Packet<*>> = ArrayList()
    private var disableLogger = false
    private var zitter = false
    private var doTeleport = false
    private var freeze = false
    private val freezeTimer = TickTimer()
    private var delay = 0
    private var endPos: BlockPos? = null
    private var objectPosition: MovingObjectPosition? = null
    override fun onEnable() {
        if (modeValue.get().equals("AAC3.5.0", ignoreCase = true)) {
            ClientUtils.displayChatMessage("§c>>> §a§lTeleport §fAAC 3.5.0 §c<<<")
            ClientUtils.displayChatMessage("§cHow to teleport: §aPress " + buttonValue.get() + " mouse button.")
            ClientUtils.displayChatMessage("§cHow to cancel teleport: §aDisable teleport module.")
        }
    }

    override fun onDisable() {
        fixedY = 0.0
        delay = 0
        mc.timer.timerSpeed = 1f
        endPos = null
        hadGround = false
        freeze = false
        disableLogger = false
        flyTimer.reset()
        packets.clear()
        super.onDisable()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        val buttonIndex = listOf(*buttonValue.values).indexOf(buttonValue.get())
        if (modeValue.get() == "AAC3.5.0") {
            freezeTimer.update()
            if (freeze && freezeTimer.hasTimePassed(40)) {
                freezeTimer.reset()
                freeze = false
                state = false
            }
            if (!flyTimer.hasTimePassed(60)) {
                flyTimer.update()
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                } else {
                    MovementUtils.forward(if (zitter) -0.21 else 0.21)
                    zitter = !zitter
                }
                hadGround = false
                return
            }
            if (mc.thePlayer.onGround) hadGround = true
            if (!hadGround) return
            if (mc.thePlayer.onGround) mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, mc.thePlayer.posY + 0.2, mc.thePlayer.posZ)
            val vanillaSpeed = 2f
            mc.thePlayer.capabilities.isFlying = false
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
            if (mc.gameSettings.keyBindJump.isKeyDown) mc.thePlayer.motionY += vanillaSpeed.toDouble()
            if (mc.gameSettings.keyBindSneak.isKeyDown) mc.thePlayer.motionY -= vanillaSpeed.toDouble()
            MovementUtils.strafe(vanillaSpeed)
            if (Mouse.isButtonDown(buttonIndex) && !doTeleport) {
                mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, mc.thePlayer.posY - 11, mc.thePlayer.posZ)
                disableLogger = true
                packets.forEach(Consumer { packet: Packet<*>? -> mc.netHandler.addToSendQueue(packet) })
                freezeTimer.reset()
                freeze = true
            }
            doTeleport = Mouse.isButtonDown(buttonIndex)
            return
        }
        if (mc.currentScreen == null && Mouse.isButtonDown(buttonIndex) && delay <= 0) {
            endPos = objectPosition!!.blockPos
            if (getBlock(endPos)!!.material === Material.air) {
                endPos = null
                return
            }
            ClientUtils.displayChatMessage("§7[§8§lTeleport§7] §3Position was set to §8" + endPos!!.x + "§3, §8" + ((if (getBlock(objectPosition!!.blockPos)!!.getCollisionBoundingBox(mc.theWorld, objectPosition!!.blockPos, getBlock(objectPosition!!.blockPos)!!.defaultState) == null) endPos!!.y + getBlock(endPos)!!.blockBoundsMaxY else getBlock(objectPosition!!.blockPos)!!.getCollisionBoundingBox(mc.theWorld, objectPosition!!.blockPos, getBlock(objectPosition!!.blockPos)!!.defaultState).maxY) + fixedY) + "§3, §8" + endPos!!.z)
            delay = 6
        }
        if (delay > 0) --delay
        if (endPos != null) {
            val endX = endPos!!.x.toDouble() + 0.5
            val endY = (if (getBlock(if (objectPosition == null || objectPosition!!.blockPos == null) endPos else objectPosition!!.blockPos)
                    !!.getCollisionBoundingBox(mc.theWorld, objectPosition!!.blockPos, getBlock(objectPosition!!.blockPos)!!.defaultState) == null
            ) endPos!!.y + getBlock(endPos)!!.blockBoundsMaxY else getBlock(objectPosition!!.blockPos)!!.getCollisionBoundingBox(mc.theWorld, objectPosition!!.blockPos, getBlock(objectPosition!!.blockPos)!!.defaultState).maxY) + fixedY
            val endZ = endPos!!.z.toDouble() + 0.5
            for (duh in arrayOf(modeValue))
            when (modeValue.get().lowercase(Locale.getDefault())) {
                "blink" -> if (!requireSneak.get() || mc.thePlayer.isSneaking) {
                    // Sneak
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING))

                    // Teleport
                    PathUtils.findBlinkPath(endX, endY, endZ).forEach(Consumer { vector3d: Vector3d ->
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(vector3d.x, vector3d.y, vector3d.z, true))
                        mc.thePlayer.setPosition(endX, endY, endZ)
                    })

                    // Sneak
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING))

                    // Notify
                    ClientUtils.displayChatMessage("§7[§8§lTeleport§7] §3You were teleported to §8$endX§3, §8$endY§3, §8$endZ")
                }

                "flag" -> if (!requireSneak.get() || mc.thePlayer.isSneaking) {
                    // Sneak
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING))

                    // Teleport
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(endX, endY, endZ, true))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 5.0, mc.thePlayer.posZ, true))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(endX, endY, endZ, true))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX + 0.5, mc.thePlayer.posY, mc.thePlayer.posZ + 0.5, true))
                    MovementUtils.forward(0.04)

                    // Sneak
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING))
                    // Notify
                    ClientUtils.displayChatMessage("§7[§8§lTeleport§7] §3You were teleported to §8$endX§3, §8$endY§3, §8$endZ")
                }

                "rewinside" -> {
                    mc.thePlayer.motionY = 0.1
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(endX, endY, endZ, true))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.6, mc.thePlayer.posZ, true))
                    if (mc.thePlayer.posX.toInt() == endX.toInt() && mc.thePlayer.posY.toInt() == endY.toInt() && mc.thePlayer.posZ.toInt() == endZ.toInt()) {
                        ClientUtils.displayChatMessage("§7[§8§lTeleport§7] §3You were teleported to §8$endX§3, §8$endY§3, §8$endZ")
                        endPos = null
                    } else ClientUtils.displayChatMessage("§7[§8§lTeleport§7] §3Teleport try...")
                }

                "oldrewinside" -> {
                    mc.thePlayer.motionY = 0.1
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(endX, endY, endZ, true))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(endX, endY, endZ, true))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                    if (mc.thePlayer.posX.toInt() == endX.toInt() && mc.thePlayer.posY.toInt() == endY.toInt() && mc.thePlayer.posZ.toInt() == endZ.toInt()) {
                        ClientUtils.displayChatMessage("§7[§8§lTeleport§7] §3You were teleported to §8$endX§3, §8$endY§3, §8$endZ")
                        endPos = null
                    } else ClientUtils.displayChatMessage("§7[§8§lTeleport§7] §3Teleport try...")
                    MovementUtils.forward(0.04)
                }

                "minesucht" -> {
                    if (!mc.thePlayer.isSneaking) break
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(endX, endY, endZ, true))
                    ClientUtils.displayChatMessage("§7[§8§lTeleport§7] §3You were teleported to §8$endX§3, §8$endY§3, §8$endZ")
                }
            }
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        if (modeValue.get() == "AAC3.5.0") return
        val lookVec = Vec3(mc.thePlayer.lookVec.xCoord * 300, mc.thePlayer.lookVec.yCoord * 300, mc.thePlayer.lookVec.zCoord * 300)
        val posVec = Vec3(mc.thePlayer.posX, mc.thePlayer.posY + 1.62, mc.thePlayer.posZ)
        objectPosition = mc.thePlayer.worldObj.rayTraceBlocks(posVec, posVec.add(lookVec), false, ignoreNoCollision.get(), false)
        if (objectPosition == null || objectPosition!!.blockPos == null) return
        val belowBlockPos = BlockPos(objectPosition!!.blockPos.x, objectPosition!!.blockPos.y - 1, objectPosition!!.blockPos.z)
        fixedY = if (getBlock(objectPosition!!.blockPos) is BlockFence) (if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(objectPosition!!.blockPos.x + 0.5 - mc.thePlayer.posX, objectPosition!!.blockPos.y + 1.5 - mc.thePlayer.posY, objectPosition!!.blockPos.z + 0.5 - mc.thePlayer.posZ)).isEmpty()) 0.5 else 0.0) else if (getBlock(belowBlockPos) is BlockFence) (if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(objectPosition!!.blockPos.x + 0.5 - mc.thePlayer.posX, objectPosition!!.blockPos.y + 0.5 - mc.thePlayer.posY, objectPosition!!.blockPos.z + 0.5 - mc.thePlayer.posZ)).isNotEmpty() || getBlock(
                objectPosition!!.blockPos
            )!!.getCollisionBoundingBox(mc.theWorld, objectPosition!!.blockPos, getBlock(objectPosition!!.blockPos)!!.defaultState) == null
        ) 0.0 else 0.5 - getBlock(objectPosition!!.blockPos)!!.blockBoundsMaxY) else if (getBlock(objectPosition!!.blockPos) is BlockSnow) getBlock(objectPosition!!.blockPos)!!.blockBoundsMaxY - 0.125 else 0.0
        val x = objectPosition!!.blockPos.x
        val y = (if (getBlock(objectPosition!!.blockPos)!!.getCollisionBoundingBox(mc.theWorld, objectPosition!!.blockPos, getBlock(objectPosition!!.blockPos)!!.defaultState) == null) objectPosition!!.blockPos.y + getBlock(objectPosition!!.blockPos)!!.blockBoundsMaxY else getBlock(objectPosition!!.blockPos)!!.getCollisionBoundingBox(mc.theWorld, objectPosition!!.blockPos, getBlock(objectPosition!!.blockPos)!!.defaultState).maxY) - 1.0 + fixedY
        val z = objectPosition!!.blockPos.z
        if (getBlock(objectPosition!!.blockPos) !is BlockAir) {
            val renderManager = mc.renderManager
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glLineWidth(2f)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glDepthMask(false)
            RenderUtils.glColor(if (modeValue.get().equals("minesucht", ignoreCase = true) && mc.thePlayer.position.y.toDouble() != y + 1) Color(255, 0, 0, 90) else if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(x + 0.5 - mc.thePlayer.posX, y + 1.0 - mc.thePlayer.posY, z + 0.5 - mc.thePlayer.posZ)).isNotEmpty()) Color(255, 0, 0, 90) else Color(0, 255, 0, 90))
            RenderUtils.drawFilledBox(AxisAlignedBB(x - renderManager.renderPosX, y + 1 - renderManager.renderPosY, z - renderManager.renderPosZ, x - renderManager.renderPosX + 1.0, y + 1.2 - renderManager.renderPosY, z - renderManager.renderPosZ + 1.0))
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDepthMask(true)
            GL11.glDisable(GL11.GL_BLEND)
            RenderUtils.renderNameTag(Math.round(mc.thePlayer.getDistance(x + 0.5, y + 1.0, z + 0.5)).toString() + "m", x + 0.5, y + 1.7, z + 0.5)
            GlStateManager.resetColor()
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (modeValue.get().equals("aac3.5.0", ignoreCase = true) && freeze) {
            event.zeroXZ()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (disableLogger) return
        if (packet is C03PacketPlayer) {
            val packetPlayer = packet
            for (duh in arrayOf(modeValue))
            when (modeValue.get().lowercase(Locale.getDefault())) {
                "spoof" -> {
                    if (endPos == null) break
                    packetPlayer.x = endPos!!.x + 0.5
                    packetPlayer.y = (endPos!!.y + 1).toDouble()
                    packetPlayer.z = endPos!!.z + 0.5
                    mc.thePlayer.setPosition(endPos!!.x + 0.5, (endPos!!.y + 1).toDouble(), endPos!!.z + 0.5)
                }

                "aac3.5.0" -> {
                    if (!flyTimer.hasTimePassed(60)) return
                    event.cancelEvent()
                    if (packet !is C04PacketPlayerPosition && packet !is C06PacketPlayerPosLook) return
                    packets.add(packet)
                }
            }
        }
    }

    override val tag: String
        get() = modeValue.get()
}
