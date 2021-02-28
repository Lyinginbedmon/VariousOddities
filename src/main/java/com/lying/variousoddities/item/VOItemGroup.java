package com.lying.variousoddities.item;

import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.init.VOItems;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public abstract class VOItemGroup extends ItemGroup
{
	public static final ItemGroup EGGS = new VOItemGroup("eggs")
	{
		public ItemStack createIcon(){ return VOEntities.SPAWN_EGGS.get(VOEntities.KOBOLD).getDefaultInstance(); }
	};
	public static final ItemGroup BLOCKS = new VOItemGroup("blocks")
	{
		public ItemStack createIcon(){ return VOItems.EGG_KOBOLD.getDefaultInstance(); }
	};
	public static final ItemGroup LOOT = new VOItemGroup("loot")
	{
		public ItemStack createIcon(){ return VOItems.SCALE_KOBOLD.getDefaultInstance(); }
	};
	
	public VOItemGroup(String labelIn)
	{
		super(Reference.ModInfo.MOD_ID+"."+labelIn);
	}
	
	public abstract ItemStack createIcon();
}
