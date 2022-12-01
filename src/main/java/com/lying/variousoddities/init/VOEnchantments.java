package com.lying.variousoddities.init;

import com.lying.variousoddities.enchantment.EnchantmentSilversheen;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class VOEnchantments
{
	public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Reference.ModInfo.MOD_ID);
	
	public static final RegistryObject<EnchantmentSilversheen> SILVERSHEEN	= ENCHANTMENTS.register("silversheen", () -> new EnchantmentSilversheen());
	
	public static void init() { }
}
