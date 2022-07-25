package com.lying.variousoddities.init;

import java.util.ArrayList;
import java.util.List;

import com.lying.variousoddities.enchantment.EnchantmentSilversheen;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

public class VOEnchantments
{
	private static final List<Enchantment> ENCHANTMENTS = new ArrayList<>();
	
	public static final Enchantment SILVERSHEEN	= register("silversheen", new EnchantmentSilversheen());
	
	private static Enchantment register(String name, Enchantment ench)
	{
		ForgeRegistries.ENCHANTMENTS.register(Reference.ModInfo.MOD_PREFIX+name, ench);
		ENCHANTMENTS.add(ench);
		return ench;
	}
	
	public void init() { }
}
