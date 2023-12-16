/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils.render

import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import javax.imageio.ImageIO

object IconUtils {
    @JvmStatic
    val favicon: Array<ByteBuffer?>?
        get() {
            try {
                return arrayOf(
                    readImageToBuffer(IconUtils::class.java.getResourceAsStream("/assets/minecraft/liquidbounce+/icon_16x16.png")),
                    readImageToBuffer(IconUtils::class.java.getResourceAsStream("/assets/minecraft/liquidbounce+/icon_32x32.png"))
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }

    @Throws(IOException::class)
    private fun readImageToBuffer(imageStream: InputStream?): ByteBuffer? {
        if (imageStream == null) return null
        val bufferedImage = ImageIO.read(imageStream)
        val rgb = bufferedImage.getRGB(0, 0, bufferedImage.width, bufferedImage.height, null, 0, bufferedImage.width)
        val byteBuffer = ByteBuffer.allocate(4 * rgb.size)
        for (i in rgb) byteBuffer.putInt(i shl 8 or (i shr 24 and 255))
        byteBuffer.flip()
        return byteBuffer
    }
}
