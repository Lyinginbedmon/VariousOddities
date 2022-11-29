package com.lying.variousoddities.item;

import com.lying.variousoddities.init.VOItems;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public abstract class VOItemGroup extends CreativeModeTab
{
	public static final CreativeModeTab EGGS = new VOItemGroup("eggs")
	{
		public ItemStack makeIcon(){ return VOItems.SPAWN_EGG_KOBOLD.getDefaultInstance(); }
	};
	public static final CreativeModeTab BLOCKS = new VOItemGroup("blocks")
	{
		public ItemStack makeIcon(){ return VOItems.EGG_KOBOLD.getDefaultInstance(); }
	};
	public static final CreativeModeTab LOOT = new VOItemGroup("loot")
	{
		public ItemStack makeIcon(){ return VOItems.SCALE_KOBOLD.getDefaultInstance(); }
	};
	
	public VOItemGroup(String labelIn)
	{
		super(Reference.ModInfo.MOD_ID+"."+labelIn);
	}
	
	public abstract ItemStack makeIcon();
}
