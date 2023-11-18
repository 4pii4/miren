package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.features.module.modules.world.Teams
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase

@ModuleInfo(name = "RenderConfig", description = "Options for all render modules", category = ModuleCategory.RENDER)
object RenderConfig: Module() {
    val teammates by BoolValue("Teammates", true)
    val bots by BoolValue("Bots", false)
    val targets by BoolValue("Targets", true)

    fun entities(): List<EntityLivingBase> {
        val list =  mc.theWorld.loadedEntityList
            .filterIsInstance<EntityLivingBase>()
            .toMutableList()
        list.removeIf { (!bots && AntiBot.isBot(it)) || (!teammates && LiquidBounce.moduleManager.getModule(Teams::class.java)!!.isInYourTeam(it))
                    || (!targets && EntityUtils.isSelected(it, false)) }
        return list
    }

    fun shouldRender(entity: EntityLivingBase): Boolean {
        return bots && AntiBot.isBot(entity) || teammates && LiquidBounce.moduleManager.getModule(Teams::class.java)!!.isInYourTeam(entity)
                || targets && EntityUtils.isSelected(entity, false)
    }
}