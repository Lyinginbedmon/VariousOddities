package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.capabilities.PlayerData.SoulCondition;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(IngameGui.class)
public class IngameGuiMixin
{
	@Shadow
	protected int scaledHeight;
	@Shadow
	protected int scaledWidth;
	
	@Shadow
	public FontRenderer getFontRenderer(){ return null; }
	
	@Inject(method = "func_238454_b_(Lcom/mojang/blaze3d/matrix/MatrixStack;I)V", at = @At("HEAD"), cancellable = true)
	public void func_238454_b_(MatrixStack matrixStack, int xPos, final CallbackInfo ci)
	{
		Minecraft mc = Minecraft.getInstance();
		PlayerEntity player = mc.player;
		if(player == null)
			return;
		
		PlayerData data = PlayerData.forPlayer(player);
		if(data == null || PlayerData.isPlayerNormalFunction(player))
			return;
		ci.cancel();
		
		AbstractGui gui = (AbstractGui)(Object)this;
		ITextComponent displayText = null;
		switch(data.getBodyCondition())
		{
			case DEAD:
				float progress = 1F - data.timeToRespawnable();
				mc.getProfiler().startSection("expBar");
					mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
					int k = (int)(progress * 183.0F);
					int l = this.scaledHeight - 32 + 3;
					gui.blit(matrixStack, xPos, l, 0, 64, 182, 5);
					if (k > 0)
						gui.blit(matrixStack, xPos, l, 0, 69, k, 5);
				mc.getProfiler().endSection();
				
				if(progress >= 1F)
				{
					KeyBinding inv = mc.gameSettings.keyBindInventory;
					displayText = new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".dead_player_respawn", inv.func_238171_j_().getString().toUpperCase());
				}
			case UNCONSCIOUS:
				if(!LivingData.forEntity(player).isUnconscious() && data.getSoulCondition() == SoulCondition.ALIVE)
				{
					KeyBinding inv = mc.gameSettings.keyBindInventory;
					displayText = new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".unconscious_player_awaken", inv.func_238171_j_().getString().toUpperCase());
				}
			default:
				;
		}
		
		if(displayText != null)
		{
			String s = displayText.getString();
			int textX = (scaledWidth - getFontRenderer().getStringWidth(s)) / 2;
			int textY = this.scaledHeight - 31 - 7;
			FontRenderer fontRenderer = getFontRenderer();
			fontRenderer.drawString(matrixStack, s, (float)(textX + 1), (float)textY, 0);
			fontRenderer.drawString(matrixStack, s, (float)(textX - 1), (float)textY, 0);
			fontRenderer.drawString(matrixStack, s, (float)textX, (float)(textY + 1), 0);
			fontRenderer.drawString(matrixStack, s, (float)textX, (float)(textY - 1), 0);
			fontRenderer.drawString(matrixStack, s, (float)textX, (float)textY, -1);
		}
	}
}
