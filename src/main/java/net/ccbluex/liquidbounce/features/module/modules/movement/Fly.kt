/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacketNoEvent
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockSlime
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemEnderPearl
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C03PacketPlayer.*
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.potion.Potion
import net.minecraft.util.*
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import javax.vecmath.Vector2f
import kotlin.math.*

@ModuleInfo(name = "Fly", description = "Allows you to fly in survival mode.", category = ModuleCategory.MOVEMENT, keyBind = Keyboard.KEY_F)
class Fly : Module() {
    val modeValue = ListValue(
        "Mode", arrayOf( // Motion-based fly modes, or vanilla one.
            "Motion",
            "Creative",
            "Damage",
            "Pearl",  // Specified fly modes for NCP.
            "NCP",
            "OldNCP",  // Old AAC fly modes.
            "AAC1.9.10",
            "AAC3.0.5",
            "AAC3.1.6-Gomme",
            "AAC3.3.12",
            "AAC3.3.12-Glide",
            "AAC3.3.13",  // New AAC Fly using exploit.
            "AAC5-Vanilla",  // For other servers, mostly outdated.
            "CubeCraft",
            "Rewinside",
            "TeleportRewinside",
            "FunCraft",
            "Mineplex",
            "NeruxVace",
            "Minesucht",  // Specified fly modes for Verus.
            "Verus",
            "VerusLowHop",  // Old Spartan fly modes.
            "Spartan",
            "Spartan2",
            "BugSpartan",  // Old Hypixel modes.
            "Hypixel",
            "BoostHypixel",
            "FreeHypixel",  // Other anticheats' fly modes.
            "MineSecure",
            "HawkEye",
            "HAC",
            "WatchCat",
            "Watchdog",
            "BlockDrop",  // Other exploit-based stuffs.
            "Jetpack",
            "KeepAlive",
            "Flag",
            "Clip",
            "Jump",
            "Derp",
            "Collide",
            "BlocksMC"
        ), "Motion"
    )
    private val vanillaSpeedValue = FloatValue("Speed", 2f, 0f, 5f) { modeValue.get().equals("motion", ignoreCase = true) || modeValue.get().equals("blockdrop", ignoreCase = true) || modeValue.get().equals("damage", ignoreCase = true) || modeValue.get().equals("pearl", ignoreCase = true) || modeValue.get().equals("aac5-vanilla", ignoreCase = true) || modeValue.get().equals("bugspartan", ignoreCase = true) || modeValue.get().equals("keepalive", ignoreCase = true) || modeValue.get().equals("derp", ignoreCase = true) }
    private val vanillaVSpeedValue = FloatValue("V-Speed", 2f, 0f, 5f) { modeValue.get().equals("motion", ignoreCase = true) || modeValue.get().equals("blockdrop", ignoreCase = true) }
    private val vanillaMotionYValue = FloatValue("Y-Motion", 0f, -1f, 1f) { modeValue.get().equals("motion", ignoreCase = true) || modeValue.get().equals("blockdrop", ignoreCase = true) }
    private val vanillaKickBypassValue = BoolValue("KickBypass", false) { modeValue.get().equals("motion", ignoreCase = true) || modeValue.get().equals("creative", ignoreCase = true) || modeValue.get().equals("blockdrop", ignoreCase = true) }
    private val groundSpoofValue = BoolValue("GroundSpoof", false) { modeValue.get().equals("motion", ignoreCase = true) || modeValue.get().equals("creative", ignoreCase = true) || modeValue.get().equals("blockdrop", ignoreCase = true) }
    private val ncpMotionValue = FloatValue("NCPMotion", 0f, 0f, 1f) { modeValue.get().equals("ncp", ignoreCase = true) }

    // Verus
    private val verusDmgModeValue = ListValue("Verus-DamageMode", arrayOf("None", "Instant", "InstantC06", "Jump"), "None") { modeValue.get().equals("verus", ignoreCase = true) }
    private val verusBoostModeValue = ListValue("Verus-BoostMode", arrayOf("Static", "Gradual"), "Gradual") { modeValue.get().equals("verus", ignoreCase = true) && !verusDmgModeValue.get().equals("none", ignoreCase = true) }
    private val verusReDamageValue = BoolValue("Verus-ReDamage", true) { modeValue.get().equals("verus", ignoreCase = true) && !verusDmgModeValue.get().equals("none", ignoreCase = true) && !verusDmgModeValue.get().equals("jump", ignoreCase = true) }
    private val verusReDmgTickValue = IntegerValue("Verus-ReDamage-Ticks", 20, 0, 300) { modeValue.get().equals("verus", ignoreCase = true) && !verusDmgModeValue.get().equals("none", ignoreCase = true) && !verusDmgModeValue.get().equals("jump", ignoreCase = true) && verusReDamageValue.get() }
    private val verusVisualValue = BoolValue("Verus-VisualPos", false) { modeValue.get().equals("verus", ignoreCase = true) }
    private val verusVisualHeightValue = FloatValue("Verus-VisualHeight", 0.42f, 0f, 1f) { modeValue.get().equals("verus", ignoreCase = true) && verusVisualValue.get() }
    private val verusSpeedValue = FloatValue("Verus-Speed", 5f, 0f, 10f) { modeValue.get().equals("verus", ignoreCase = true) && !verusDmgModeValue.get().equals("none", ignoreCase = true) }
    private val verusTimerValue = FloatValue("Verus-Timer", 1f, 0.1f, 10f) { modeValue.get().equals("verus", ignoreCase = true) && !verusDmgModeValue.get().equals("none", ignoreCase = true) }
    private val verusDmgTickValue = IntegerValue("Verus-Ticks", 20, 0, 300) { modeValue.get().equals("verus", ignoreCase = true) && !verusDmgModeValue.get().equals("none", ignoreCase = true) }
    private val verusSpoofGround = BoolValue("Verus-SpoofGround", false) { modeValue.get().equals("verus", ignoreCase = true) }

    // AAC
    private val aac5NoClipValue = BoolValue("AAC5-NoClip", true) { modeValue.get().equals("aac5-vanilla", ignoreCase = true) }
    private val aac5NofallValue = BoolValue("AAC5-NoFall", true) { modeValue.get().equals("aac5-vanilla", ignoreCase = true) }
    private val aac5UseC04Packet = BoolValue("AAC5-UseC04", true) { modeValue.get().equals("aac5-vanilla", ignoreCase = true) }
    private val aac5Packet = ListValue("AAC5-Packet", arrayOf("Original", "Rise", "Other"), "Original") { modeValue.get().equals("aac5-vanilla", ignoreCase = true) } // Original is from UnlegitMC/FDPClient.
    private val aac5PursePacketsValue = IntegerValue("AAC5-Purse", 7, 3, 20) { modeValue.get().equals("aac5-vanilla", ignoreCase = true) }
    private val clipDelay = IntegerValue("Clip-DelayTick", 25, 1, 50) { modeValue.get().equals("clip", ignoreCase = true) }
    private val clipH = FloatValue("Clip-Horizontal", 7.9f, 0f, 10f) { modeValue.get().equals("clip", ignoreCase = true) }
    private val clipV = FloatValue("Clip-Vertical", 1.75f, -10f, 10f) { modeValue.get().equals("clip", ignoreCase = true) }
    private val clipMotionY = FloatValue("Clip-MotionY", 0f, -2f, 2f) { modeValue.get().equals("clip", ignoreCase = true) }
    private val clipTimer = FloatValue("Clip-Timer", 1f, 0.08f, 10f) { modeValue.get().equals("clip", ignoreCase = true) }
    private val clipGroundSpoof = BoolValue("Clip-GroundSpoof", true) { modeValue.get().equals("clip", ignoreCase = true) }
    private val clipCollisionCheck = BoolValue("Clip-CollisionCheck", true) { modeValue.get().equals("clip", ignoreCase = true) }
    private val clipNoMove = BoolValue("Clip-NoMove", true) { modeValue.get().equals("clip", ignoreCase = true) }

