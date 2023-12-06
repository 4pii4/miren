package net.ccbluex.liquidbounce.utils.render.animations.impl

import net.ccbluex.liquidbounce.utils.render.animations.Animation
import net.ccbluex.liquidbounce.utils.render.animations.Direction
import kotlin.math.max
import kotlin.math.pow

class EaseBackIn : Animation {
    private val easeAmount: Float

    constructor(ms: Int, endPoint: Double, easeAmount: Float) : super(ms, endPoint) {
        this.easeAmount = easeAmount
    }

    constructor(ms: Int, endPoint: Double, easeAmount: Float, direction: Direction) : super(ms, endPoint, direction) {
        this.easeAmount = easeAmount
    }

    override fun correctOutput(): Boolean {
        return true
    }

    override fun getEquation(x: Double): Double {
        val shrink = easeAmount + 1
        return max(0.0, 1.0 + shrink * (x - 1).pow(3.0) + easeAmount * (x - 1).pow(2.0))
    }
}
