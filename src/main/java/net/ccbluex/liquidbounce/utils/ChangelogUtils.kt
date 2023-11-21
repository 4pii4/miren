package net.ccbluex.liquidbounce.utils

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.liuli.elixir.utils.HttpUtils
import net.ccbluex.liquidbounce.LiquidBounce

object ChangelogUtils {
    var buildMsg: String = ""
    val changes = mutableListOf<String>()

    fun update() {
        changes.clear()
        try {
            val commits: JsonArray = JsonParser().parse(HttpUtils.get("https://api.github.com/repos/${LiquidBounce.CLIENT_REPO}/commits")).asJsonArray
            changes.addAll(commits[0].asJsonObject["commit"].asJsonObject["message"].asString.split("\n").filter { it.isNotEmpty() })
            buildMsg = changes.removeAt(0)
        } catch (_: Exception) {
            println("Failed to fetch changelog")
        }
    }
}