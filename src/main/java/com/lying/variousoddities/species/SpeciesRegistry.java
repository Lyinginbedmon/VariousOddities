package com.lying.variousoddities.species;

import java.util.Set;

import javax.annotation.Nullable;

import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.types.EnumCreatureType;
import com.lying.variousoddities.types.TypeHandler.DamageResist;
import com.lying.variousoddities.types.abilities.AbilityDamageReduction;
import com.lying.variousoddities.types.abilities.AbilityDamageResistance;
import com.lying.variousoddities.types.abilities.AbilityFastHealing;
import com.lying.variousoddities.types.abilities.AbilityHoldBreath;
import com.lying.variousoddities.types.abilities.AbilityNaturalArmour;
import com.lying.variousoddities.types.abilities.AbilityNaturalRegen;
import com.lying.variousoddities.types.abilities.AbilityResistance;
import com.lying.variousoddities.types.abilities.AbilityStatusEffect;
import com.lying.variousoddities.types.abilities.AbilityTeleportToHome;
import com.lying.variousoddities.types.abilities.AbilityTeleportToPos;
import com.lying.variousoddities.types.abilities.DamageType;

import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

public class SpeciesRegistry
{
	public static final IForgeRegistry<Species> SPECIES;
	
	public static final ResourceLocation SPECIES_ARCHFEY		= new ResourceLocation(Reference.ModInfo.MOD_ID, "archfey");
	public static final ResourceLocation SPECIES_LIZARDFOLK		= new ResourceLocation(Reference.ModInfo.MOD_ID, "lizardfolk");
	public static final ResourceLocation SPECIES_NECROPOLITAN	= new ResourceLocation(Reference.ModInfo.MOD_ID, "necropolitan");
	public static final ResourceLocation SPECIES_SKELETON		= new ResourceLocation(Reference.ModInfo.MOD_ID, "skeleton");
	public static final ResourceLocation SPECIES_ZOMBIE			= new ResourceLocation(Reference.ModInfo.MOD_ID, "zombie");
	
	@Nullable
	public static Species getSpecies(ResourceLocation nameIn)
	{
		if(SPECIES.containsKey(nameIn))
			return SPECIES.getValue(nameIn);
		else
			return null;
	}
	
	public static Set<ResourceLocation> speciesNames()
	{
		return SPECIES.getKeys();
	}
	
	public static void initDefaultSpecies()
	{
		/*
		 * Archfey
		 * Aasimar
		 * Blue Dragon
		 * Ender Dragon
		 * Green Dragon
		 * Red Dragon
		 * Goblin
		 * Half-Orc
		 * Kobold
		 * Lizardfolk
		 * Orc
		 * Skeleton
		 * Tiefling
		 * Troglodyte
		 * Zombie
		 */
		
		addSpecies(SPECIES_ARCHFEY, new Species()
				.setPlayerSelect(false)
				.addType(EnumCreatureType.FEY, EnumCreatureType.HOLY)
				.addAbility(new AbilityNaturalArmour(5D))
				.addAbility(new AbilityDamageReduction(10, DamageType.SILVER))
				.addAbility(new AbilityFastHealing(3F))
				.addAbility(new AbilityResistance(5, DamageType.MAGIC))
				.addAbility(new AbilityTeleportToPos(16D))
				.addAbility(new AbilityTeleportToHome()));
		addSpecies(SPECIES_LIZARDFOLK, new Species()
				.addType(EnumCreatureType.HUMANOID, EnumCreatureType.REPTILE)
				.addAbility(new AbilityNaturalArmour(5D))
				.addAbility(new AbilityHoldBreath()));
		addSpecies(SPECIES_NECROPOLITAN, new Species()
				.addType(EnumCreatureType.UNDEAD)
				.addAbility(new AbilityNaturalRegen()));
		addSpecies(SPECIES_SKELETON, new Species()
				.addType(EnumCreatureType.UNDEAD)
				.addAbility(new AbilityNaturalArmour(2D))
				.addAbility(new AbilityDamageResistance(DamageType.COLD, DamageResist.IMMUNE))
				.addAbility(new AbilityDamageReduction(3))
				.addAbility(new AbilityStatusEffect(new EffectInstance(Effects.SPEED, 0, 0, true, false))));
		addSpecies(SPECIES_ZOMBIE, new Species()
				.addType(EnumCreatureType.UNDEAD)
				.addAbility(new AbilityNaturalArmour(2D))
				.addAbility(new AbilityDamageReduction(3))
				.addAbility(new AbilityStatusEffect(new EffectInstance(Effects.STRENGTH, 0, 0, true, false)))
				.addAbility(new AbilityStatusEffect(new EffectInstance(Effects.SLOWNESS, 0, 0, true, false))));
	}
	
	public static void initDefaultTemplates()
	{
		/*
		 * Half-Dragon
		 * Lich
		 * Vampire
		 * Vampire Spawn
		 */
	}
	
	private static void addSpecies(ResourceLocation nameIn, Species speciesIn)
	{
		speciesIn.setRegistryName(nameIn);
		SPECIES.registerAll(speciesIn);
	}
	
	private static <T extends IForgeRegistryEntry<T>> IForgeRegistry<T> makeRegistry(ResourceLocation name, Class<T> type, int max)
	{
        return new RegistryBuilder<T>().setName(name).setType(type).setMaxID(max).create();
    }
	
	static
	{
		SPECIES = makeRegistry(new ResourceLocation(Reference.ModInfo.MOD_ID, "species"), Species.class, Integer.MAX_VALUE >> 5);
	}
}
