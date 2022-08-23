package com.lying.variousoddities.mixin;

import javax.swing.text.JTextComponent.KeyBinding;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.capabilities.PlayerData.SoulCondition;
import com.lying.variousoddities.client.gui.GuiHandler;
import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(IngameGui.class)
public class IngameGuiMixin
{
	private static final int ICON_SIZE = 9;
	
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
		Player player = mc.player;
		if(player == null)
			return;
		
		PlayerData data = PlayerData.forPlayer(player);
		if(data == null || PlayerData.isPlayerNormalFunction(player))
			return;
		ci.cancel();
		Screen gui = (Screen)(Object)this;
		Component displayText = null;
		switch(data.getBodyCondition())
		{
			case DEAD:
				float progress = 1F - data.timeToRespawnable();
				mc.getProfiler().startSection("expBar");
					mc.getTextureManager().bindTexture(Screen.GUI_ICONS_LOCATION);
					int k = (int)(progress * 183.0F);
					int l = this.scaledHeight - 32 + 3;
					gui.blit(matrixStack, xPos, l, 0, 64, 182, 5);
					if (k > 0)
						gui.blit(matrixStack, xPos, l, 0, 69, k, 5);
				mc.getProfiler().endSection();
				
				if(progress >= 1F)
				{
					KeyBinding inv = mc.gameSettings.keyBindInventory;
					displayText = Component.translatable("gui."+Reference.ModInfo.MOD_ID+".dead_player_respawn", inv.func_238171_j_().getSerializedName().toUpperCase());
				}
				break;
			case UNCONSCIOUS:
				if(data.getSoulCondition() == SoulCondition.ALIVE)
					if(!LivingData.unconscious(player))
					{
						KeyBinding inv = mc.gameSettings.keyBindInventory;
						displayText = Component.translatable("gui."+Reference.ModInfo.MOD_ID+".unconscious_player.awaken", inv.func_238171_j_().getSerializedName().toUpperCase());
					}
					else
					{
						if(player.getActivePotionEffect(VOPotions.SLEEP) != null && player.getActivePotionEffect(VOPotions.SLEEP).getDuration() > 0)
							displayText = Component.translatable("gui."+Reference.ModInfo.MOD_ID+".unconscious_player.sleep");
						else
						{
							displayText = Component.translatable("gui."+Reference.ModInfo.MOD_ID+".unconscious_player.bludgeoning");
							float bludgeoning = LivingData.forEntity(player).getBludgeoning();
							int healthToWaking = (int)(bludgeoning - player.getHealth()) + 1;
							
							int totalHearts = (int)Math.ceil(healthToWaking * 0.5D);
							int heartsWidth = (totalHearts * ICON_SIZE) + (totalHearts - 1);
							int heartX = (scaledWidth / 2) - (heartsWidth / 2);
							int heartY = this.scaledHeight - 38 + getFontRenderer().FONT_HEIGHT + 1;
							
							Minecraft.getInstance().getTextureManager().bindTexture(GuiHandler.HUD_ICONS);
							for(int heart = healthToWaking; heart >= 0; --heart)
								if(heart%2 > 0)
								{
									int texX = heart > 1 ? 0 : ICON_SIZE;
									int texY = 9;
									this.blitIcon(matrixStack, heartX, heartY, texX, texY);
									heartX += ICON_SIZE + 1;
								}
							Minecraft.getInstance().getTextureManager().bindTexture(Screen.GUI_ICONS_LOCATION);
						}
					}
				break;
			default:
				break;
		}
		
		if(displayText != null)
		{
			String s = displayText.getSerializedName();
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
	
	private void blitIcon(MatrixStack matrixStack, int x, int y, int uOffset, int vOffset)
	{
		Screen.blit(matrixStack, x, y, 0, (float)uOffset, (float)vOffset, ICON_SIZE, ICON_SIZE, 256, 256);
	}
}
