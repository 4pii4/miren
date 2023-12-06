package net.ccbluex.liquidbounce.utils.render.animations.impl

import net.ccbluex.liquidbounce.utils.render.animations.Animation
import net.ccbluex.liquidbounce.utils.render.animations.Direction

open class CustomAnimation(duration: Int, target: Double, direction: Direction, val easeFunc: (Double) -> Double): Animation(duration, target, direction) {
    override fun getEquation(x: Double) = easeFunc(x)
}