    // Pearl
    private val pearlActivateCheck = ListValue("PearlActiveCheck", arrayOf("Teleport", "Damage"), "Teleport") { modeValue.get().equals("pearl", ignoreCase = true) }

    // AAC
    private val aacSpeedValue = FloatValue("AAC1.9.10-Speed", 0.3f, 0f, 1f) { modeValue.get().equals("aac1.9.10", ignoreCase = true) }
    private val aacFast = BoolValue("AAC3.0.5-Fast", true) { modeValue.get().equals("aac3.0.5", ignoreCase = true) }
    private val aacMotion = FloatValue("AAC3.3.12-Motion", 10f, 0.1f, 10f) { modeValue.get().equals("aac3.3.12", ignoreCase = true) }
    private val aacMotion2 = FloatValue("AAC3.3.13-Motion", 10f, 0.1f, 10f) { modeValue.get().equals("aac3.3.13", ignoreCase = true) }
    private val hypixelBoostMode = ListValue("BoostHypixel-Mode", arrayOf("Default", "MorePackets", "NCP"), "Default") { modeValue.get().equals("boosthypixel", ignoreCase = true) }
    private val hypixelVisualY = BoolValue("BoostHypixel-VisualY", true) { modeValue.get().equals("boosthypixel", ignoreCase = true) }
    private val hypixelC04 = BoolValue("BoostHypixel-MoreC04s", false) { modeValue.get().equals("boosthypixel", ignoreCase = true) }

    // Hypixel
    private val hypixelBoost = BoolValue("Hypixel-Boost", true) { modeValue.get().equals("hypixel", ignoreCase = true) }
    private val hypixelBoostDelay = IntegerValue("Hypixel-BoostDelay", 1200, 0, 2000) { modeValue.get().equals("hypixel", ignoreCase = true) }
    private val hypixelBoostTimer = FloatValue("Hypixel-BoostTimer", 1f, 0f, 5f) { modeValue.get().equals("hypixel", ignoreCase = true) }
    private val mineplexSpeedValue = FloatValue("MineplexSpeed", 1f, 0.5f, 10f) { modeValue.get().equals("mineplex", ignoreCase = true) }
    private val neruxVaceTicks = IntegerValue("NeruxVace-Ticks", 6, 0, 20) { modeValue.get().equals("neruxvace", ignoreCase = true) }

    // General
    private val resetMotionValue = BoolValue("ResetMotion", true)

