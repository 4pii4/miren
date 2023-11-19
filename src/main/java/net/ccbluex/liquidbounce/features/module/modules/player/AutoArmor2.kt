/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.timer.TimerUtils
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack

@ModuleInfo(name = "VirtueAutoArmor", spacedName = "Virtue Auto Armor", description = "Automatically equips the best armor in your inventory.", category = ModuleCategory.PLAYER)
class AutoArmor2 : Module() {
    private val bestArmor = IntArray(4)
    private val timer = TimerUtils()
    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (timer.hasReached(250.0)) {
            for (i in bestArmor.indices) {
                bestArmor[i] = -1
            }
            for (i in 0..35) {
                val itemstack: ItemStack = mc.thePlayer.inventory.getStackInSlot(i)
                if (itemstack != null && itemstack.item is ItemArmor) {
                    val armor = itemstack.item as ItemArmor
                    if (armor.damageReduceAmount > bestArmor[3 - armor.armorType]) {
                        bestArmor[3 - armor.armorType] = i
                    }
                }
            }
            for (i in 0..3) {
                val itemstack: ItemStack = mc.thePlayer.inventory.armorItemInSlot(i)
                var currentArmor: ItemArmor? = null
                if (itemstack != null && itemstack.item is ItemArmor) {
                    currentArmor = itemstack.item as ItemArmor
                }
                val bestArmor: ItemArmor = try {
                    mc.thePlayer.inventory.getStackInSlot(this.bestArmor[i]).item as ItemArmor
                } catch (e: Exception) {
                    continue
                }
                if (bestArmor == null || currentArmor != null && bestArmor.damageReduceAmount <= currentArmor.damageReduceAmount) {
                    continue
                }
                if (mc.thePlayer.inventory.firstEmptyStack == -1 && currentArmor == null) {
                    continue
                }
                mc.playerController.windowClick(0, 8 - i, 0, 1, mc.thePlayer)
                mc.playerController.windowClick(0, if (this.bestArmor[i] < 9) 36 + this.bestArmor[i] else this.bestArmor[i], 0, 1, Minecraft.getMinecraft().thePlayer)
            }
            timer.reset()
        }
    }
}