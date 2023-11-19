package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce.moduleManager
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.utils.render.AttackParticle
import net.ccbluex.liquidbounce.utils.render.ColorUtils.LiquidSlowly
import net.ccbluex.liquidbounce.utils.render.ColorUtils.fade
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.TimerUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.util.Vec3
import java.awt.Color
import java.util.*
import java.util.function.Consumer

@ModuleInfo(name = "AttackEffect", spacedName = "Attack Effect", description = "Gey", category = ModuleCategory.RENDER)
class AttackEffect : Module() {
    private val amount = IntegerValue("Amount", 8, 0, 30)
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Health", "Rainbow", "Sky", "LiquidSlowly", "Fade", "Mixer"), "Custom")
    private val colorRedValue = IntegerValue("Red", 255, 0, 255)
    private val colorGreenValue = IntegerValue("Green", 255, 0, 255)
    private val colorBlueValue = IntegerValue("Blue", 255, 0, 255)
    private val saturationValue = FloatValue("Saturation", 1f, 0f, 1f)
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)
    private val mixerSecondsValue = IntegerValue("Seconds", 2, 1, 10)
    private val particles: MutableList<AttackParticle> = LinkedList()
    private val timer = TimerUtils()
    @EventTarget
    private fun onUpdate(event: UpdateEvent) {
        val killAura = moduleManager.getModule(KillAura::class.java)
        if (killAura!!.state) {
            if (killAura.target!!.hurtTime != 0) {
                for (i in 1 until amount.get()) {
                    particles.add(AttackParticle(Vec3(killAura.target!!.posX + (Math.random() - 0.5) * 0.5, killAura.target!!.posY + Math.random() + 0.5, killAura.target!!.posZ + (Math.random() - 0.5) * 0.5)))
                }
            }
        }
    }

    @EventTarget
    private fun onRender3D(event: Render3DEvent) {
        if (particles.isEmpty()) {
            return
        }
        var i = 0
        while (i.toDouble() <= timer.time.toDouble() / 1.0E11) {
            particles.forEach(Consumer { obj: AttackParticle -> obj.updateWithoutPhysics() })
            ++i
        }
        particles.removeIf { particle: AttackParticle -> mc.thePlayer.getDistanceSq(particle.position.xCoord, particle.position.yCoord, particle.position.zCoord) > 300.0 }
        timer.reset()
        RenderUtils.renderParticles(particles, color)
    }

    val color: Color
        get() = when (colorModeValue.get()) {
            "Custom" -> Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
            "Rainbow" -> Color(RenderUtils.getRainbowOpaque(mixerSecondsValue.get(), saturationValue.get(), brightnessValue.get(), 0))
            "Sky" -> RenderUtils.skyRainbow(0, saturationValue.get(), brightnessValue.get())
            "LiquidSlowly" -> LiquidSlowly(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get())
            "Mixer" -> ColorMixer.getMixedColor(0, mixerSecondsValue.get())
            "Fade" -> fade(Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), 0, 100)
            else -> Color.white
        }
}
