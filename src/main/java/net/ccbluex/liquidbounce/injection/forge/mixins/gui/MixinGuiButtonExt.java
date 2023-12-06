/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.client.HUD;
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.AnimationUtils;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.render.ColorUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.render.ShaderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.awt.*;
import java.util.Objects;

@Mixin(GuiButtonExt.class)
public abstract class MixinGuiButtonExt extends GuiButton {

   private float bright;
   private float moveX = 0F;
   private float cut;
   private float alpha;
   private float anim;

   public MixinGuiButtonExt(int p_i1020_1_, int p_i1020_2_, int p_i1020_3_, String p_i1020_4_) {
      super(p_i1020_1_, p_i1020_2_, p_i1020_3_, p_i1020_4_);
   }

   public MixinGuiButtonExt(int p_i46323_1_, int p_i46323_2_, int p_i46323_3_, int p_i46323_4_,
                            int p_i46323_5_, String p_i46323_6_) {
      super(p_i46323_1_, p_i46323_2_, p_i46323_3_, p_i46323_4_, p_i46323_5_, p_i46323_6_);
   }

   /**
    * @author CCBlueX
    * @reason custom button
    */
   @Overwrite
   public void drawButton(Minecraft mc, int mouseX, int mouseY) {
      if (visible) {
         final FontRenderer fontRenderer =
            mc.getLanguageManager().isCurrentLocaleUnicode() ? mc.fontRendererObj : Fonts.fontSFUI40;
         hovered = (mouseX >= this.xPosition && mouseY >= this.yPosition &&
                    mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height);

         final int delta = RenderUtils.deltaTime;
         final float speedDelta = 0.01F * delta;

         final HUD hud = LiquidBounce.moduleManager.getModule(HUD.class);

         if (hud == null) return;

         if (enabled && hovered) {
            // LiquidBounce
            cut += 0.05F * delta;
            if (cut >= 4) cut = 4;
            alpha += 0.3F * delta;
            if (alpha >= 210) alpha = 210;

            // LiquidBounce+
            moveX = AnimationUtils.animate(this.width - 2.4F, moveX, speedDelta);

            // Miren
            anim = AnimationUtils.animate(1f, anim, speedDelta / 2f);
         } else {
            // LiquidBounce
            cut -= 0.05F * delta;
            if (cut <= 0) cut = 0;
            alpha -= 0.3F * delta;
            if (alpha <= 120) alpha = 120;

            // LiquidBounce+
            moveX = AnimationUtils.animate(0F, moveX, speedDelta);

            // Miren
            anim = AnimationUtils.animate(0f, anim, speedDelta / 2f);
         }

         float roundCorner = Math.max(0F, 2.4F + moveX - (this.width - 2.4F));

         switch (hud.getGuiButtonStyle().get().toLowerCase()) {
            case "miren":
               new Color(255, 255, 255, 255);
               Color backgroundColor;
               Color textColor;
               Color black = new Color(30, 30, 30, 255);
               Color white = new Color(255, 255, 255, 255);
               if (this.enabled) {
                  backgroundColor = ColorUtils.interpolateColorC(white, black, anim);
                  textColor = ColorUtils.interpolateColorC(black, white, anim);
               } else {
                  backgroundColor = new Color(180, 180, 180, 255);
                  textColor = new Color(40, 40, 40, 255);
               }
               ShaderUtils.INSTANCE.drawRoundedRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 3F, backgroundColor.getRGB());
               fontRenderer.drawString(displayString, (this.xPosition + (float) this.width / 2) - (float) fontRenderer.getStringWidth(displayString) / 2, (int) (this.yPosition + (this.height - fontRenderer.FONT_HEIGHT) / 2F + 1.5f), textColor.getRGB(), true);
               break;
            case "minecraft":
               mc.getTextureManager().bindTexture(buttonTextures);
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
               int i = this.getHoverState(this.hovered);
               GlStateManager.enableBlend();
               GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
               GlStateManager.blendFunc(770, 771);
               this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + i * 20, this.width / 2, this.height);
               this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
               this.mouseDragged(mc, mouseX, mouseY);
               int j = 14737632;

               if (!this.enabled)
               {
                  j = 10526880;
               }
               else if (this.hovered)
               {
                  j = 16777120;
               }

               this.drawCenteredString(mc.fontRendererObj, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
               break;
            case "liquidbounce":
               Gui.drawRect(this.xPosition + (int) this.cut, this.yPosition,
                       this.xPosition + this.width - (int) this.cut, this.yPosition + this.height,
                       this.enabled ? new Color(0F, 0F, 0F, this.alpha / 255F).getRGB() :
                               new Color(0.5F, 0.5F, 0.5F, 0.5F).getRGB());
               break;
            case "rounded":
               RenderUtils.originalRoundedRect(this.xPosition, this.yPosition,
                       this.xPosition + this.width, this.yPosition + this.height, 2F,
                       this.enabled ? new Color(0F, 0F, 0F, this.alpha / 255F).getRGB() :
                               new Color(0.5F, 0.5F, 0.5F, 0.5F).getRGB());
               break;
            case "liquidbounce+":
               RenderUtils.drawRoundedRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 2.4F, new Color(0, 0, 0, 150).getRGB());
               RenderUtils.customRounded(this.xPosition, this.yPosition, this.xPosition + 2.4F + moveX, this.yPosition + this.height, 2.4F, roundCorner, roundCorner, 2.4F, (this.enabled ? new Color(0, 111, 255) : new Color(71, 71, 71)).getRGB());
               break;
            case "line":
               this.hovered = (mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height);
               updatefade();
               Gui.drawRect(
                       this.xPosition + (int) this.cut,
                       this.yPosition,
                       this.xPosition + this.width - (int) this.cut,
                       this.yPosition + this.height,
                       this.enabled ? new Color(0F, 0F, 0F, this.alpha / 255F).getRGB() : new Color(0.5F, 0.5F, 0.5F, 0.5F).getRGB());
               RenderUtils.drawRect(
                       (this.xPosition + (int) this.cut),
                       (this.yPosition + this.height - 1),
                       (this.xPosition + this.width - (int) this.cut),
                       (this.yPosition + this.height),
                       new Color(255,255,255,255).getRGB());
               break;
         }

         if (hud.getGuiButtonStyle().get().equalsIgnoreCase("minecraft")) return;

         mc.getTextureManager().bindTexture(buttonTextures);
         mouseDragged(mc, mouseX, mouseY);

         if (!Objects.equals(hud.getGuiButtonStyle().get(), "Miren")) {

            AWTFontRenderer.Companion.setAssumeNonVolatile(true);

            fontRenderer.drawStringWithShadow(displayString,
                    (float) ((this.xPosition + this.width / 2) -
                            fontRenderer.getStringWidth(displayString) / 2),
                    this.yPosition + (this.height - 5) / 2F - 2, 14737632);

            AWTFontRenderer.Companion.setAssumeNonVolatile(false);
         }

         GlStateManager.resetColor();
      }
   }

   private void updatefade() {
      this.alpha += 25;
      if (this.alpha >= 120)
         this.alpha = 120;
   }
}
