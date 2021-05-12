package com.lying.variousoddities.init;

import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.Species;
import com.lying.variousoddities.species.abilities.Ability;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

public class VORegistries
{
	private static final ResourceLocation ABILITY_REG = new ResourceLocation(Reference.ModInfo.MOD_ID, "abilities"); 
	private static final ResourceLocation SPECIES_REG = new ResourceLocation(Reference.ModInfo.MOD_ID, "species"); 
	
	public static final IForgeRegistry<Ability.Builder> ABILITIES;
	public static final IForgeRegistry<Species> SPECIES;
	
	static
	{
		ABILITIES = makeRegistry(ABILITY_REG, Ability.Builder.class, Integer.MAX_VALUE >> 5);
		SPECIES = makeRegistry(SPECIES_REG, Species.class, Integer.MAX_VALUE >> 5);
	}
	
	public static void init(){ }
	
	private static <T extends IForgeRegistryEntry<T>> IForgeRegistry<T> makeRegistry(ResourceLocation name, Class<T> type, int max)
	{
        return new RegistryBuilder<T>().setName(name).setType(type).setMaxID(max).create();
    }
}
