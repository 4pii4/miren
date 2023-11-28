package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.client.Minecraft
import kotlin.math.max
import kotlin.math.min

class AnimationHelper {
    var animationX = 0f
    var alpha = 0

    constructor() {
        alpha = 0
    }

    constructor(value: BoolValue) {
        animationX = (if (value.get()) 5 else -5).toFloat()
    }

    constructor(module: Module) {
        animationX = (if (module.state) 5 else -5).toFloat()
    }

    companion object {
        fun clamp(number: Float, min: Float, max: Float): Float {
            return if (number < min) min else min(number, max)
        }

        @JvmStatic
        fun calculateCompensation(target: Float, current: Float, delta: Long, speed: Int): Float {
            var current = current
            var delta = delta
            val diff = current - target
            if (delta < 1L) {
                delta = 1L
            }
            val xD: Double
            if (diff > speed.toFloat()) {
                xD = if ((speed.toLong() * delta / 16L).toDouble() < 0.25) 0.5 else (speed.toLong() * delta / 16L).toDouble()
                current = (current.toDouble() - xD).toFloat()
                if (current < target) {
                    current = target
                }
            } else if (diff < (-speed).toFloat()) {
                xD = if ((speed.toLong() * delta / 16L).toDouble() < 0.25) 0.5 else (speed.toLong() * delta / 16L).toDouble()
                current = (current.toDouble() + xD).toFloat()
                if (current > target) {
                    current = target
                }
            } else {
                current = target
            }
            return current
        }

        @JvmStatic
        fun animate(target: Double, current: Double, speed: Double): Double {
            var current = current
            var speed = speed
            val larger: Boolean
            larger = target > current
            val bl = larger
            if (speed < 0.0) {
                speed = 0.0
            } else if (speed > 1.0) {
                speed = 1.0
            }
            val dif = max(target, current) - min(target, current)
            var factor = dif * speed
            if (factor < 0.1) {
                factor = 0.1
            }
            current = if (larger) factor.let { current += it; current } else factor.let { current -= it; current }
            return current
        }

        fun moveUD(current: Float, end: Float, smoothSpeed: Float, minSpeed: Float): Float {
            var movement = (end - current) * smoothSpeed
            if (movement > 10.0f) {
                movement = max(minSpeed, movement)
                movement = min(end - current, movement)
            } else if (movement < 10.0f) {
                movement = min(-minSpeed, movement)
                movement = max(end - current, movement)
            }
            return current + movement
        }

        fun moveTowards(current: Float, end: Float, smoothSpeed: Float, minSpeed: Float): Float {
            var movement = (end - current) * smoothSpeed
            if (movement > 0) {
                movement = max(minSpeed, movement)
                movement = min(end - current, movement)
            } else if (movement < 0) {
                movement = min(-minSpeed, movement)
                movement = max(end - current, movement)
            }
            return current + movement
        }

        var deltaTime = 0
        var speedTarget = 0.125f
        fun animation(current: Float, targetAnimation: Float, speed: Float): Float {
            return animation(current, targetAnimation, speedTarget, speed)
        }

        fun animation(animation: Float, target: Float, poxyi: Float, speedTarget: Float): Float {
            var da = (target - animation) / max(Minecraft.getDebugFPS().toFloat(), 5.0f) * 15.0f
            if (da > 0.0f) {
                da = max(speedTarget, da)
                da = min(target - animation, da)
            } else if (da < 0.0f) {
                da = min(-speedTarget, da)
                da = max(target - animation, da)
            }
            return animation + da
        }

        fun calculateCompensation(target: Float, current: Float, delta: Long, speed: Double): Float {
            var current = current
            var delta = delta
            val diff = current - target
            if (delta < 1) {
                delta = 1
            }
            if (delta > 1000) {
                delta = 16
            }
            if (diff > speed) {
                val xD = if (speed * delta / (1000 / 60) < 0.5) 0.5 else speed * delta / (1000 / 60)
                current -= xD.toFloat()
                if (current < target) {
                    current = target
                }
            } else if (diff < -speed) {
                val xD = if (speed * delta / (1000 / 60) < 0.5) 0.5 else speed * delta / (1000 / 60)
                current += xD.toFloat()
                if (current > target) {
                    current = target
                }
            } else {
                current = target
            }
            return current
        }
    }
}
