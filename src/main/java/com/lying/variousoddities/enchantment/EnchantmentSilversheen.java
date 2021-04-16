package com.lying.variousoddities.enchantment;

import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

public class EnchantmentSilversheen extends TemporaryEnchantment
{
	public EnchantmentSilversheen()
	{
		super(Rarity.COMMON, EnchantmentType.WEAPON, new EquipmentSlotType[] { EquipmentSlotType.MAINHAND });
	}
	
	public boolean canApplyAtEnchantingTable(ItemStack stack){ return false; }
	
	public boolean canApply(ItemStack stack){ return this.type.canEnchantItem(stack.getItem()); }
	
	public boolean isAllowedOnBooks(){ return false; }
	
	public int getDuration(){ return 60 * 60; }
}
