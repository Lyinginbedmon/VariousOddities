package com.lying.variousoddities.item;

import java.util.List;

import javax.annotation.Nullable;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemSap extends VOItem implements IBludgeoningItem
{
	public ItemSap(Properties properties)
	{
		super(properties);
	}
	
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
	{
		tooltip.add(Component.translatable("info."+Reference.ModInfo.MOD_ID+".bludgeoning_item"));
	}
}
