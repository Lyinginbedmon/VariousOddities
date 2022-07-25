package com.lying.variousoddities.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class EnchantmentSilversheen extends TemporaryEnchantment
{
	public EnchantmentSilversheen()
	{
		super(Rarity.COMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
	}
	
	public boolean canApplyAtEnchantingTable(ItemStack stack){ return false; }
	
	public boolean canEnchant(ItemStack stack){ return this.category.canEnchant(stack.getItem()); }
	
	public boolean isAllowedOnBooks(){ return false; }
	
	public int getDuration(){ return 60 * 60; }
}
