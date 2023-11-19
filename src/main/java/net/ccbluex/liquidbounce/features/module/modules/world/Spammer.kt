/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimerUtils.Companion.randomDelay
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.TextValue
import java.util.*

@ModuleInfo(name = "Spammer", description = "Spams the chat with a given message.", category = ModuleCategory.WORLD)
class Spammer : Module() {
    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 1000, 0, 5000, "ms") {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val minDelayValueObject = minDelayValue.get()
            if (minDelayValueObject > newValue) set(minDelayValueObject)
            delay = randomDelay(minDelayValue.get(), this.get())
        }
    }
    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 500, 0, 5000, "ms") {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val maxDelayValueObject = maxDelayValue.get()
            if (maxDelayValueObject < newValue) set(maxDelayValueObject)
            delay = randomDelay(this.get(), maxDelayValue.get())
        }
    }
    private val messageValue = TextValue("Message", "Example text")
    private val customValue = BoolValue("Custom", false)
    private val blankText = TextValue("Placeholder guide", "") { customValue.get() }
    private val guideFloat = TextValue("%f", "Random float") { customValue.get() }
    private val guideInt = TextValue("%i", "Random integer (max length 10000)") { customValue.get() }
    private val guideString = TextValue("%s", "Random string (max length 9)") { customValue.get() }
    private val guideShortString = TextValue("%ss", "Random short string (max length 5)") { customValue.get() }
    private val guideLongString = TextValue("%ls", "Random long string (max length 16)") { customValue.get() }
    private val msTimer = MSTimer()
    private var delay = randomDelay(minDelayValue.get(), maxDelayValue.get())
    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (msTimer.hasTimePassed(delay)) {
            mc.thePlayer.sendChatMessage(if (customValue.get()) replace(messageValue.get()) else messageValue.get() + " >" + RandomUtils.randomString(5 + Random().nextInt(5)) + "<")
            msTimer.reset()
            delay = randomDelay(minDelayValue.get(), maxDelayValue.get())
        }
    }

    private fun replace(`object`: String): String {
        var `object` = `object`
        val r = Random()
        while (`object`.contains("%f")) `object` = `object`.substring(0, `object`.indexOf("%f")) + r.nextFloat() + `object`.substring(`object`.indexOf("%f") + "%f".length)
        while (`object`.contains("%i")) `object` = `object`.substring(0, `object`.indexOf("%i")) + r.nextInt(10000) + `object`.substring(`object`.indexOf("%i") + "%i".length)
        while (`object`.contains("%s")) `object` = `object`.substring(0, `object`.indexOf("%s")) + RandomUtils.randomString(r.nextInt(8) + 1) + `object`.substring(`object`.indexOf("%s") + "%s".length)
        while (`object`.contains("%ss")) `object` = `object`.substring(0, `object`.indexOf("%ss")) + RandomUtils.randomString(r.nextInt(4) + 1) + `object`.substring(`object`.indexOf("%ss") + "%ss".length)
        while (`object`.contains("%ls")) `object` = `object`.substring(0, `object`.indexOf("%ls")) + RandomUtils.randomString(r.nextInt(15) + 1) + `object`.substring(`object`.indexOf("%ls") + "%ls".length)
        return `object`
    }
}
