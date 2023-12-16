/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.entity.AbstractClientPlayer
import java.awt.Color

@ModuleInfo(name = "TestRender", description = "math gaming.", category = ModuleCategory.RENDER)
class TestRender : Module() {
    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        RenderUtils.drawCustomFan(300f, 300f, 0f, 360f, 50f, 1f, Color(1f, 1f, 1f, 1f))

        if (mc.thePlayer.ticksExisted % 50 == 0) {
            chat((mc.thePlayer as AbstractClientPlayer).skinType)
        }
    }
}
