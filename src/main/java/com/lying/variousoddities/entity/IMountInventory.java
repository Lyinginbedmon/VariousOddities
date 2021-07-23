package com.lying.variousoddities.entity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IMountInventory
{
	@OnlyIn(Dist.CLIENT)
	public void openGui(PlayerEntity playerIn);
}
