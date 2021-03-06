package com.lying.variousoddities.init;

import java.util.HashMap;
import java.util.Map;

import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.Species;
import com.lying.variousoddities.species.Template;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.templates.TemplateOperation;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

public class VORegistries
{
	private static final ResourceLocation ABILITY_REG = new ResourceLocation(Reference.ModInfo.MOD_ID, "abilities");
	private static final ResourceLocation OPERATIONS_REG = new ResourceLocation(Reference.ModInfo.MOD_ID, "template_operations");
	
	public static final IForgeRegistry<Ability.Builder> ABILITIES;
	public static final IForgeRegistry<TemplateOperation.Builder> OPERATIONS;
	
	public static final Map<ResourceLocation, Species> SPECIES = new HashMap<>();
	public static final Map<ResourceLocation, Template> TEMPLATES = new HashMap<>();
	
	static
	{
		ABILITIES = makeRegistry(ABILITY_REG, Ability.Builder.class, Integer.MAX_VALUE >> 5);
		OPERATIONS = makeRegistry(OPERATIONS_REG, TemplateOperation.Builder.class, Integer.MAX_VALUE >> 5);
	}
	
	public static void init(){ }
	
	private static <T extends IForgeRegistryEntry<T>> IForgeRegistry<T> makeRegistry(ResourceLocation name, Class<T> type, int max)
	{
        return new RegistryBuilder<T>().setName(name).setType(type).setMaxID(max).create();
    }
}
