package com.lying.variousoddities.client.gui;

import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

public class ScreenPetrified extends AbstractParalysisScreen
{
	public ScreenPetrified(PlayerEntity playerIn)
	{
		super(new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".petrified"), playerIn);
	}
	
	public boolean shouldClose()
	{
		return thePlayer.getActivePotionEffect(VOPotions.PETRIFIED) == null || thePlayer.getActivePotionEffect(VOPotions.PETRIFIED).getDuration() == 0;
	}
	
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderDirtBackground(0);
		drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 40, 16777215);
		drawCenteredString(matrixStack, this.font, new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".paralysed.permanent"), this.width / 2, 55, 16777215);
		
		for(int i = 0; i < this.buttons.size(); ++i){ this.buttons.get(i).render(matrixStack, mouseX, mouseY, partialTicks); }
	}
}
