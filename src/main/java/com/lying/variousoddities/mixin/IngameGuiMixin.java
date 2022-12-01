package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.capabilities.PlayerData.SoulCondition;
import com.lying.variousoddities.client.gui.GuiHandler;
import com.lying.variousoddities.init.VOMobEffects;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(Gui.class)
public class IngameGuiMixin
{
	private static final int ICON_SIZE = 9;
	
	@Shadow
	protected int screenHeight;
	@Shadow
	protected int screenWidth;
	
	@Shadow
	public Font getFont(){ return null; }
	
	@Inject(method = "renderExperienceBar(Lcom/mojang/blaze3d/matrix/PoseStack;I)V", at = @At("HEAD"), cancellable = true)
	public void renderExperienceBar(PoseStack matrixStack, int xPos, final CallbackInfo ci)
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
		KeyMapping inv = mc.options.keyInventory;
		switch(data.getBodyCondition())
		{
			case DEAD:
				float progress = 1F - data.timeToRespawnable();
				mc.getProfiler().push("expBar");
					RenderSystem.setShaderTexture(0, Screen.GUI_ICONS_LOCATION);
					int k = (int)(progress * 183.0F);
					int l = this.screenHeight - 32 + 3;
					gui.blit(matrixStack, xPos, l, 0, 64, 182, 5);
					if (k > 0)
						gui.blit(matrixStack, xPos, l, 0, 69, k, 5);
				mc.getProfiler().pop();
				
				if(progress >= 1F)
					displayText = Component.translatable("gui."+Reference.ModInfo.MOD_ID+".dead_player_respawn", inv.getTranslatedKeyMessage().getString().toUpperCase());
				break;
			case UNCONSCIOUS:
				if(data.getSoulCondition() == SoulCondition.ALIVE)
					if(!LivingData.unconscious(player))
						displayText = Component.translatable("gui."+Reference.ModInfo.MOD_ID+".unconscious_player.awaken", inv.getTranslatedKeyMessage().getString().toUpperCase());
					else
					{
						if(player.getEffect(VOMobEffects.SLEEP.get()) != null && player.getEffect(VOMobEffects.SLEEP.get()).getDuration() > 0)
							displayText = Component.translatable("gui."+Reference.ModInfo.MOD_ID+".unconscious_player.sleep");
						else
						{
							displayText = Component.translatable("gui."+Reference.ModInfo.MOD_ID+".unconscious_player.bludgeoning");
							float bludgeoning = LivingData.forEntity(player).getBludgeoning();
							int healthToWaking = (int)(bludgeoning - player.getHealth()) + 1;
							
							int totalHearts = (int)Math.ceil(healthToWaking * 0.5D);
							int heartsWidth = (totalHearts * ICON_SIZE) + (totalHearts - 1);
							int heartX = (screenWidth / 2) - (heartsWidth / 2);
							int heartY = this.screenHeight - 38 + getFont().lineHeight + 1;
							
							RenderSystem.setShaderTexture(0, GuiHandler.HUD_ICONS);
							for(int heart = healthToWaking; heart >= 0; --heart)
								if(heart%2 > 0)
								{
									int texX = heart > 1 ? 0 : ICON_SIZE;
									int texY = 9;
									this.blitIcon(matrixStack, heartX, heartY, texX, texY);
									heartX += ICON_SIZE + 1;
								}
							RenderSystem.setShaderTexture(0, Screen.GUI_ICONS_LOCATION);
						}
					}
				break;
			default:
				break;
		}
		
		if(displayText != null)
		{
			String s = displayText.getString();
			int textX = (screenWidth - getFont().width(s)) / 2;
			int textY = this.screenHeight - 31 - 7;
			Font fontRenderer = getFont();
			fontRenderer.draw(matrixStack, s, (float)(textX + 1), (float)textY, 0);
			fontRenderer.draw(matrixStack, s, (float)(textX - 1), (float)textY, 0);
			fontRenderer.draw(matrixStack, s, (float)textX, (float)(textY + 1), 0);
			fontRenderer.draw(matrixStack, s, (float)textX, (float)(textY - 1), 0);
			fontRenderer.draw(matrixStack, s, (float)textX, (float)textY, -1);
		}
	}
	
	private void blitIcon(PoseStack matrixStack, int x, int y, int uOffset, int vOffset)
	{
		Screen.blit(matrixStack, x, y, 0, (float)uOffset, (float)vOffset, ICON_SIZE, ICON_SIZE, 256, 256);
	}
}
