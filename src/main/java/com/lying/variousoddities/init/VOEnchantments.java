package com.lying.variousoddities.init;

import com.lying.variousoddities.enchantment.EnchantmentSilversheen;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class VOEnchantments
{
	public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Reference.ModInfo.MOD_ID);
	
	public static final Enchantment SILVERSHEEN	= register("silversheen", new EnchantmentSilversheen());
	
	private static Enchantment register(String name, Enchantment ench)
	{
		return ENCHANTMENTS.register(name, () -> ench).get();
	}
	
	public static void init() { }
}
