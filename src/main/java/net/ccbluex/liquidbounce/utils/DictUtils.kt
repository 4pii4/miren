package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.LiquidBounce
import java.io.File
import java.util.*
import kotlin.random.Random

object DictUtils {

    private val words = mutableListOf<String>()

    fun init() {
        val dictFile = File(LiquidBounce.fileManager.dir, "dict.txt")
        if (!dictFile.exists()) {
            dictFile.writeText(LiquidBounce::class.java.getResource("/assets/minecraft/liquidbounce+/dict.txt").readText())
            ClientUtils.logger.info("[DictUtils] Extracted dictionary")
        }

        words.clear()
        words.addAll(dictFile.readText().lines().filter { !it.contains(Regex("\\s")) })
        ClientUtils.logger.info("[DictUtils] Loaded ${words.size} words from dictionary")

    }

    private fun getInternal(format: String): String {
        var name = format
        name = name
            .replace(Regex("%w")) { words.random() }
            .replace(Regex("%W")) { words.random().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }
            .replace(Regex("%d")) { Random.nextInt(10).toString() }
            .replace(Regex("%c")) { "abcdefghijklmnopqrstuvwxyz".random().toString() }
            .replace(Regex("%C")) { "ABCDEFGHIJKLMNOPQRSTUVWXYZ".random().toString() }

        return name
    }

    fun get(format: String): String {
        var s: String
        do {
            s = getInternal(format)
        } while (s.length > 16)

        return s
    }
}