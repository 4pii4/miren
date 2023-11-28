/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils.timer

class MSTimer {
    var time = -1L

    fun hasTimePassed(ms: Number): Boolean {
        return System.currentTimeMillis() >= time + ms.toLong()
    }

    fun hasTimeLeft(ms: Number): Long {
        return ms.toLong() + time - System.currentTimeMillis()
    }

    fun reset() {
        time = System.currentTimeMillis()
    }
}
