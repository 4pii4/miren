package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.features.module.modules.world.Teams
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.entity.EntityLivingBase

@ModuleInfo(name = "RenderConfig", description = "Options for all render modules", category = ModuleCategory.RENDER)
object RenderConfig: Module() {
    private val teammates by BoolValue("Teammates", true)
    private val bots by BoolValue("Bots", false)
    private val targets by BoolValue("Targets", true)

    fun entities(): List<EntityLivingBase> {
        val loadedEntities =  mc.theWorld.loadedEntityList
            .filterIsInstance<EntityLivingBase>()
            .toMutableList()

        if (!bots && LiquidBounce.moduleManager.getModule(AntiBot::class.java)!!.state)
            loadedEntities.removeIf { AntiBot.isBot(it) }
        if (!teammates && LiquidBounce.moduleManager.getModule(Teams::class.java)!!.state)
            loadedEntities.removeIf { LiquidBounce.moduleManager.getModule(Teams::class.java)!!.isInYourTeam(it) }
        if (!targets)
            loadedEntities.removeIf { !EntityUtils.isSelected(it, false) }
        return loadedEntities
    }

    fun shouldRender(entity: EntityLivingBase): Boolean {
        if (!bots && LiquidBounce.moduleManager.getModule(AntiBot::class.java)!!.state && AntiBot.isBot(entity) )
            return false
        if (!teammates && LiquidBounce.moduleManager.getModule(Teams::class.java)!!.state && LiquidBounce.moduleManager.getModule(Teams::class.java)!!.isInYourTeam(entity))
            return false
        if (!targets && !EntityUtils.isSelected(entity, false))
            return false
        return true
    }
}