package com.lying.variousoddities.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

public class ItemOddEgg extends SpawnEggItem
{
	private final EntityType<? extends Mob> entity;
	
	@SuppressWarnings("deprecation")
	public ItemOddEgg(EntityType<? extends Mob> typeIn, int primaryColorIn, int secondaryColorIn, Properties builder)
	{
		super(typeIn, primaryColorIn, secondaryColorIn, builder.tab(VOItemGroup.EGGS));
		this.entity = typeIn;
	}
	
	public Component getName(ItemStack stack)
	{
		Component entityName = entity.getDescription();
		return Component.translatable("item.varodd.spawn_egg", entityName);
	}
}
