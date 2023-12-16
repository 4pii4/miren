//
// Decompiled by Procyon v0.5.36
//
package net.ccbluex.liquidbounce.utils.misc

import java.io.*

object FileUtils {
    fun readFile(file: File?): String {
        val stringBuilder = StringBuilder()
        try {
            val fileInputStream = FileInputStream(file)
            val bufferedReader = BufferedReader(InputStreamReader(fileInputStream))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) stringBuilder.append(line).append('\n')
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }

    fun readInputStream(inputStream: InputStream?): String {
        val stringBuilder = StringBuilder()
        try {
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) stringBuilder.append(line).append('\n')
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }
}
