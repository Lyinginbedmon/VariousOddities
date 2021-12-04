package com.lying.variousoddities.item;

import java.util.List;

import javax.annotation.Nullable;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class ItemSap extends VOItem implements IBludgeoningItem
{
	public ItemSap(Properties properties)
	{
		super(properties);
	}
	
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
	{
		tooltip.add(new TranslationTextComponent("info."+Reference.ModInfo.MOD_ID+".bludgeoning_item"));
	}
}
