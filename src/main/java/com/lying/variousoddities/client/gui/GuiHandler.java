package com.lying.variousoddities.client.gui;

import java.util.Map;

import com.lying.variousoddities.capabilities.Abilities;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.types.abilities.Ability;
import com.lying.variousoddities.types.abilities.AbilityRegistry;
import com.lying.variousoddities.types.abilities.ActivatedAbility;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

@OnlyIn(Dist.CLIENT)
public class GuiHandler
{
	public static Minecraft mc;
	public static IProfiler profiler;
	public static PlayerEntity player;
	
	public static void onGameOverlayPost(RenderGameOverlayEvent.Post event)
	{
		mc = Minecraft.getInstance();
		profiler = mc.getProfiler();
		
		if(event.getType() == ElementType.ALL)
		{
			MatrixStack matrix = event.getMatrixStack();
			float partialTicks = event.getPartialTicks();
			
			profiler.startSection("varodd-hud");
				player = Minecraft.getInstance().player;
				if(player != null)
				{
					if(!player.isSpectator() && player.isAlive())
						drawFavouritedAbilities(matrix, partialTicks);
				}
			profiler.endSection();
		}
	}
	
	private static void drawFavouritedAbilities(MatrixStack matrix, float partialTicks)
	{
		profiler.startSection("abilities");
		
		Map<ResourceLocation, Ability> abilityMap = AbilityRegistry.getCreatureAbilities(player);
		Abilities abilities = LivingData.forEntity(player).getAbilities();
		
		float posX = 5F;
		float posY = 5F;
		for(int i=0; i<5; i++)
		{
			ResourceLocation mapName = abilities.getFavourite(i);
			if(mapName != null)
			{
				ActivatedAbility ability = (ActivatedAbility)abilityMap.get(mapName);
				boolean canTrigger = ability.canTrigger(player);
				mc.fontRenderer.drawString(matrix, ability.translatedName().getString(), posX + (canTrigger ? 5F : 0F), posY, canTrigger ? -1 : 0);
			}
			posY += 10F;
		}
		
		profiler.endSection();
	}
}
