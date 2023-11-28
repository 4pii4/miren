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
import net.ccbluex.liquidbounce.features.module.modules.exploit.Disabler
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac.*
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.hypixel.HypixelBoost
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.hypixel.HypixelCustom
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.hypixel.HypixelNew
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.hypixel.HypixelStable
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.matrix.*
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp.*
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other.*
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.verus.VerusHard
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.verus.VerusHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.verus.VerusLowHop
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.vulcan.VulcanGroundSpeed
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.vulcan.VulcanLowHopSpeed
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.vulcan.VulcanYPort2Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.vulcan.VulcanYPortSpeed
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiIngameMenu
import net.minecraft.client.settings.GameSettings

@ModuleInfo(name = "Speed", description = "Allows you to move faster.", category = ModuleCategory.MOVEMENT)
class Speed : Module() {
    private val speedModes = arrayOf( // NCP
        NCPBHop(),
        NCPFHop(),
        SNCPBHop(),
        NCPHop(),
        NCPHop2(),
        NCPYPort(),  // AAC
        AAC4Hop(),
        AAC4SlowHop(),
        AACv4BHop(),
        AACBHop(),
        AAC2BHop(),
        AAC3BHop(),
        AAC4BHop(),
        AAC5BHop(),
        AAC6BHop(),
        AAC7BHop(),
        OldAACBHop(),
        AACPort(),
        AACLowHop(),
        AACLowHop2(),
        AACLowHop3(),
        AACGround(),
        AACGround2(),
        AACHop350(),
        AACHop3313(),
        AACHop3310(),
        AACHop438(),
        AACYPort(),
        AACYPort2(),  // Hypixel
        HypixelBoost(),
        HypixelStable(),
        HypixelCustom(),
        HypixelNew(),
        SlowHop(),
        CustomSpeed(),
        Jump(),
        Legit(),
        AEMine(),
        GWEN(),
        Boost(),
        Frame(),
        MiJump(),
        OnGround(),
        YPort(),
        YPort2(),
        HiveHop(),
        MineplexGround(),
        TeleportCubeCraft(),
        GrimCombat(),  // Verus
        VerusHop(),
        VerusLowHop(),
        VerusHard(),  //Vulcan
        VulcanGroundSpeed(),
        VulcanLowHopSpeed(),
        VulcanYPort2Speed(),
        VulcanYPortSpeed(),  // Matrix
        MatrixSemiStrafe(),
        MatrixTimerBalance(),
        MatrixMultiply(),
        MatrixDynamic(),
        Matrix692(),
        MatrixLowHop()
    )
    private val typeValue: ListValue = object : ListValue("Type", arrayOf("NCP", "AAC", "Hypixel", "Verus", "Vulcan", "Matrix", "Custom", "Other"), "NCP") {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }
    private val ncpModeValue: ListValue = object : ListValue("NCP-Mode", arrayOf("BHop", "FHop", "SBHop", "Hop", "YPort", "Hop2"), "BHop", { typeValue.get().equals("ncp", ignoreCase = true) }) {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }
    private val aacModeValue: ListValue = object : ListValue("AAC-Mode", arrayOf(
        "4Hop",
        "4SlowHop",
        "v4BHop",
        "BHop",
        "2BHop",
        "3BHop",
        "4BHop",
        "5BHop",
        "6BHop",
        "7BHop",
        "OldBHop",
        "Port",
        "LowHop",
        "LowHop2",
        "LowHop3",
        "Ground",
        "Ground2",
        "Hop3.5.0",
        "Hop3.3.13",
        "Hop3.3.10",
        "Hop4.3.8",
        "YPort",
        "YPort2"
    ), "4Hop", { typeValue.get().equals("aac", ignoreCase = true) }) {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }
    private val hypixelModeValue: ListValue = object : ListValue("Hypixel-Mode", arrayOf("Boost", "Stable", "Custom", "New"), "Stable", { typeValue.get().equals("hypixel", ignoreCase = true) }) {
        // the worst hypixel bypass ever existed
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    private val verusModeValue: ListValue = object : ListValue("Verus-Mode", arrayOf("Hop", "LowHop", "Hard"), "Hop", { typeValue.get().equals("verus", ignoreCase = true) }) {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }
    private val vulcanModeValue: ListValue = object : ListValue("Vulcan-Mode", arrayOf("GroundSpeed", "LowHopSpeed", "YPort2Speed", "YPortSpeed"), "Ground", { typeValue.get().equals("vulcan", ignoreCase = true) }) {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }
    private val matrixModeValue: ListValue = object : ListValue("Matrix-Mode", arrayOf("Matrix6.9.2", "MatrixSemiStrafe", "MatrixTimerBalance", "MatrixMultiply", "MatrixDynamic", "MatrixLowHop"), "MatrixSemiStrafe", { typeValue.get().equals("matrix", ignoreCase = true) }) {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    private val otherModeValue: ListValue = object : ListValue("Other-Mode", arrayOf("YPort", "YPort2", "Boost", "Frame", "MiJump", "OnGround", "SlowHop", "Jump", "Legit", "AEMine", "GWEN", "HiveHop", "MineplexGround", "TeleportCubeCraft", "GrimCombat"), "Boost", { typeValue.get().equals("other", ignoreCase = true) }) {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    private val modifySprint = BoolValue("ModifySprinting", true)
    val timerValue = BoolValue("UseTimer", true) { modeName.equals("hypixelcustom", true) }
    val smoothStrafe = BoolValue("SmoothStrafe", true) { modeName.equals("hypixelcustom", true) }
    val customSpeedValue = FloatValue("StrSpeed", 0.42f, 0.2f, 2f) { modeName.equals("hypixelcustom", true) }
    val motionYValue = FloatValue("MotionY", 0.42f, 0f, 2f) { modeName.equals("hypixelcustom", true) }
    val verusTimer = FloatValue("Verus-Timer", 1f, 0.1f, 10f) { modeName.equals("verushard", true) }
    val speedValue = FloatValue("CustomSpeed", 1.6f, 0.2f, 2f) { typeValue.isMode("custom") }
    val launchSpeedValue = FloatValue("CustomLaunchSpeed", 1.6f, 0.2f, 2f) { typeValue.isMode("custom") }
    val addYMotionValue = FloatValue("CustomAddYMotion", 0f, 0f, 2f) { typeValue.isMode("custom") }
    val yValue = FloatValue("CustomY", 0f, 0f, 4f) { typeValue.isMode("custom") }
    val upTimerValue = FloatValue("CustomUpTimer", 1f, 0.1f, 2f) { typeValue.isMode("custom") }
    val downTimerValue = FloatValue("CustomDownTimer", 1f, 0.1f, 2f) { typeValue.isMode("custom") }
    val strafeValue = ListValue("CustomStrafe", arrayOf("Strafe", "Boost", "Plus", "PlusOnlyUp", "Non-Strafe"), "Boost") { typeValue.isMode("custom") }
    val groundStay = IntegerValue("CustomGroundStay", 0, 0, 10) { typeValue.isMode("custom") }
    val groundResetXZValue = BoolValue("CustomGroundResetXZ", false) { typeValue.isMode("custom") }
    val resetXZValue = BoolValue("CustomResetXZ", false) { typeValue.isMode("custom") }
    val resetYValue = BoolValue("CustomResetY", false) { typeValue.isMode("custom") }
    val doLaunchSpeedValue = BoolValue("CustomDoLaunchSpeed", true) { typeValue.isMode("custom") }
    private val noBob = BoolValue("NoBob", true)
    val jumpStrafe = BoolValue("JumpStrafe", false) { typeValue.isMode("other") }
    val sendJumpValue = BoolValue("SendJump", true) { typeValue.isMode("hypixel") && !modeName.equals("hypixelcustom", true) }
    val recalcValue = BoolValue("ReCalculate", true) { typeValue.isMode("hypixel") && sendJumpValue.get() && !modeName.equals("hypixelcustom", true) }
    val glideStrengthValue = FloatValue("GlideStrength", 0.03f, 0f, 0.05f) { typeValue.isMode("hypixel") && !modeName.equals("hypixelcustom", true) }
    val moveSpeedValue = FloatValue("MoveSpeed", 1.47f, 1f, 1.7f) { typeValue.isMode("hypixel") && !modeName.equals("hypixelcustom", true) }
    val jumpYValue = FloatValue("JumpY", 0.42f, 0f, 1f) { typeValue.isMode("hypixel") && !modeName.equals("hypixelcustom", true) }
    val baseStrengthValue = FloatValue("BaseMultiplier", 1f, 0.5f, 1f) { typeValue.isMode("hypixel") && !modeName.equals("hypixelcustom", true) }
    val baseTimerValue = FloatValue("BaseTimer", 1.5f, 1f, 3f) { modeName.equals("hypixelboost", true) }
    val baseMTimerValue = FloatValue("BaseMultiplierTimer", 1f, 0f, 3f) { modeName.equals("hypixelboost", true) }
    private val bypassWarning = BoolValue("BypassWarning", true) { typeValue.isMode("hypixel") && !modeName.equals("hypixelcustom", true) }
    val customSpeedBoost = FloatValue("SpeedPotJumpModifier", 0.1f, 0f, 0.4f) { hypixelModeValue.isMode("new") }
    val portMax = FloatValue("AAC-PortLength", 1f, 1f, 20f) { typeValue.isMode("aac") }
    val aacGroundTimerValue = FloatValue("AACGround-Timer", 3f, 1.1f, 10f) { typeValue.isMode("aac") }
    val cubecraftPortLengthValue = FloatValue("CubeCraft-PortLength", 1f, 0.1f, 2f) { modeName.equals("teleportcubecraft", true) }
    val mineplexGroundSpeedValue = FloatValue("MineplexGround-Speed", 0.5f, 0.1f, 1f) { modeName.equals("mineplexground", true) }
    val onlyAir = BoolValue("OnlyAir", false) { modeName.equals("grimcombat", true) }
    val okstrafe = BoolValue("Strafe", false) { modeName.equals("grimcombat", true) }
    val speedUp = BoolValue("SpeedUp", false) { modeName.equals("grimcombat", true) }
    val speed = IntegerValue("Speed", 0, 0, 15) { modeName.equals("grimcombat", true) }
    val distance = FloatValue("Range", 0f, 0f, 2f) { modeName.equals("grimcombat", true) }
    private val tagDisplay = ListValue("Tag", arrayOf("Type", "FullName", "All"), "Type")

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (mc.thePlayer.isSneaking) return
        if (MovementUtils.isMoving && modifySprint.get()) mc.thePlayer.isSprinting = !modeName.equals("verushard", ignoreCase = true)
        val speedMode = mode
        speedMode?.onUpdate()
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState !== EventState.PRE) return
        if (MovementUtils.isMoving && modifySprint.get()) mc.thePlayer.isSprinting = !modeName.equals("verushard", ignoreCase = true)
        val speedMode = mode
        if (speedMode != null) {
            speedMode.onMotion(event)
            speedMode.onMotion()
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        val speedMode = mode
        speedMode?.onMove(event)
    }

    @EventTarget
    fun onTick(event: TickEvent) {
        if (mc.thePlayer.isSneaking) return
        val speedMode = mode
        speedMode?.onTick()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer.isSneaking) return
        val speedMode = mode
        speedMode?.onPacket(event)
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        val speedMode = mode
        speedMode?.onJump(event)
    }

    override fun onEnable() {
        if (noBob.get()) mc.gameSettings.viewBobbing = false
        if (mc.thePlayer == null) return
        if (bypassWarning.get() && typeValue.get().equals("hypixel", ignoreCase = true) && !LiquidBounce.moduleManager.getModule(Disabler::class.java)!!.state) {
            LiquidBounce.hud.addNotification(Notification("Disabler is OFF! Disable this notification in settings.", Type.WARNING, 3000, "Speed"))
        }
        mc.timer.timerSpeed = 1f
        val speedMode = mode
        speedMode?.onEnable()
    }

    override fun onDisable() {
        if (noBob.get()) mc.gameSettings.viewBobbing = true
        if (mc.thePlayer == null) return
        mc.timer.timerSpeed = 1f
        mc.gameSettings.keyBindJump.pressed = mc.thePlayer != null && (mc.inGameHasFocus || LiquidBounce.moduleManager.getModule(InvMove::class.java)!!.state) && !(mc.currentScreen is GuiIngameMenu || mc.currentScreen is GuiChat) && GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        val speedMode = mode
        speedMode?.onDisable()
    }

    override val tag: String
        get() {
            if (tagDisplay.get().equals("type", ignoreCase = true)) return typeValue.get()
            if (tagDisplay.get().equals("fullname", ignoreCase = true)) return modeName
            return if (typeValue.get() === "Other") otherModeValue.get() else if (typeValue.get() === "Custom") "Custom" else typeValue.get() + ", " + onlySingleName
        }
    private val onlySingleName: String
        get() {
            var mode = ""
            when (typeValue.get()) {
                "NCP" -> mode = ncpModeValue.get()
                "AAC" -> mode = aacModeValue.get()
                "Hypixel" -> mode = hypixelModeValue.get()
                "Verus" -> mode = verusModeValue.get()
                "Matrix" -> mode = matrixModeValue.get()
                "Vulcan" -> mode = vulcanModeValue.get()
            }
            return mode
        }
    val modeName: String
        get() {
            var mode = ""
            when (typeValue.get()) {
                "NCP" -> mode = if (ncpModeValue.get().equals("SBHop", ignoreCase = true)) "SNCPBHop" else "NCP" + ncpModeValue.get()
                "AAC" -> mode = if (aacModeValue.get().equals("oldbhop", ignoreCase = true)) "OldAACBHop" else "AAC" + aacModeValue.get()
                "Hypixel" -> mode = "Hypixel" + hypixelModeValue.get()
                "Verus" -> mode = "Verus" + verusModeValue.get()
                "Custom" -> mode = "Custom"
                "Other" -> mode = otherModeValue.get()
                "Matrix" -> mode = matrixModeValue.get()
                "Vulcan" -> mode = vulcanModeValue.get()
            }
            return mode
        }

    val mode: SpeedMode?
        get() = speedModes.find { it.modeName.equals(modeName, true) }
}
