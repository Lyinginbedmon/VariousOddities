package com.lying.variousoddities.client.gui.screen;

import com.lying.variousoddities.init.VOMobEffects;
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
		return thePlayer.getEffect(VOMobEffects.PETRIFIED.get()) == null || thePlayer.getEffect(VOMobEffects.PETRIFIED.get()).getDuration() == 0;
	}
	
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderDirtBackground(0);
		drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 40, 16777215);
		drawCenteredString(matrixStack, this.font, Component.translatable("gui."+Reference.ModInfo.MOD_ID+".paralysed.permanent"), this.width / 2, 55, 16777215);
		
		for(int i = 0; i < this.renderables.size(); ++i)
			this.renderables.get(i).render(matrixStack, mouseX, mouseY, partialTicks);
	}
}
