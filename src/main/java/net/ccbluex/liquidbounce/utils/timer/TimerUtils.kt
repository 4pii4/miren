/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils.timer

import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.minecraft.util.MathHelper

class TimerUtils {
    private var lastMS = 0L
    private var previousTime: Long

    init {
        previousTime = -1L
    }

    private val currentMS: Long
        private get() = System.nanoTime() / 1000000L

    fun hasReached(milliseconds: Double): Boolean {
        return (currentMS - lastMS).toDouble() >= milliseconds
    }

    fun delay(milliSec: Float): Boolean {
        return (time - lastMS).toFloat() >= milliSec
    }

    var time: Long
        get() = System.nanoTime() / 1000000L
        set(time) {
            lastMS = time
        }

    fun hasTimeElapsed(time: Long): Boolean {
        return System.currentTimeMillis() - lastMS > time
    }

    fun check(milliseconds: Float): Boolean {
        return System.currentTimeMillis() - previousTime >= milliseconds
    }

    fun delay(milliseconds: Double): Boolean {
        return MathHelper.clamp_float((currentMS - lastMS).toFloat(), 0f, milliseconds.toFloat()) >= milliseconds
    }

    fun reset() {
        previousTime = System.currentTimeMillis()
        lastMS = currentMS
    }

    fun time(): Long {
        return System.nanoTime() / 1000000L - lastMS
    }

    fun delay(nextDelay: Long): Boolean {
        return System.currentTimeMillis() - lastMS >= nextDelay
    }

    fun delay(nextDelay: Float, reset: Boolean): Boolean {
        if (System.currentTimeMillis() - lastMS >= nextDelay) {
            if (reset) {
                reset()
            }
            return true
        }
        return false
    }

    companion object {
        @JvmStatic
        fun randomDelay(minDelay: Int, maxDelay: Int): Long {
            return RandomUtils.nextInt(minDelay, maxDelay).toLong()
        }

        fun randomClickDelay(minCPS: Int, maxCPS: Int): Long {
            return (Math.random() * (1000 / minCPS - 1000 / maxCPS + 1) + 1000 / maxCPS).toLong()
        }
    }
}