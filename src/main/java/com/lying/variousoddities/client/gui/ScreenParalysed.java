package com.lying.variousoddities.client.gui;

import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class ScreenParalysed extends AbstractParalysisScreen
{
	public ScreenParalysed(Player playerIn)
	{
		super(Component.translatable("gui."+Reference.ModInfo.MOD_ID+".paralysed"), playerIn);
	}
	
	public int ticksToDisplay()
	{
		MobEffectInstance paralysis = thePlayer.getEffect(VOPotions.PARALYSIS);
		if(paralysis == null)
			return 0;
		else if(paralysis.isNoCounter())
			return -1;
		
		return paralysis.getDuration();
	}
	
	public boolean shouldClose()
	{
		return thePlayer.getEffect(VOPotions.PARALYSIS) == null || thePlayer.getEffect(VOPotions.PARALYSIS).getDuration() == 0;
	}
	
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		
		Component duration;
		if(ticksToDisplay() >= 0)
			duration = Component.translatable("gui."+Reference.ModInfo.MOD_ID+".paralysed.temporary", StringUtil.ticksToElapsedTime(Mth.floor((float)ticksToDisplay())));
		else
			duration = Component.translatable("gui."+Reference.ModInfo.MOD_ID+".paralysed.permanent");
		
		drawCenteredString(matrixStack, this.font, duration, this.width / 2, 55, 16777215);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
}
