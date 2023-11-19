package net.ccbluex.liquidbounce.utils

import net.minecraft.potion.Potion
import net.minecraft.util.ResourceLocation

object PotionUtils {
    fun getPotionIcon(potion: Potion): ResourceLocation {
        return Potion.getPotionLocations().toList()[potion.statusIconIndex]
    }
}