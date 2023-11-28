@file:JvmName("MathUtils2")

package net.ccbluex.liquidbounce.utils.math

import net.minecraft.util.Vec3
import java.math.RoundingMode
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sqrt

const val DEGREES_TO_RADIANS = 0.017453292519943295

const val RADIANS_TO_DEGREES = 57.29577951308232

/**
 * Rounds double with [x] number of decimals
 */
fun Double.round(x: Int): Double {
    require(x >= 0) { "The value of decimal places must be absolute" }

    return this.toBigDecimal().setScale(x, RoundingMode.HALF_UP).toDouble()
}

/**
 * Converts double to radians
 */
fun Double.toRadians() = this * DEGREES_TO_RADIANS

/**
 * Converts double to degrees
 */
fun Double.toDegrees() = this * RADIANS_TO_DEGREES

/**
 * Calculates Gaussian value in one dimension
 *
 * [Assignment information](https://en.wikipedia.org/wiki/Gaussian_blur)
 */
fun gaussian(x: Int, sigma: Float): Float {
    val s = sigma * sigma * 2

    return (1f / (sqrt(PI.toFloat() * s))) * exp(-(x * x) / s)
}
fun Float.toRadians() = this * 0.017453292f
fun Float.toRadiansD() = toRadians().toDouble()

operator fun Vec3.plus(vec: Vec3): Vec3 = add(vec)
operator fun Vec3.minus(vec: Vec3): Vec3 = subtract(vec)
operator fun Vec3.times(number: Double) = Vec3(xCoord * number, yCoord * number, zCoord * number)
operator fun Vec3.div(number: Double) = times(1 / number)

fun Int.toRoman(): String {
    var s = ""
    fun select(number: Int, collection: Array<Int>, size: Int): Int {
        fun show(n : Int) {
            s += when (n) {
                1  -> "I"
                4  -> "IV"
                5  -> "V"
                9  -> "IX"
                10 -> "X"
                40 -> "XL"
                50 -> "L"
                90 -> "XC"
                100 -> "C"
                400 -> "CD"
                500 -> "D"
                900 -> "DM"
                1000 -> "M"
                else -> ""
            }
        }
        var n = 1
        var i = 0
        while (i < size) {
            if (number >= collection[i])
                n = collection[i]
            else
                break

            i += 1
        }
        show(n)
        return number - n
    }

    var number = this
    if (number <= 0)
        return ""

    val collection : Array<Int> = arrayOf(1, 4, 5, 9, 10, 40, 50, 90, 100, 400, 500, 900, 1000)
    val size : Int = collection.count()
    while (number > 0)
        number = select(number, collection, size)

    return s
}