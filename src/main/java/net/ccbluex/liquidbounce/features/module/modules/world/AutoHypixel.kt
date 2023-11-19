/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 * 
 * This code belongs to WYSI-Foundation. Please give credits when using this in your repository.
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.Stencil
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.MathHelper
import java.awt.Color
import java.text.DecimalFormat
import java.util.*

@ModuleInfo(name = "AutoHypixel", spacedName = "Auto Hypixel", description = "Automatically send you into random games on Hypixel after you die or win.", category = ModuleCategory.WORLD)
class AutoHypixel : Module() {
    private val delayValue = IntegerValue("Delay", 0, 0, 5000, "ms")
    private val autoGGValue = BoolValue("Auto-GG", true)
    private val ggMessageValue = TextValue("GG-Message", "gOoD GaMe") { autoGGValue.get() }
    private val checkValue = BoolValue("CheckGameMode", true)
    private val antiSnipeValue = BoolValue("AntiSnipe", true)
    private val renderValue = BoolValue("Render", true)
    private val modeValue = ListValue("Mode", arrayOf("Solo", "Teams", "Ranked", "Mega"), "Solo")
    private val soloTeamsValue = ListValue("Solo/Teams-Mode", arrayOf("Normal", "Insane"), "Insane") { modeValue.get().equals("solo", ignoreCase = true) || modeValue.get().equals("teams", ignoreCase = true) }
    private val megaValue = ListValue("Mega-Mode", arrayOf("Normal", "Doubles"), "Normal") { modeValue.get().equals("mega", ignoreCase = true) }
    private val timer = MSTimer()
    var shouldChangeGame = false
    var useOtherWord = false
    private val dFormat = DecimalFormat("0.0")
    private var posY = -20f
    private val strings = arrayOf(
        "1st Killer -",
        "1st Place -",
        "died! Want to play again? Click here!",
        "won! Want to play again? Click here!",
        "- Damage Dealt -",
        "1st -",
        "Winning Team -",
        "Winners:",
        "Winner:",
        "Winning Team:",
        " win the game!",
        "1st Place:",
        "Last team standing!",
        "Winner #1 (",
        "Top Survivors",
        "Winners -"
    )

    override fun onEnable() {
        shouldChangeGame = false
        timer.reset()
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent?) {
        if (checkValue.get() && !gameMode.lowercase(Locale.getDefault()).contains("skywars")) return
        val sc = ScaledResolution(mc)
        val middleX = sc.scaledWidth / 2f
        val detail = "Next game in " + dFormat.format((timer.hasTimeLeft(delayValue.get()).toFloat() / 1000f).toDouble()) + "s..."
        val middleWidth = Fonts.font40.getStringWidth(detail) / 2f
        val strength = MathHelper.clamp_float(timer.hasTimeLeft(delayValue.get()).toFloat() / delayValue.get(), 0f, 1f)
        val wid = strength * (5f + middleWidth) * 2f
        posY = AnimationUtils.animate(if (shouldChangeGame) 10f else -20f, posY, 0.25f * 0.05f * RenderUtils.deltaTime)
        if (!renderValue.get() || posY < -15) return
        Stencil.write(true)
        RenderUtils.drawRoundedRect(middleX - 5f - middleWidth, posY, middleX + 5f + middleWidth, posY + 15f, 3f, -0x60000000)
        Stencil.erase(true)
        RenderUtils.drawRect(middleX - 5f - middleWidth, posY, middleX - 5f - middleWidth + wid, posY + 15f, Color(0.4f, 0.8f, 0.4f, 0.35f).rgb)
        Stencil.dispose()
        GlStateManager.resetColor()
        Fonts.fontSFUI40.drawString(detail, middleX - middleWidth - 1f, posY + 4f, -1)
    }

    @EventTarget
    fun onMotion(event: MotionEvent?) {
        if ((!checkValue.get() || gameMode.lowercase(Locale.getDefault()).contains("skywars")) && shouldChangeGame && timer.hasTimePassed(delayValue.get())) {
            mc.thePlayer.sendChatMessage("/play " + modeValue.get().lowercase(Locale.getDefault()) + if (modeValue.get().equals("ranked", ignoreCase = true)) "_normal" else if (modeValue.get().equals("mega", ignoreCase = true)) "_" + megaValue.get().lowercase(Locale.getDefault()) else "_" + soloTeamsValue.get().lowercase(Locale.getDefault()))
            shouldChangeGame = false
        }
        if (!shouldChangeGame) timer.reset()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is S02PacketChat) {
            val chat = event.packet
            if (chat.chatComponent != null) {
                if (antiSnipeValue.get() && chat.chatComponent.unformattedText.contains("Sending you to")) {
                    event.cancelEvent()
                    return
                }
                for (s in strings) if (chat.chatComponent.unformattedText.contains(s)) {
                    //LiquidBounce.hud.addNotification(new Notification("Attempting to send you to the next game in "+dFormat.format((double)delayValue.get()/1000D)+"s.",1000L));
                    if (autoGGValue.get() && chat.chatComponent.unformattedText.contains(strings[3])) mc.thePlayer.sendChatMessage(ggMessageValue.get())
                    shouldChangeGame = true
                    break
                }
            }
        }
    }

    companion object {
        @JvmField
        var gameMode = "NONE"
    }
}