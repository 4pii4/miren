/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils

import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtils {
    @Throws(IOException::class)
    fun unpackFile(file: File?, name: String?) {
        val fos = FileOutputStream(file)
        IOUtils.copy(FileUtils::class.java.getClassLoader().getResourceAsStream(name), fos)
        fos.close()
    }
}
