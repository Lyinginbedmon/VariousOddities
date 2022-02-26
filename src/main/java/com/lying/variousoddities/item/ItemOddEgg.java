package com.lying.variousoddities.item;

import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ItemOddEgg extends SpawnEggItem
{
	private final EntityType<?> entity;
	
	@SuppressWarnings("deprecation")
	public ItemOddEgg(EntityType<?> typeIn, int primaryColorIn, int secondaryColorIn, Properties builder)
	{
		super(typeIn, primaryColorIn, secondaryColorIn, builder.group(VOItemGroup.EGGS));
		this.entity = typeIn;
	}
	
	public ITextComponent getDisplayName(ItemStack stack)
	{
		ITextComponent entityName = entity.getName();
		return new TranslationTextComponent("item.varodd.spawn_egg", entityName);
	}
}
