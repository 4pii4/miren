package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.LiquidBounce
import java.io.File
import java.util.*
import kotlin.random.Random

object DictUtils {
//    private val dict = LiquidBounce::class.java.getResource("/assets/minecraft/liquidbounce+/dict.txt").readText().lines()

    private var dict: MutableList<String>? = null

    fun init() {
        val dictFile = File(LiquidBounce.fileManager.dir, "dict.txt")
        if (!dictFile.exists()) {
            dictFile.writeText(LiquidBounce::class.java.getResource("/assets/minecraft/liquidbounce+/dict.txt").readText())
            ClientUtils.logger.info("[DictUtils] Extracted dictionary")
        }

        dict = mutableListOf()
        dict!!.addAll(dictFile.readText().lines().filter { !it.contains(Regex("\\s")) })
        ClientUtils.logger.info("[DictUtils] Loaded ${dict!!.size} words from dictionary")

    }

    private fun getInternal(format: String): String {
        var name = format
        name = name
            .replace(Regex("%w")) { dict!!.random() }
            .replace(Regex("%W")) { dict!!.random().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }
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