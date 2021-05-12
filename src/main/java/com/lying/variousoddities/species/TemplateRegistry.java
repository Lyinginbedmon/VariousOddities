package com.lying.variousoddities.species;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

public class TemplateRegistry
{
	public static final List<Template> DEFAULT_TEMPLATES = Lists.newArrayList();
	public static final IForgeRegistry<Template> TEMPLATES = makeRegistry(new ResourceLocation(Reference.ModInfo.MOD_ID, "templates"), Template.class, Integer.MAX_VALUE >> 5);
	
	public static void init(){ }
	
	public static void onRegisterTemplates(RegistryEvent.Register<Template> event)
	{
		registerDefaultTemplates(event.getRegistry());
	}
	
	private static void registerDefaultTemplates(IForgeRegistry<Template> registry)
	{
		DEFAULT_TEMPLATES.forEach((species) -> { registry.register(species); });
	}
	
	static
	{
		/*
		 * Half-Dragon
		 * Lich
		 * Vampire
		 * Vampire Spawn
		 */
	}
	
	private static <T extends IForgeRegistryEntry<T>> IForgeRegistry<T> makeRegistry(ResourceLocation name, Class<T> type, int max)
	{
        return new RegistryBuilder<T>().setName(name).setType(type).setMaxID(max).create();
    }
}