    // Visuals
    private val fakeDmgValue = BoolValue("FakeDamage", true)
    private val bobbingValue = BoolValue("Bobbing", true)
    private val bobbingAmountValue = FloatValue("BobbingAmount", 0.2f, 0f, 1f) { bobbingValue.get() }
    private val markValue = BoolValue("Mark", true)
    private var startY = 0.0
    private val flyTimer = MSTimer()
    private val groundTimer = MSTimer()
    private val mineSecureVClipTimer = MSTimer()
    private val mineplexTimer = MSTimer()
    private val spartanTimer = TickTimer()
    private val verusTimer = TickTimer()
    private val hypixelTimer = TickTimer()
    private val cubecraftTeleportTickTimer = TickTimer()
    private val freeHypixelTimer = TickTimer()
    private var shouldFakeJump = false
    private var shouldActive = false
    private var noPacketModify = false
    private var isBoostActive = false
    private var noFlag = false
    private var pearlState = 0
    private var startVec: Vec3? = null
    private var rotationVec: Vector2f? = null
    private var wasDead = false
    private var boostTicks = 0
    private var dmgCooldown = 0
    private var verusJumpTimes = 0
    var wdState = 0
    var wdTick = 0
    private var verusDmged = false
    private var shouldActiveDmg = false
    private var lastYaw = 0f
    private var lastPitch = 0f
    private var moveSpeed = 0.0
    private var expectItemStack = -1
    private var aacJump = 0.0
    private var aac3delay = 0
    private var aac3glideDelay = 0
    private var minesuchtTP: Long = 0
    private var boostHypixelState = 1
    private var lastDistance = 0.0
    private var failedStart = false
    private var freeHypixelYaw = 0f
    private var freeHypixelPitch = 0f
    private var bmcSpeed = 0.0
    private var started = false
    private fun doMove(h: Double, v: Double) {
        if (mc.thePlayer == null) return
        val x = mc.thePlayer.posX
        val y = mc.thePlayer.posY
        val z = mc.thePlayer.posZ
        val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
        val expectedX = x + -sin(yaw) * h
        val expectedY = y + v
        val expectedZ = z + cos(yaw) * h
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(expectedX, expectedY, expectedZ, mc.thePlayer.onGround))
        mc.thePlayer.setPosition(expectedX, expectedY, expectedZ)
    }

    private fun hClip(x: Double, y: Double, z: Double) {
        if (mc.thePlayer == null) return
        val expectedX = mc.thePlayer.posX + x
        val expectedY = mc.thePlayer.posY + y
        val expectedZ = mc.thePlayer.posZ + z
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(expectedX, expectedY, expectedZ, mc.thePlayer.onGround))
        mc.thePlayer.setPosition(expectedX, expectedY, expectedZ)
    }

    private fun getMoves(h: Double, v: Double): DoubleArray {
        if (mc.thePlayer == null) return doubleArrayOf(0.0, 0.0, 0.0)
        val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
        val expectedX = -sin(yaw) * h
        val expectedZ = cos(yaw) * h
        return doubleArrayOf(expectedX, v, expectedZ)
    }

    override fun onEnable() {
        if (mc.thePlayer == null) return
        noPacketModify = true
        verusTimer.reset()
        flyTimer.reset()
        shouldFakeJump = false
        shouldActive = true
        isBoostActive = false
        expectItemStack = -1
        val x = mc.thePlayer.posX
        val y = mc.thePlayer.posY
        val z = mc.thePlayer.posZ
        lastYaw = mc.thePlayer.rotationYaw
        lastPitch = mc.thePlayer.rotationPitch
        val mode = modeValue.get()
        boostTicks = 0
        dmgCooldown = 0
        pearlState = 0
        verusJumpTimes = 0
        verusDmged = false
        moveSpeed = 0.0
        wdState = 0
        wdTick = 0
        bmcSpeed = 0.0
        started = false

        duh@
        for (duh in arrayOf(mode)) {
            when (mode.lowercase(Locale.getDefault())) {
                "ncp" -> {
                    mc.thePlayer.motionY = -ncpMotionValue.get().toDouble()
                    if (mc.gameSettings.keyBindSneak.isKeyDown) mc.thePlayer.motionY = -0.5
                    MovementUtils.strafe()
                }

                "blockdrop" -> {
                    startVec = Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
                    rotationVec = Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
                }

                "oldncp" -> {
                    if (startY > mc.thePlayer.posY) mc.thePlayer.motionY = -0.000000000000000000000000000000001
                    if (mc.gameSettings.keyBindSneak.isKeyDown) mc.thePlayer.motionY = -0.2
                    if (mc.gameSettings.keyBindJump.isKeyDown && mc.thePlayer.posY < startY - 0.1) mc.thePlayer.motionY = 0.2
                    MovementUtils.strafe()
                }

                "verus" -> {
                    if (verusDmgModeValue.get().equals("Instant", ignoreCase = true)) {
                        if (mc.thePlayer.onGround && mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, 4.0, 0.0).expand(0.0, 0.0, 0.0)).isEmpty()) {
                            sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, y + 4, mc.thePlayer.posZ, false))
                            sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, y, mc.thePlayer.posZ, false))
                            sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, y, mc.thePlayer.posZ, true))
                            mc.thePlayer.motionZ = 0.0
                            mc.thePlayer.motionX = mc.thePlayer.motionZ
                            if (verusReDamageValue.get()) dmgCooldown = verusReDmgTickValue.get()
                        }
                    } else if (verusDmgModeValue.get().equals("InstantC06", ignoreCase = true)) {
                        if (mc.thePlayer.onGround && mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, 4.0, 0.0).expand(0.0, 0.0, 0.0)).isEmpty()) {
                            sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, y + 4, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false))
                            sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, y, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false))
                            sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, y, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, true))
                            mc.thePlayer.motionZ = 0.0
                            mc.thePlayer.motionX = mc.thePlayer.motionZ
                            if (verusReDamageValue.get()) dmgCooldown = verusReDmgTickValue.get()
                        }
                    } else if (verusDmgModeValue.get().equals("Jump", ignoreCase = true)) {
                        if (mc.thePlayer.onGround) {
                            mc.thePlayer.jump()
                            verusJumpTimes = 1
                        }
                    } else {
                        // set dmged = true since there's no damage method
                        verusDmged = true
                    }
                    if (verusVisualValue.get()) mc.thePlayer.setPosition(mc.thePlayer.posX, y + verusVisualHeightValue.get(), mc.thePlayer.posZ)
                    shouldActiveDmg = dmgCooldown > 0
                }

                "bugspartan" -> {
                    var i = 0
                    while (i < 65) {
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.049, z, false))
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y, z, false))
                        ++i
                    }
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y + 0.1, z, true))
                    mc.thePlayer.motionX *= 0.1
                    mc.thePlayer.motionZ *= 0.1
                    mc.thePlayer.swingItem()
                }

                "funcraft" -> {
                    if (mc.thePlayer.onGround) mc.thePlayer.jump()
                    moveSpeed = 1.0
                }

                "watchdog" -> {
                    expectItemStack = slimeSlot
                    if (expectItemStack == -1) {
                        LiquidBounce.hud.addNotification(Notification("The fly requires slime blocks to be activated properly.", Type.ERROR, 500, "Fly"))
                        break
                    }
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump()
                        wdState = 1
                    }
                }

                "boosthypixel" -> {
                    if (!mc.thePlayer.onGround) break
                    if (hypixelC04.get()) {
                        var i = 0
                        while (i < 10) {
                            //Imagine flagging to NCP.
                            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                            i++
                        }
                    }
                    if (hypixelBoostMode.get().equals("ncp", ignoreCase = true)) {
                        var i = 0
                        while (i < 65) {
                            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.049, mc.thePlayer.posZ, false))
                            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
                            i++
                        }
                    } else {
                        var fallDistance = if (hypixelBoostMode.get().equals("morepackets", ignoreCase = true)) 3.4025 else 3.0125 //add 0.0125 to ensure we get the fall dmg
                        while (fallDistance > 0) {
                            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0624986421, mc.thePlayer.posZ, false))
                            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0625, mc.thePlayer.posZ, false))
                            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0624986421, mc.thePlayer.posZ, false))
                            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0000013579, mc.thePlayer.posZ, false))
                            fallDistance -= 0.0624986421
                        }
                    }
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                    if (hypixelVisualY.get()) {
                        mc.thePlayer.jump()
                        mc.thePlayer.posY += 0.42 // Visual
                    }
                    boostHypixelState = 1
                    moveSpeed = 0.1
                    lastDistance = 0.0
                    failedStart = false
                }
            }
        }

        startY = mc.thePlayer.posY
        noPacketModify = false
        aacJump = -3.8
        if (mode.equals("freehypixel", ignoreCase = true)) {
            freeHypixelTimer.reset()
            mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, mc.thePlayer.posY + 0.42, mc.thePlayer.posZ)
            freeHypixelYaw = mc.thePlayer.rotationYaw
            freeHypixelPitch = mc.thePlayer.rotationPitch
        }
        if (!mode.equals("watchdog", ignoreCase = true)
            && !mode.equals("bugspartan", ignoreCase = true) && !mode.equals("verus", ignoreCase = true) && !mode.equals("damage", ignoreCase = true) && !mode.lowercase(Locale.getDefault()).contains("hypixel")
            && fakeDmgValue.get()
        ) {
            mc.thePlayer.handleStatusUpdate(2.toByte())
        }
        super.onEnable()
    }

    override fun onDisable() {
        wasDead = false
        if (mc.thePlayer == null) return
        noFlag = false
        val mode = modeValue.get()
        if (resetMotionValue.get() && !mode.uppercase(Locale.getDefault()).startsWith("AAC") && !mode.equals("Hypixel", ignoreCase = true) &&
            !mode.equals("CubeCraft", ignoreCase = true) && !mode.equals("Collide", ignoreCase = true) && !mode.equals("Verus", ignoreCase = true) && !mode.equals("Jump", ignoreCase = true) && !mode.equals("creative", ignoreCase = true) || mode.equals("pearl", ignoreCase = true) && pearlState != -1
        ) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.motionZ = 0.0
        }
        if (resetMotionValue.get() && boostTicks > 0 && mode.equals("Verus", ignoreCase = true)) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
        if (mode.equals("AAC5-Vanilla", ignoreCase = true) && !mc.isIntegratedServerRunning) {
            sendAAC5Packets()
        }
        mc.thePlayer.capabilities.isFlying = false
        mc.timer.timerSpeed = 1f
        mc.thePlayer.speedInAir = 0.02f
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        val vanillaSpeed = vanillaSpeedValue.get()
        val vanillaVSpeed = vanillaVSpeedValue.get()
        mc.thePlayer.noClip = modeValue.get().equals("aac5-vanilla", ignoreCase = true) && aac5NoClipValue.get()
        for (duh in arrayOf(modeValue)) {
            when (modeValue.get().lowercase(Locale.getDefault())) {
                "motion", "blockdrop" -> {
                    mc.thePlayer.capabilities.isFlying = false
                    mc.thePlayer.motionY = vanillaMotionYValue.get().toDouble()
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    if (mc.gameSettings.keyBindJump.isKeyDown) mc.thePlayer.motionY += vanillaVSpeed.toDouble()
                    if (mc.gameSettings.keyBindSneak.isKeyDown) mc.thePlayer.motionY -= vanillaVSpeed.toDouble()
                    MovementUtils.strafe(vanillaSpeed)
                    handleVanillaKickBypass()
                }

                "cubecraft" -> {
                    mc.timer.timerSpeed = 0.6f
                    cubecraftTeleportTickTimer.update()
                }

                "ncp" -> {
                    mc.thePlayer.motionY = -ncpMotionValue.get().toDouble()
                    if (mc.gameSettings.keyBindSneak.isKeyDown) mc.thePlayer.motionY = -0.5
                    MovementUtils.strafe()
                }

                "oldncp" -> {
                    if (startY > mc.thePlayer.posY) mc.thePlayer.motionY = -0.000000000000000000000000000000001
                    if (mc.gameSettings.keyBindSneak.isKeyDown) mc.thePlayer.motionY = -0.2
                    if (mc.gameSettings.keyBindJump.isKeyDown && mc.thePlayer.posY < startY - 0.1) mc.thePlayer.motionY = 0.2
                    MovementUtils.strafe()
                }

                "clip" -> {
                    mc.thePlayer.motionY = clipMotionY.get().toDouble()
                    mc.timer.timerSpeed = clipTimer.get()
                    if (mc.thePlayer.ticksExisted % clipDelay.get() == 0) {
                        val expectMoves = getMoves(clipH.get().toDouble(), clipV.get().toDouble())
                        if (!clipCollisionCheck.get() || mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(expectMoves[0], expectMoves[1], expectMoves[2]).expand(0.0, 0.0, 0.0)).isEmpty()) hClip(expectMoves[0], expectMoves[1], expectMoves[2])
                    }
                }

                "damage" -> {
                    mc.thePlayer.capabilities.isFlying = false
                    if (mc.thePlayer.hurtTime <= 0) break
                    mc.thePlayer.capabilities.isFlying = false
                    mc.thePlayer.motionY = 0.0
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    if (mc.gameSettings.keyBindJump.isKeyDown) mc.thePlayer.motionY += vanillaSpeed.toDouble()
                    if (mc.gameSettings.keyBindSneak.isKeyDown) mc.thePlayer.motionY -= vanillaSpeed.toDouble()
                    MovementUtils.strafe(vanillaSpeed)
                }

                "derp", "aac5-vanilla", "bugspartan" -> {
                    mc.thePlayer.capabilities.isFlying = false
                    mc.thePlayer.motionY = 0.0
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    if (mc.gameSettings.keyBindJump.isKeyDown) mc.thePlayer.motionY += vanillaSpeed.toDouble()
                    if (mc.gameSettings.keyBindSneak.isKeyDown) mc.thePlayer.motionY -= vanillaSpeed.toDouble()
                    MovementUtils.strafe(vanillaSpeed)
                }

                "verus" -> {
                    mc.thePlayer.capabilities.isFlying = false
                    run {
                        mc.thePlayer.motionZ = 0.0
                        mc.thePlayer.motionX = mc.thePlayer.motionZ
                    }
                    if (!verusDmgModeValue.get().equals("Jump", ignoreCase = true) || shouldActiveDmg || verusDmged) mc.thePlayer.motionY = 0.0
                    if (verusDmgModeValue.get().equals("Jump", ignoreCase = true) && verusJumpTimes < 5) {
                        if (mc.thePlayer.onGround) {
                            mc.thePlayer.jump()
                            verusJumpTimes += 1
                        }
                        return
                    }
                    if (shouldActiveDmg) {
                        if (dmgCooldown > 0) dmgCooldown-- else if (verusDmged) {
                            verusDmged = false
                            val y = mc.thePlayer.posY
                            if (verusDmgModeValue.get().equals("Instant", ignoreCase = true)) {
                                if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, 4.0, 0.0).expand(0.0, 0.0, 0.0)).isEmpty()) {
                                    sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, y + 4, mc.thePlayer.posZ, false))
                                    sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, y, mc.thePlayer.posZ, false))
                                    sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, y, mc.thePlayer.posZ, true))
                                    mc.thePlayer.motionZ = 0.0
                                    mc.thePlayer.motionX = mc.thePlayer.motionZ
                                }
                            } else if (verusDmgModeValue.get().equals("InstantC06", ignoreCase = true)) {
                                if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, 4.0, 0.0).expand(0.0, 0.0, 0.0)).isEmpty()) {
                                    sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, y + 4, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false))
                                    sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, y, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false))
                                    sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, y, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, true))
                                    mc.thePlayer.motionZ = 0.0
                                    mc.thePlayer.motionX = mc.thePlayer.motionZ
                                }
                            }
                            dmgCooldown = verusReDmgTickValue.get()
                        }
                    }
                    if (!verusDmged && mc.thePlayer.hurtTime > 0) {
                        verusDmged = true
                        boostTicks = verusDmgTickValue.get()
                    }
                    if (boostTicks > 0) {
                        mc.timer.timerSpeed = verusTimerValue.get()
                        val motion: Float = if (verusBoostModeValue.get().equals("static", ignoreCase = true)) verusSpeedValue.get() else boostTicks.toFloat() / verusDmgTickValue.get().toFloat() * verusSpeedValue.get()
                        boostTicks--
                        MovementUtils.strafe(motion)
                    } else if (verusDmged) {
                        mc.timer.timerSpeed = 1f
                        MovementUtils.strafe(MovementUtils.baseMoveSpeed.toFloat() * 0.6f)
                    } else {
                        mc.thePlayer.movementInput.moveForward = 0f
                        mc.thePlayer.movementInput.moveStrafe = 0f
                    }
                }

                "creative" -> {
                    mc.thePlayer.capabilities.isFlying = true
                    handleVanillaKickBypass()
                }

                "aac1.9.10" -> {
                    if (mc.gameSettings.keyBindJump.isKeyDown) aacJump += 0.2
                    if (mc.gameSettings.keyBindSneak.isKeyDown) aacJump -= 0.2
                    if (startY + aacJump > mc.thePlayer.posY) {
                        mc.netHandler.addToSendQueue(C03PacketPlayer(true))
                        mc.thePlayer.motionY = 0.8
                        MovementUtils.strafe(aacSpeedValue.get())
                    }
                    MovementUtils.strafe()
                }

                "aac3.0.5" -> {
                    if (aac3delay == 2) mc.thePlayer.motionY = 0.1 else if (aac3delay > 2) aac3delay = 0
                    if (aacFast.get()) {
                        if (mc.thePlayer.movementInput.moveStrafe.toDouble() == 0.0) mc.thePlayer.jumpMovementFactor = 0.08f else mc.thePlayer.jumpMovementFactor = 0f
                    }
                    aac3delay++
                }

                "aac3.1.6-gomme" -> {
                    mc.thePlayer.capabilities.isFlying = true
                    if (aac3delay == 2) {
                        mc.thePlayer.motionY += 0.05
                    } else if (aac3delay > 2) {
                        mc.thePlayer.motionY -= 0.05
                        aac3delay = 0
                    }
                    aac3delay++
                    if (!noFlag) mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.onGround))
                    if (mc.thePlayer.posY <= 0.0) noFlag = true
                }

                "flag" -> {
                    mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(mc.thePlayer.posX + mc.thePlayer.motionX * 999, mc.thePlayer.posY + (if (mc.gameSettings.keyBindJump.isKeyDown) 1.5624 else 0.00000001) - if (mc.gameSettings.keyBindSneak.isKeyDown) 0.0624 else 0.00000002, mc.thePlayer.posZ + mc.thePlayer.motionZ * 999, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, true))
                    mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(mc.thePlayer.posX + mc.thePlayer.motionX * 999, mc.thePlayer.posY - 6969, mc.thePlayer.posZ + mc.thePlayer.motionZ * 999, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, true))
                    mc.thePlayer.setPosition(mc.thePlayer.posX + mc.thePlayer.motionX * 11, mc.thePlayer.posY, mc.thePlayer.posZ + mc.thePlayer.motionZ * 11)
                    mc.thePlayer.motionY = 0.0
                }

                "keepalive" -> {
                    mc.netHandler.addToSendQueue(C00PacketKeepAlive())
                    mc.thePlayer.capabilities.isFlying = false
                    mc.thePlayer.motionY = 0.0
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    if (mc.gameSettings.keyBindJump.isKeyDown) mc.thePlayer.motionY += vanillaSpeed.toDouble()
                    if (mc.gameSettings.keyBindSneak.isKeyDown) mc.thePlayer.motionY -= vanillaSpeed.toDouble()
                    MovementUtils.strafe(vanillaSpeed)
                }

                "minesecure" -> {
                    mc.thePlayer.capabilities.isFlying = false
                    if (!mc.gameSettings.keyBindSneak.isKeyDown) mc.thePlayer.motionY = -0.01
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    MovementUtils.strafe(vanillaSpeed)
                    if (mineSecureVClipTimer.hasTimePassed(150) && mc.gameSettings.keyBindJump.isKeyDown) {
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 5, mc.thePlayer.posZ, false))
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(0.5, -1000.0, 0.5, false))
                        val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
                        val x = -sin(yaw) * 0.4
                        val z = cos(yaw) * 0.4
                        mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z)
                        mineSecureVClipTimer.reset()
                    }
                }

                "hac" -> {
                    mc.thePlayer.motionX *= 0.8
                    mc.thePlayer.motionZ *= 0.8
                    mc.thePlayer.motionY = if (mc.thePlayer.motionY <= -0.42) 0.42 else -0.42
                }

                "hawkeye" -> mc.thePlayer.motionY = if (mc.thePlayer.motionY <= -0.42) 0.42 else -0.42
                "teleportrewinside" -> {
                    val vectorStart = Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
                    val yaw = -mc.thePlayer.rotationYaw
                    val pitch = -mc.thePlayer.rotationPitch
                    val length = 9.9
                    val vectorEnd = Vec3(
                        sin(Math.toRadians(yaw.toDouble())) * cos(Math.toRadians(pitch.toDouble())) * length + vectorStart.xCoord,
                        sin(Math.toRadians(pitch.toDouble())) * length + vectorStart.yCoord,
                        cos(Math.toRadians(yaw.toDouble())) * cos(Math.toRadians(pitch.toDouble())) * length + vectorStart.zCoord
                    )
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(vectorEnd.xCoord, mc.thePlayer.posY + 2, vectorEnd.zCoord, true))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(vectorStart.xCoord, mc.thePlayer.posY + 2, vectorStart.zCoord, true))
                    mc.thePlayer.motionY = 0.0
                }

                "minesucht" -> {
                    val posX = mc.thePlayer.posX
                    val posY = mc.thePlayer.posY
                    val posZ = mc.thePlayer.posZ
                    if (!mc.gameSettings.keyBindForward.isKeyDown) break
                    if (System.currentTimeMillis() - minesuchtTP > 99) {
                        val vec3 = mc.thePlayer.getPositionEyes(0f)
                        val vec31 = mc.thePlayer.getLook(0f)
                        val vec32 = vec3.addVector(vec31.xCoord * 7, vec31.yCoord * 7, vec31.zCoord * 7)
                        if (mc.thePlayer.fallDistance > 0.8) {
                            mc.thePlayer.sendQueue.addToSendQueue(C04PacketPlayerPosition(posX, posY + 50, posZ, false))
                            mc.thePlayer.fall(100f, 100f)
                            mc.thePlayer.fallDistance = 0f
                            mc.thePlayer.sendQueue.addToSendQueue(C04PacketPlayerPosition(posX, posY + 20, posZ, true))
                        }
                        mc.thePlayer.sendQueue.addToSendQueue(C04PacketPlayerPosition(vec32.xCoord, mc.thePlayer.posY + 50, vec32.zCoord, true))
                        mc.thePlayer.sendQueue.addToSendQueue(C04PacketPlayerPosition(posX, posY, posZ, false))
                        mc.thePlayer.sendQueue.addToSendQueue(C04PacketPlayerPosition(vec32.xCoord, posY, vec32.zCoord, true))
                        mc.thePlayer.sendQueue.addToSendQueue(C04PacketPlayerPosition(posX, posY, posZ, false))
                        minesuchtTP = System.currentTimeMillis()
                    } else {
                        mc.thePlayer.sendQueue.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
                        mc.thePlayer.sendQueue.addToSendQueue(C04PacketPlayerPosition(posX, posY, posZ, true))
                    }
                }

                "jetpack" -> if (mc.gameSettings.keyBindJump.isKeyDown) {
                    mc.effectRenderer.spawnEffectParticle(EnumParticleTypes.FLAME.particleID, mc.thePlayer.posX, mc.thePlayer.posY + 0.2, mc.thePlayer.posZ, -mc.thePlayer.motionX, -0.5, -mc.thePlayer.motionZ)
                    mc.thePlayer.motionY += 0.15
                    mc.thePlayer.motionX *= 1.1
                    mc.thePlayer.motionZ *= 1.1
                }

                "mineplex" -> if (mc.thePlayer.inventory.getCurrentItem() == null) {
                    if (mc.gameSettings.keyBindJump.isKeyDown && mineplexTimer.hasTimePassed(100)) {
                        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.6, mc.thePlayer.posZ)
                        mineplexTimer.reset()
                    }
                    if (mc.thePlayer.isSneaking && mineplexTimer.hasTimePassed(100)) {
                        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.6, mc.thePlayer.posZ)
                        mineplexTimer.reset()
                    }
                    val blockPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY - 1, mc.thePlayer.posZ)
                    val vec = Vec3(blockPos).addVector(0.4, 0.4, 0.4).add(Vec3(EnumFacing.UP.directionVec))
                    mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem(), blockPos, EnumFacing.UP, Vec3(vec.xCoord * 0.4f, vec.yCoord * 0.4f, vec.zCoord * 0.4f))
                    MovementUtils.strafe(0.27f)
                    mc.timer.timerSpeed = 1 + mineplexSpeedValue.get()
                } else {
                    mc.timer.timerSpeed = 1f
                    state = false
                    ClientUtils.displayChatMessage("§8[§c§lMineplex-§a§lFly§8] §aSelect an empty slot to fly.")
                }

                "aac3.3.12" -> {
                    if (mc.thePlayer.posY < -70) mc.thePlayer.motionY = aacMotion.get().toDouble()
                    mc.timer.timerSpeed = 1f
                    if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                        mc.timer.timerSpeed = 0.2f
                        mc.rightClickDelayTimer = 0
                    }
                }

                "aac3.3.12-glide" -> {
                    if (!mc.thePlayer.onGround) aac3glideDelay++
                    if (aac3glideDelay == 2) mc.timer.timerSpeed = 1f
                    if (aac3glideDelay == 12) mc.timer.timerSpeed = 0.1f
                    if (aac3glideDelay >= 12 && !mc.thePlayer.onGround) {
                        aac3glideDelay = 0
                        mc.thePlayer.motionY = .015
                    }
                }

                "aac3.3.13" -> {
                    if (mc.thePlayer.isDead) wasDead = true
                    if (wasDead || mc.thePlayer.onGround) {
                        wasDead = false
                        mc.thePlayer.motionY = aacMotion2.get().toDouble()
                        mc.thePlayer.onGround = false
                    }
                    mc.timer.timerSpeed = 1f
                    if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                        mc.timer.timerSpeed = 0.2f
                        mc.rightClickDelayTimer = 0
                    }
                }

                "watchcat" -> {
                    MovementUtils.strafe(0.15f)
                    mc.thePlayer.isSprinting = true
                    if (mc.thePlayer.posY < startY + 2) {
                        mc.thePlayer.motionY = Math.random() * 0.5
                        break
                    }
                    if (startY > mc.thePlayer.posY) MovementUtils.strafe(0f)
                }

                "spartan" -> {
                    mc.thePlayer.motionY = 0.0
                    spartanTimer.update()
                    if (spartanTimer.hasTimePassed(12)) {
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 8, mc.thePlayer.posZ, true))
                        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 8, mc.thePlayer.posZ, true))
                        spartanTimer.reset()
                    }
                }

                "spartan2" -> {
                    MovementUtils.strafe(0.264f)
                    if (mc.thePlayer.ticksExisted % 8 == 0) mc.thePlayer.sendQueue.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 10, mc.thePlayer.posZ, true))
                }

                "pearl" -> {
                    mc.thePlayer.capabilities.isFlying = false
                    run {
                        mc.thePlayer.motionZ = 0.0
                        mc.thePlayer.motionY = mc.thePlayer.motionZ
                        mc.thePlayer.motionX = mc.thePlayer.motionY
                    }
                    val enderPearlSlot = pearlSlot
                    if (pearlState == 0) {
                        if (enderPearlSlot == -1) {
                            LiquidBounce.hud.addNotification(Notification("You don't have any ender pearl!", Type.ERROR, 500, "Fly"))
                            pearlState = -1
                            state = false
                            return
                        }
                        if (mc.thePlayer.inventory.currentItem != enderPearlSlot) {
                            mc.thePlayer.sendQueue.addToSendQueue(C09PacketHeldItemChange(enderPearlSlot))
                        }
                        mc.thePlayer.sendQueue.addToSendQueue(C05PacketPlayerLook(mc.thePlayer.rotationYaw, 90f, mc.thePlayer.onGround))
                        mc.thePlayer.sendQueue.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, mc.thePlayer.inventoryContainer.getSlot(enderPearlSlot + 36).stack, 0f, 0f, 0f))
                        if (enderPearlSlot != mc.thePlayer.inventory.currentItem) {
                            mc.thePlayer.sendQueue.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                        }
                        pearlState = 1
                    }
                    if (pearlActivateCheck.get().equals("damage", ignoreCase = true) && pearlState == 1 && mc.thePlayer.hurtTime > 0) pearlState = 2
                    if (pearlState == 2) {
                        if (mc.gameSettings.keyBindJump.isKeyDown) mc.thePlayer.motionY += vanillaSpeed.toDouble()
                        if (mc.gameSettings.keyBindSneak.isKeyDown) mc.thePlayer.motionY -= vanillaSpeed.toDouble()
                        MovementUtils.strafe(vanillaSpeed)
                    }
                }

                "jump" -> if (mc.thePlayer.onGround) mc.thePlayer.jump()
                "neruxvace" -> {
                    if (!mc.thePlayer.onGround) aac3glideDelay++
                    if (aac3glideDelay >= neruxVaceTicks.get() && !mc.thePlayer.onGround) {
                        aac3glideDelay = 0
                        mc.thePlayer.motionY = .015
                    }
                }

                "hypixel" -> {
                    val boostDelay = hypixelBoostDelay.get()
                    if (hypixelBoost.get() && !flyTimer.hasTimePassed(boostDelay)) {
                        mc.timer.timerSpeed = 1f + hypixelBoostTimer.get() * (flyTimer.hasTimeLeft(boostDelay).toFloat() / boostDelay.toFloat())
                    }
                    hypixelTimer.update()
                    if (hypixelTimer.hasTimePassed(2)) {
                        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.0E-5, mc.thePlayer.posZ)
                        hypixelTimer.reset()
                    }
                }

                "freehypixel" -> {
                    if (freeHypixelTimer.hasTimePassed(10)) {
                        mc.thePlayer.capabilities.isFlying = true
                        break
                    } else {
                        mc.thePlayer.rotationYaw = freeHypixelYaw
                        mc.thePlayer.rotationPitch = freeHypixelPitch
                        mc.thePlayer.motionY = 0.0
                        mc.thePlayer.motionZ = mc.thePlayer.motionY
                        mc.thePlayer.motionX = mc.thePlayer.motionZ
                    }
                    if (startY == BigDecimal(mc.thePlayer.posY).setScale(3, RoundingMode.HALF_DOWN).toDouble()) freeHypixelTimer.update()
                }
            }
        }
    }

    @EventTarget // drew
    fun onMotion(event: MotionEvent) {
        if (mc.thePlayer == null) return
        if (bobbingValue.get()) {
            mc.thePlayer.cameraYaw = bobbingAmountValue.get()
            mc.thePlayer.prevCameraYaw = bobbingAmountValue.get()
        }
        if (modeValue.get().equals("boosthypixel", ignoreCase = true)) {
            when (event.eventState) {
                EventState.PRE -> {
                    hypixelTimer.update()
                    if (hypixelTimer.hasTimePassed(2)) {
                        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.0E-5, mc.thePlayer.posZ)
                        hypixelTimer.reset()
                    }
                    if (!failedStart) mc.thePlayer.motionY = 0.0
                }

                EventState.POST -> {
                    val xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX
                    val zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ
                    lastDistance = sqrt(xDist * xDist + zDist * zDist)
                }
                else -> { }
            }
        }
        if (modeValue.get().equals("blockdrop", ignoreCase = true)) {
            when (event.eventState) {
                EventState.PRE -> {
                    mc.thePlayer.motionY = if (mc.gameSettings.keyBindJump.isKeyDown) 2.0 else if (mc.gameSettings.keyBindJump.isKeyDown) -2.0 else 0.0
                    var var10_8 = 0
                    while (var10_8 < 3) {
                        sendPacketNoEvent(C06PacketPlayerPosLook(startVec!!.xCoord, startVec!!.yCoord, startVec!!.zCoord, rotationVec!!.getX(), rotationVec!!.getY(), false))
                        ++var10_8
                    }
                }

                EventState.POST -> {
                    var i2 = 0
                    while (i2 < 1) {
                        sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, rotationVec!!.getX(), rotationVec!!.getY(), false))
                        ++i2
                    }
                }
                else -> { }
            }

        }
        when (modeValue.get().lowercase(Locale.getDefault())) {
            "funcraft" -> {
                event.onGround = true
                if (!MovementUtils.isMoving) moveSpeed = 0.25
                if (moveSpeed > 0.25) {
                    moveSpeed -= moveSpeed / 159.0
                }
                if (event.eventState === EventState.PRE) {
                    mc.thePlayer.capabilities.isFlying = false
                    mc.thePlayer.motionY = 0.0
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    MovementUtils.strafe(moveSpeed.toFloat())
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 8e-6, mc.thePlayer.posZ)
                }
            }

            "watchdog" -> {
                val current = mc.thePlayer.inventory.currentItem
                if (event.eventState === EventState.PRE) {
                    if (wdState == 1 && mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, -1.0, 0.0).expand(0.0, 0.0, 0.0)).isEmpty()) {
                        sendPacketNoEvent(C09PacketHeldItemChange(expectItemStack))
                        wdState = 2
                    }
                    mc.timer.timerSpeed = 1f
                    if (wdState == 3 && expectItemStack != -1) {
                        sendPacketNoEvent(C09PacketHeldItemChange(current))
                        expectItemStack = -1
                    }
                    if (wdState == 4) {
                        if (MovementUtils.isMoving) MovementUtils.strafe(MovementUtils.baseMoveSpeed.toFloat() * 0.938f) else MovementUtils.strafe(0f)
                        mc.thePlayer.motionY = -0.0015
                    } else if (wdState < 3) {
                        val rot = RotationUtils.getRotationFromPosition(mc.thePlayer.posX, mc.thePlayer.posZ, (mc.thePlayer.posY.toInt() - 1).toDouble())
                        RotationUtils.setTargetRotation(rot)
                        event.yaw = rot.yaw
                        event.pitch = rot.pitch
                    } else event.y = event.y - 0.08
                } else if (wdState == 2) {
                    if (mc.playerController.onPlayerRightClick(
                            mc.thePlayer, mc.theWorld,
                            mc.thePlayer.inventoryContainer.getSlot(expectItemStack).stack,
                            BlockPos(mc.thePlayer.posX, (mc.thePlayer.posY.toInt() - 2).toDouble(), mc.thePlayer.posZ),
                            EnumFacing.UP,
                            RotationUtils.getVectorForRotation(RotationUtils.getRotationFromPosition(mc.thePlayer.posX, mc.thePlayer.posZ, (mc.thePlayer.posY.toInt() - 1).toDouble()))
                        )
                    ) mc.netHandler.addToSendQueue(C0APacketAnimation())
                    wdState = 3
                }
            }
        }
        if (modeValue.get() === "BlocksMC") {
            if (event.eventState === EventState.PRE) {
                val bb = mc.thePlayer.entityBoundingBox.offset(0.0, 1.0, 0.0)
                if (started) {
                    mc.thePlayer.motionY += 0.025
                    MovementUtils.strafe(0.95f.let { bmcSpeed *= it; bmcSpeed }.toFloat())
                    if (mc.thePlayer.motionY < -0.5 && !MovementUtils.isBlockUnder) {
                        toggle()
                    }
                }
                if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty() && !started) {
                    started = true
                    mc.thePlayer.jump()
                    MovementUtils.strafe(4.also { bmcSpeed = it.toDouble() }.toFloat())
                }
            }
        }
    }

    fun coerceAtMost(value: Double, max: Double): Float {
        return min(value, max).toFloat()
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        val mode = modeValue.get()
        if (!markValue.get() || mode.equals("Motion", ignoreCase = true) || modeValue.get().equals("blockdrop", ignoreCase = true) || mode.equals("Creative", ignoreCase = true) || mode.equals("Damage", ignoreCase = true) || mode.equals("AAC5-Vanilla", ignoreCase = true) || mode.equals("Derp", ignoreCase = true) || mode.equals("KeepAlive", ignoreCase = true)) return
        val y = startY + 2.0
        RenderUtils.drawPlatform(y, if (mc.thePlayer.entityBoundingBox.maxY < y) Color(0, 255, 0, 90) else Color(255, 0, 0, 90), 1.0)
        when (mode.lowercase(Locale.getDefault())) {
            "aac1.9.10" -> RenderUtils.drawPlatform(startY + aacJump, Color(0, 0, 255, 90), 1.0)
            "aac3.3.12" -> RenderUtils.drawPlatform(-70.0, Color(0, 0, 255, 90), 1.0)
        }
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent?) {
        val mode = modeValue.get()
        val scaledRes = ScaledResolution(mc)
        if (mode.equals("Verus", ignoreCase = true) && boostTicks > 0) {
            val width = (verusDmgTickValue.get() - boostTicks).toFloat() / verusDmgTickValue.get().toFloat() * 60f
            RenderUtils.drawRect(scaledRes.scaledWidth / 2f - 31f, scaledRes.scaledHeight / 2f + 14f, scaledRes.scaledWidth / 2f + 31f, scaledRes.scaledHeight / 2f + 18f, -0x60000000)
            RenderUtils.drawRect(scaledRes.scaledWidth / 2f - 30f, scaledRes.scaledHeight / 2f + 15f, scaledRes.scaledWidth / 2f - 30f + width, scaledRes.scaledHeight / 2f + 17f, -0x1)
        }
        if (mode.equals("Verus", ignoreCase = true) && shouldActiveDmg) {
            val width = (verusReDmgTickValue.get() - dmgCooldown).toFloat() / verusReDmgTickValue.get().toFloat() * 60f
            RenderUtils.drawRect(scaledRes.scaledWidth / 2f - 31f, scaledRes.scaledHeight / 2f + 14f + 10f, scaledRes.scaledWidth / 2f + 31f, scaledRes.scaledHeight / 2f + 18f + 10f, -0x60000000)
            RenderUtils.drawRect(scaledRes.scaledWidth / 2f - 30f, scaledRes.scaledHeight / 2f + 15f + 10f, scaledRes.scaledWidth / 2f - 30f + width, scaledRes.scaledHeight / 2f + 17f + 10f, -0xe0e1)
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        val mode = modeValue.get()
        if (noPacketModify) return
        if (packet is C09PacketHeldItemChange && mode.equals("watchdog", ignoreCase = true) && wdState < 4) event.cancelEvent()
        if (packet is S08PacketPlayerPosLook) {
            if (mode.equals("watchdog", ignoreCase = true) && wdState == 3) {
                wdState = 4
                if (fakeDmgValue.get() && mc.thePlayer != null) mc.thePlayer.handleStatusUpdate(2.toByte())
            }
            if (mode.equals("pearl", ignoreCase = true) && pearlActivateCheck.get().equals("teleport", ignoreCase = true) && pearlState == 1) pearlState = 2
            if (mode.equals("BoostHypixel", ignoreCase = true)) {
                failedStart = true
                ClientUtils.displayChatMessage("§8[§c§lBoostHypixel-§a§lFly§8] §cSetback detected.")
            }
        }
        if (mode.equals("blockdrop", ignoreCase = true)) {
            if (packet is S08PacketPlayerPosLook) {
                if (mc.thePlayer.ticksExisted <= 20) return
                val i2 = event.packet as S08PacketPlayerPosLook
                event.cancelEvent()
                startVec = Vec3(i2.getX(), i2.getY(), i2.getZ())
                rotationVec = Vector2f(i2.getYaw(), i2.getPitch())
            }
            if (packet is C03PacketPlayer) {
                event.cancelEvent()
                return
            }
            if (packet !is C02PacketUseEntity) return
            sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false))
        }
        if (packet is C03PacketPlayer) {
            val packetPlayer = packet
            val lastOnGround = packetPlayer.onGround
            if (mode.equals("NCP", ignoreCase = true) || mode.equals("Rewinside", ignoreCase = true) || mode.equals("Mineplex", ignoreCase = true) && mc.thePlayer.inventory.getCurrentItem() == null || mode.equals("Verus", ignoreCase = true) && verusSpoofGround.get() && verusDmged) packetPlayer.onGround = true
            if (mode.equals("Hypixel", ignoreCase = true) || mode.equals("BoostHypixel", ignoreCase = true)) packetPlayer.onGround = false
            if (mode.equals("Derp", ignoreCase = true)) {
                packetPlayer.yaw = RandomUtils.nextFloat(0f, 360f)
                packetPlayer.pitch = RandomUtils.nextFloat(-90f, 90f)
            }
            if (mode.equals("AAC5-Vanilla", ignoreCase = true) && !mc.isIntegratedServerRunning) {
                if (aac5NofallValue.get()) packetPlayer.onGround = true
                aac5C03List.add(packetPlayer)
                event.cancelEvent()
                if (aac5C03List.size > aac5PursePacketsValue.get()) sendAAC5Packets()
            }
            if (mode.equals("clip", ignoreCase = true) && clipGroundSpoof.get()) packetPlayer.onGround = true
            if ((mode.equals("motion", ignoreCase = true) || modeValue.get().equals("blockdrop", ignoreCase = true) || mode.equals("creative", ignoreCase = true)) && groundSpoofValue.get()) packetPlayer.onGround = true
            if (verusDmgModeValue.get().equals("Jump", ignoreCase = true) && verusJumpTimes < 5 && mode.equals("Verus", ignoreCase = true)) {
                packetPlayer.onGround = false
            }
        }
    }

    private val aac5C03List = ArrayList<C03PacketPlayer>()
    private fun sendAAC5Packets() {
        var yaw = mc.thePlayer.rotationYaw
        var pitch = mc.thePlayer.rotationPitch
        for (packet in aac5C03List) {
            sendPacketNoEvent(packet)
            if (packet.isMoving) {
                if (packet.getRotating()) {
                    yaw = packet.yaw
                    pitch = packet.pitch
                }
                when (aac5Packet.get()) {
                    "Original" -> if (aac5UseC04Packet.get()) {
                        sendPacketNoEvent(C04PacketPlayerPosition(packet.x, 1e+159, packet.z, true))
                        sendPacketNoEvent(C04PacketPlayerPosition(packet.x, packet.y, packet.z, true))
                    } else {
                        sendPacketNoEvent(C06PacketPlayerPosLook(packet.x, 1e+159, packet.z, yaw, pitch, true))
                        sendPacketNoEvent(C06PacketPlayerPosLook(packet.x, packet.y, packet.z, yaw, pitch, true))
                    }

                    "Rise" -> if (aac5UseC04Packet.get()) {
                        sendPacketNoEvent(C04PacketPlayerPosition(packet.x, -1e+159, packet.z + 10, true))
                        sendPacketNoEvent(C04PacketPlayerPosition(packet.x, packet.y, packet.z, true))
                    } else {
                        sendPacketNoEvent(C06PacketPlayerPosLook(packet.x, -1e+159, packet.z + 10, yaw, pitch, true))
                        sendPacketNoEvent(C06PacketPlayerPosLook(packet.x, packet.y, packet.z, yaw, pitch, true))
                    }

                    "Other" -> if (aac5UseC04Packet.get()) {
                        sendPacketNoEvent(C04PacketPlayerPosition(packet.x, 1.7976931348623157E+308, packet.z, true))
                        sendPacketNoEvent(C04PacketPlayerPosition(packet.x, packet.y, packet.z, true))
                    } else {
                        sendPacketNoEvent(C06PacketPlayerPosLook(packet.x, 1.7976931348623157E+308, packet.z, yaw, pitch, true))
                        sendPacketNoEvent(C06PacketPlayerPosLook(packet.x, packet.y, packet.z, yaw, pitch, true))
                    }
                }
            }
        }
        aac5C03List.clear()
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        for (duh in arrayOf(modeValue)) {
            when (modeValue.get().lowercase(Locale.getDefault())) {
                "pearl" -> if (pearlState != 2 && pearlState != -1) {
                    event.cancelEvent()
                }

                "verus" -> if (!verusDmged) if (verusDmgModeValue.get().equals("Jump", ignoreCase = true)) event.zeroXZ() else event.cancelEvent()
                "clip" -> if (clipNoMove.get()) event.zeroXZ()
                "veruslowhop" -> if (!mc.thePlayer.isInWeb && !mc.thePlayer.isInLava && !mc.thePlayer.isInWater && !mc.thePlayer.isOnLadder && !mc.gameSettings.keyBindJump.isKeyDown && mc.thePlayer.ridingEntity == null) {
                    if (MovementUtils.isMoving) {
                        mc.gameSettings.keyBindJump.pressed = false
                        if (mc.thePlayer.onGround) {
                            mc.thePlayer.jump()
                            mc.thePlayer.motionY = 0.0
                            MovementUtils.strafe(0.61f)
                            event.y = 0.41999998688698
                        }
                        MovementUtils.strafe()
                    }
                }

                "watchdog" -> if (wdState < 4) event.zeroXZ()
                "cubecraft" -> {
                    val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
                    if (cubecraftTeleportTickTimer.hasTimePassed(2)) {
                        event.x = -sin(yaw) * 2.4
                        event.z = cos(yaw) * 2.4
                        cubecraftTeleportTickTimer.reset()
                    } else {
                        event.x = -sin(yaw) * 0.2
                        event.z = cos(yaw) * 0.2
                    }
                }

                "boosthypixel" -> {
                    if (!MovementUtils.isMoving) {
                        event.x = 0.0
                        event.z = 0.0
                        break
                    }
                    if (failedStart) break
                    val amplifier: Double = 1.0 + if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) 0.2 *
                            (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1) else 0.0
                    val baseSpeed = 0.29 * amplifier
                    when (boostHypixelState) {
                        1 -> {
                            moveSpeed = (if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) 1.56 else 2.034) * baseSpeed
                            boostHypixelState = 2
                        }

                        2 -> {
                            moveSpeed *= 2.16
                            boostHypixelState = 3
                        }

                        3 -> {
                            moveSpeed = lastDistance - (if (mc.thePlayer.ticksExisted % 2 == 0) 0.0103 else 0.0123) * (lastDistance - baseSpeed)
                            boostHypixelState = 4
                        }

                        else -> moveSpeed = lastDistance - lastDistance / 159.8
                    }
                    moveSpeed = max(moveSpeed, 0.3)
                    val yaw = MovementUtils.direction
                    event.x = -sin(yaw) * moveSpeed
                    event.z = cos(yaw) * moveSpeed
                    mc.thePlayer.motionX = event.x
                    mc.thePlayer.motionZ = event.z
                }

                "freehypixel" -> if (!freeHypixelTimer.hasTimePassed(10)) event.zero()
            }
        }
    }

    @EventTarget
    fun onBB(event: BlockBBEvent) {
        if (mc.thePlayer == null) return
        val mode = modeValue.get()
        if (event.block is BlockAir && mode.equals("Jump", ignoreCase = true) && event.y < startY) event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), (event.x + 1).toDouble(), startY, (event.z + 1).toDouble())
        if (event.block is BlockAir && (mode.equals("collide", ignoreCase = true) && !mc.thePlayer.isSneaking || mode.equals("veruslowhop", ignoreCase = true))) event.boundingBox = AxisAlignedBB(-2.0, -1.0, -2.0, 2.0, 1.0, 2.0).offset(event.x.toDouble(), event.y.toDouble(), event.z.toDouble())
        if (event.block is BlockAir && (mode.equals("Hypixel", ignoreCase = true) ||
                    mode.equals("BoostHypixel", ignoreCase = true) || mode.equals("Rewinside", ignoreCase = true) || mode.equals("Mineplex", ignoreCase = true) && mc.thePlayer.inventory.getCurrentItem() == null || mode.equals("Verus", ignoreCase = true) && (verusDmgModeValue.get().equals("none", ignoreCase = true) || verusDmged)) && event.y < mc.thePlayer.posY
        ) event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), (event.x + 1).toDouble(), mc.thePlayer.posY, (event.z + 1).toDouble())
    }

    @EventTarget
    fun onJump(e: JumpEvent) {
        val mode = modeValue.get()
        if (mode.equals("Hypixel", ignoreCase = true) || mode.equals("BoostHypixel", ignoreCase = true) ||
            mode.equals("Rewinside", ignoreCase = true) || mode.equals("Mineplex", ignoreCase = true) && mc.thePlayer.inventory.getCurrentItem() == null || mode.equals("FunCraft", ignoreCase = true) && moveSpeed > 0 || mode.equals("watchdog", ignoreCase = true) && wdState >= 1
        ) e.cancelEvent()
    }

    @EventTarget
    fun onStep(e: StepEvent) {
        val mode = modeValue.get()
        if (mode.equals("Hypixel", ignoreCase = true) || mode.equals("BoostHypixel", ignoreCase = true) ||
            mode.equals("Rewinside", ignoreCase = true) || mode.equals("Mineplex", ignoreCase = true) && mc.thePlayer.inventory.getCurrentItem() == null || mode.equals("FunCraft", ignoreCase = true) || mode.equals("watchdog", ignoreCase = true)
        ) e.stepHeight = 0f
    }

    private fun handleVanillaKickBypass() {
        if (!vanillaKickBypassValue.get() || !groundTimer.hasTimePassed(1000)) return
        val ground = calculateGround()
        run {
            var posY = mc.thePlayer.posY
            while (posY > ground) {
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, posY, mc.thePlayer.posZ, true))
                if (posY - 8.0 < ground) break // Prevent next step
                posY -= 8.0
            }
        }
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, ground, mc.thePlayer.posZ, true))
        var posY = ground
        while (posY < mc.thePlayer.posY) {
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, posY, mc.thePlayer.posZ, true))
            if (posY + 8.0 > mc.thePlayer.posY) break // Prevent next step
            posY += 8.0
        }
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
        groundTimer.reset()
    }

    // TODO: Make better and faster calculation lol
    private fun calculateGround(): Double {
        val playerBoundingBox = mc.thePlayer.entityBoundingBox
        var blockHeight = 1.0
        var ground = mc.thePlayer.posY
        while (ground > 0.0) {
            val customBox = AxisAlignedBB(playerBoundingBox.maxX, ground + blockHeight, playerBoundingBox.maxZ, playerBoundingBox.minX, ground, playerBoundingBox.minZ)
            if (mc.theWorld.checkBlockCollision(customBox)) {
                if (blockHeight <= 0.05) return ground + blockHeight
                ground += blockHeight
                blockHeight = 0.05
            }
            ground -= blockHeight
        }
        return 0.0
    }

    private val pearlSlot: Int
        get() {
            for (i in 36..44) {
                val stack = mc.thePlayer.inventoryContainer.getSlot(i).stack
                if (stack != null && stack.item is ItemEnderPearl) {
                    return i - 36
                }
            }
            return -1
        }
    private val slimeSlot: Int
        get() {
            for (i in 36..44) {
                val stack = mc.thePlayer.inventoryContainer.getSlot(i).stack
                if (stack != null && stack.item != null && stack.item is ItemBlock) {
                    val itemBlock = stack.item as ItemBlock
                    if (itemBlock.getBlock() is BlockSlime) return i - 36
                }
            }
            return -1
        }
    override val tag: String
        get() = modeValue.get()
}
