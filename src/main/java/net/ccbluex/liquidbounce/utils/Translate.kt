package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.utils.AnimationHelper.Companion.calculateCompensation
import kotlin.math.abs

class Translate(var x: Float, var y: Float) {
    private var lastMS: Long

    init {
        lastMS = System.currentTimeMillis()
    }

    fun interpolate(targetX: Float, targetY: Float, xSpeed: Int, ySpeed: Int) {
        val currentMS = System.currentTimeMillis()
        val delta = currentMS - lastMS //16.66666
        lastMS = currentMS
        val deltaX = (abs((targetX - x).toDouble()) * 0.51f).toInt()
        val deltaY = (abs((targetY - y).toDouble()) * 0.51f).toInt()
        x = calculateCompensation(targetX, x, delta, deltaX)
        y = calculateCompensation(targetY, y, delta, deltaY)
    }

    fun interpolate(targetX: Float, targetY: Float, speed: Double) {
        val currentMS = System.currentTimeMillis()
        val delta = currentMS - lastMS //16.66666
        lastMS = currentMS
        var deltaX = 0.0
        var deltaY = 0.0
        if (speed != 0.0) {
            deltaX = abs((targetX - x).toDouble()) * 0.35f / (10 / speed)
            deltaY = abs((targetY - y).toDouble()) * 0.35f / (10 / speed)
        }
        x = calculateCompensation(targetX, x, delta, deltaX.toInt())
        y = calculateCompensation(targetY, y, delta, deltaY.toInt())
    }
}
