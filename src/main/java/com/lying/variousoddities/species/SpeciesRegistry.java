package com.lying.variousoddities.species;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.Species.SpeciesInstance;
import com.lying.variousoddities.species.abilities.AbilityBreathWeapon;
import com.lying.variousoddities.species.abilities.AbilityBreathWeapon.BreathType;
import com.lying.variousoddities.species.abilities.AbilityBreatheWater;
import com.lying.variousoddities.species.abilities.AbilityDamageReduction;
import com.lying.variousoddities.species.abilities.AbilityDamageResistance;
import com.lying.variousoddities.species.abilities.AbilityFastHealing;
import com.lying.variousoddities.species.abilities.AbilityFlight;
import com.lying.variousoddities.species.abilities.AbilityFlight.Grade;
import com.lying.variousoddities.species.abilities.AbilityHoldBreath;
import com.lying.variousoddities.species.abilities.AbilityNaturalArmour;
import com.lying.variousoddities.species.abilities.AbilityNaturalRegen;
import com.lying.variousoddities.species.abilities.AbilityResistance;
import com.lying.variousoddities.species.abilities.AbilityStatusEffect;
import com.lying.variousoddities.species.abilities.AbilityTeleportToHome;
import com.lying.variousoddities.species.abilities.AbilityTeleportToPos;
import com.lying.variousoddities.species.abilities.DamageType;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.species.types.TypeHandler.DamageResist;

import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class SpeciesRegistry extends JsonReloadListener
{
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	
	private static final List<Species> DEFAULT_SPECIES = Lists.newArrayList();
	
	public static final ResourceLocation SPECIES_ARCHFEY		= new ResourceLocation(Reference.ModInfo.MOD_ID, "archfey");
	public static final ResourceLocation SPECIES_DRAGON_GREEN	= new ResourceLocation(Reference.ModInfo.MOD_ID, "green_dragon");
	public static final ResourceLocation SPECIES_LIZARDFOLK		= new ResourceLocation(Reference.ModInfo.MOD_ID, "lizardfolk");
	public static final ResourceLocation SPECIES_NECROPOLITAN	= new ResourceLocation(Reference.ModInfo.MOD_ID, "necropolitan");
	public static final ResourceLocation SPECIES_SKELETON		= new ResourceLocation(Reference.ModInfo.MOD_ID, "skeleton");
	public static final ResourceLocation SPECIES_ZOMBIE			= new ResourceLocation(Reference.ModInfo.MOD_ID, "zombie");
	
	private static SpeciesRegistry instance;
	
	public static SpeciesRegistry getInstance()
	{
		if(instance == null)
			instance = new SpeciesRegistry();
		return instance;
	}
	
	public SpeciesRegistry()
	{
		super(GSON, "species");
	}
	
	public static List<Species> getDefaultSpecies(){ return Lists.newArrayList(DEFAULT_SPECIES); }
	
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn)
	{
		VariousOddities.log.info("Attempting to load species from data, entries: "+objectIn.size());
		Map<ResourceLocation, Species> loaded = new HashMap<>();
		objectIn.forEach((name, json) -> {
            try
            {
                Species builder = Species.fromJson(json);
                if(builder != null)
                {
                	VariousOddities.log.info(" -Loaded: "+name.toString());
                	builder.setRegistryName(name);
                    loaded.put(name, builder);
                }
            }
            catch (IllegalArgumentException | JsonParseException e)
            {
                VariousOddities.log.error("Failed to load species {}: {}", name);
            }
            catch(Exception e)
            {
            	VariousOddities.log.error("Unrecognised error loading species {}", name);
            }
        });
		
		loaded.forEach((name,species) -> { VORegistries.SPECIES.put(name, species); });
		
		// If no species were found in the datapack, load the defaults
		if(loaded.isEmpty())
		{
			VariousOddities.log.warn("No species found, loading defaults");
			DEFAULT_SPECIES.forEach((species) -> { VORegistries.SPECIES.put(species.getRegistryName(), species); });
		}
	}
	
	static
	{
		/*
		 * Archfey
		 * Aasimar
		 * Creeper
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
		
		DEFAULT_SPECIES.add(new Species(SPECIES_ARCHFEY)
				.setPlayerSelect(false)
				.setPower(10)
				.addType(EnumCreatureType.FEY, EnumCreatureType.HOLY)
				.addAbility(new AbilityNaturalArmour(5D))
				.addAbility(new AbilityDamageReduction(10, DamageType.SILVER))
				.addAbility(new AbilityFastHealing(3F))
				.addAbility(new AbilityResistance(5, DamageType.MAGIC))
				.addAbility(new AbilityTeleportToPos(16D))
				.addAbility(new AbilityTeleportToHome()));
		DEFAULT_SPECIES.add(new Species(SPECIES_DRAGON_GREEN)
				.setPower(5)
				.addType(EnumCreatureType.DRAGON, EnumCreatureType.AIR)
				.addAbility(new AbilityNaturalArmour(7D))
				.addAbility(new AbilityDamageResistance(DamageType.ACID, DamageResist.IMMUNE))
				.addAbility(new AbilityBreatheWater())
				.addAbility(new AbilityFlight(Grade.POOR))
				.addAbility(new AbilityBreathWeapon(DamageType.ACID, BreathType.CONE, 9D, 4F, 24F).setParticle(ParticleTypes.DRAGON_BREATH)));
		DEFAULT_SPECIES.add(new Species(SPECIES_LIZARDFOLK)
				.setPower(1)
				.addType(EnumCreatureType.HUMANOID, EnumCreatureType.REPTILE)
				.addAbility(new AbilityNaturalArmour(5D))
				.addAbility(new AbilityHoldBreath()));
		DEFAULT_SPECIES.add(new Species(SPECIES_NECROPOLITAN)
				.setPower(1)
				.addType(EnumCreatureType.UNDEAD)
				.addAbility(new AbilityNaturalRegen()));
		DEFAULT_SPECIES.add(new Species(SPECIES_SKELETON)
				.setPlayerSelect(false)
				.addType(EnumCreatureType.UNDEAD)
				.addAbility(new AbilityNaturalArmour(2D))
				.addAbility(new AbilityDamageResistance(DamageType.COLD, DamageResist.IMMUNE))
				.addAbility(new AbilityDamageReduction(3))
				.addAbility(new AbilityStatusEffect(new EffectInstance(Effects.SPEED, 0, 0, true, false))));
		DEFAULT_SPECIES.add(new Species(SPECIES_ZOMBIE)
				.setPlayerSelect(false)
				.addAbility(new AbilityStatusEffect(new EffectInstance(Effects.STRENGTH, 0, 0, true, false)))
				.addAbility(new AbilityStatusEffect(new EffectInstance(Effects.SLOWNESS, 0, 0, true, false))));
	}
	
	@Nullable
	public static Species getSpecies(ResourceLocation nameIn)
	{
		if(VORegistries.SPECIES.containsKey(nameIn))
			return VORegistries.SPECIES.get(nameIn);
		else
			return null;
	}
	
	@Nullable
	public static SpeciesInstance instanceFromNBT(CompoundNBT compound)
	{
		ResourceLocation name = new ResourceLocation(compound.getString("Name"));
		Species species = getSpecies(name);
		if(species != null)
		{
			SpeciesInstance instance = species.createInstance();
			instance.readFromNBT(compound);
			return instance;
		}
		return null;
	}
}
