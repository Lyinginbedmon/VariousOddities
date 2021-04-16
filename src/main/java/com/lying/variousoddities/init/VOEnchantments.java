package com.lying.variousoddities.init;

import java.util.ArrayList;
import java.util.List;

import com.lying.variousoddities.enchantment.EnchantmentSilversheen;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class VOEnchantments
{
	private static final List<Enchantment> ENCHANTMENTS = new ArrayList<>();
	
	public static final Enchantment SILVERSHEEN	= register("silversheen", new EnchantmentSilversheen());
	
	private static Enchantment register(String name, Enchantment ench)
	{
		ench.setRegistryName(Reference.ModInfo.MOD_ID, name);
		ENCHANTMENTS.add(ench);
		return ench;
	}
	
	@SubscribeEvent
	public static void registerEnchantmentsEvent(Register<Enchantment> event)
	{
		IForgeRegistry<Enchantment> registry = event.getRegistry();
		for(Enchantment ench : ENCHANTMENTS)
			registry.register(ench);
	}
}
