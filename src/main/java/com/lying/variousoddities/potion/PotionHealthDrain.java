package com.lying.variousoddities.potion;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.world.item.ItemStack;

public class PotionHealthDrain extends PotionHealthDamage
{
	private static final UUID DRAIN_UUID = UUID.fromString("53dff69b-ce95-47f4-b891-d1279208b093");
	
	public PotionHealthDrain()
	{
		super(DRAIN_UUID, 3146242);
	}
	
	public List<ItemStack> getCurativeItems(){ return Lists.newArrayList(); }
}
