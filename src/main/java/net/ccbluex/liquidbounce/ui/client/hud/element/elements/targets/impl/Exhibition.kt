/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.impl

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Target
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.targets.TargetStyle
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.darker
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.render.BlendUtils
import net.ccbluex.liquidbounce.utils.render.GLUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import java.awt.Color

class Exhibition(inst: Target): TargetStyle("Exhibition", inst, true) {

    override fun drawTarget(entity: EntityPlayer) {
        updateAnim(entity.health)
        val font = Fonts.fontTahoma
        val minWidth = 140F.coerceAtLeast(47F + font.getStringWidth(entity.name))

        RenderUtils.drawExhiRect(0F, 0F, minWidth, 45F, 1F - targetInstance.getFadeProgress())

        RenderUtils.drawRect(2.5F, 2.5F, 42.5F, 42.5F, getColor(Color(60, 60, 60)).rgb)
        RenderUtils.drawRect(3F, 3F, 42F, 42F, getColor(Color(20, 20, 20)).rgb)

        GL11.glColor4f(1f, 1f, 1f, 1f - targetInstance.getFadeProgress())
        RenderUtils.drawEntityOnScreen(22, 40, 16, entity)

        font.drawString(entity.name, 46, 5, getColor(-1).rgb)

        val barLength = 70F * (entity.health / entity.maxHealth).coerceIn(0F, 1F)
        RenderUtils.drawRect(45F, 14F, 45F + 70F, 18F, getColor(BlendUtils.getHealthColor(entity.health, entity.maxHealth).darker(0.3F)).rgb)
        RenderUtils.drawRect(45F, 14F, 45F + barLength, 18F, getColor(BlendUtils.getHealthColor(entity.health, entity.maxHealth)).rgb)

        for (i in 0..9)
            RenderUtils.drawRectBasedBorder(45F + i * 7F, 14F, 45F + (i + 1) * 7F, 18F, 0.5F, getColor(Color(60, 60, 60)).rgb)

        Fonts.fontTahomaSmall.drawString("HP:${entity.health.toInt()} | Dist:${mc.thePlayer.getDistanceToEntityBox(entity).toInt()}", 45F, 21F, getColor(-1).rgb)

        GlStateManager.resetColor()
        GL11.glPushMatrix()
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        if (mc.theWorld != null) GLUtils.enableGUIStandardItemLighting()
        GlStateManager.pushMatrix()
        GlStateManager.disableAlpha()
        mc.renderItem.zLevel = 100f
//        GlStateManager.clear(256)

        var x = 45
        val y = 28

        for (index in 3 downTo 0) {
            val stack = entity.inventory.armorInventory[index] ?: continue

            if (stack.item == null)
                continue

            mc.renderItem.renderItemIntoGUI(stack, x, y)
            mc.renderItem.renderItemOverlays(mc.fontRendererObj, stack, x, y)
//            RenderUtils.drawExhiEnchants(stack, x.toFloat(), y.toFloat(), 5)

            x += 16
        }

        val mainStack = entity.heldItem
        if (mainStack != null && mainStack.item != null) {
            mc.renderItem.renderItemIntoGUI(mainStack, x, y)
            mc.renderItem.renderItemOverlays(mc.fontRendererObj, mainStack, x, y)
//            RenderUtils.drawExhiEnchants(mainStack, x.toFloat(), y.toFloat(), 5)
        }

        mc.renderItem.zLevel = 0f
        if (mc.theWorld != null) GLUtils.disableStandardItemLighting()
        GlStateManager.enableAlpha()
        GlStateManager.popMatrix()
        GL11.glPopMatrix()
    }

    override fun handleBlur(entity: EntityPlayer) {
        val font = Fonts.fontTahoma
        val minWidth = 143F.coerceAtLeast(47F + font.getStringWidth(entity.name))
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.fastRoundedRect(-3F, -3F, minWidth, 48F, 7F)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    override fun handleShadowCut(entity: EntityPlayer) = handleBlur(entity)

    override fun handleShadow(entity: EntityPlayer) {
        val font = Fonts.fontTahoma
        val minWidth = 143F.coerceAtLeast(47F + font.getStringWidth(entity.name))

        RenderUtils.newDrawRect(-3F, -3F, minWidth, 48F, shadowOpaque.rgb)
    }

    override fun getBorder(entity: EntityPlayer?): Border {
        entity ?: return Border(-3F, -3F, 143F, 48F)

        val font = Fonts.fontTahoma
        val minWidth = 143F.coerceAtLeast(47F + font.getStringWidth(entity.name))

        return Border(-3F, -3F, minWidth, 48F)
    }

}
