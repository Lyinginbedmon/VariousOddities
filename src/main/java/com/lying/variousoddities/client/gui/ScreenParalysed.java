package com.lying.variousoddities.client.gui;

import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ScreenParalysed extends AbstractParalysisScreen
{
	public ScreenParalysed(PlayerEntity playerIn)
	{
		super(new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".paralysed"), playerIn);
	}
	
	public int ticksToDisplay()
	{
		EffectInstance paralysis = thePlayer.getActivePotionEffect(VOPotions.PARALYSIS);
		if(paralysis == null)
			return 0;
		else if(paralysis.getIsPotionDurationMax())
			return -1;
		
		return paralysis.getDuration();
	}
	
	public boolean shouldClose()
	{
		return thePlayer.getActivePotionEffect(VOPotions.PARALYSIS) == null || thePlayer.getActivePotionEffect(VOPotions.PARALYSIS).getDuration() == 0;
	}
	
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		
		ITextComponent duration;
		if(ticksToDisplay() >= 0)
			duration = new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".paralysed.temporary", StringUtils.ticksToElapsedTime(MathHelper.floor((float)ticksToDisplay())));
		else
			duration = new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".paralysed.permanent");
		
		drawCenteredString(matrixStack, this.font, duration, this.width / 2, 55, 16777215);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
}
