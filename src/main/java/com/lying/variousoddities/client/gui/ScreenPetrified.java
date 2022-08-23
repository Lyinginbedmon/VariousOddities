package com.lying.variousoddities.client.gui;

import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class ScreenPetrified extends AbstractParalysisScreen
{
	public ScreenPetrified(Player playerIn)
	{
		super(Component.translatable("gui."+Reference.ModInfo.MOD_ID+".petrified"), playerIn);
	}
	
	public boolean shouldClose()
	{
		return thePlayer.getEffect(VOPotions.PETRIFIED) == null || thePlayer.getEffect(VOPotions.PETRIFIED).getDuration() == 0;
	}
	
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderDirtBackground(0);
		drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 40, 16777215);
		drawCenteredString(matrixStack, this.font, Component.translatable("gui."+Reference.ModInfo.MOD_ID+".paralysed.permanent"), this.width / 2, 55, 16777215);
		
		for(int i = 0; i < this.buttons.size(); ++i)
			this.buttons.get(i).render(matrixStack, mouseX, mouseY, partialTicks);
	}
}
