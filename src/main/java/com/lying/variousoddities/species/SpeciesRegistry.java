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
import com.lying.variousoddities.species.abilities.AbilityDamageCap;
import com.lying.variousoddities.species.abilities.AbilityDamageReduction;
import com.lying.variousoddities.species.abilities.AbilityDamageResistance;
import com.lying.variousoddities.species.abilities.AbilityDarkvision;
import com.lying.variousoddities.species.abilities.AbilityExplode;
import com.lying.variousoddities.species.abilities.AbilityFastHealing;
import com.lying.variousoddities.species.abilities.AbilityFlight;
import com.lying.variousoddities.species.abilities.AbilityFlight.Grade;
import com.lying.variousoddities.species.abilities.AbilityHoldBreath;
import com.lying.variousoddities.species.abilities.AbilityLightSensitivity;
import com.lying.variousoddities.species.abilities.AbilityModifierCon;
import com.lying.variousoddities.species.abilities.AbilityModifierStr;
import com.lying.variousoddities.species.abilities.AbilityNaturalArmour;
import com.lying.variousoddities.species.abilities.AbilityResistance;
import com.lying.variousoddities.species.abilities.AbilitySize;
import com.lying.variousoddities.species.abilities.AbilitySize.Size;
import com.lying.variousoddities.species.abilities.AbilityStability;
import com.lying.variousoddities.species.abilities.AbilityTeleportToHome;
import com.lying.variousoddities.species.abilities.AbilityTeleportToPos;
import com.lying.variousoddities.species.abilities.DamageType;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.species.types.TypeHandler.DamageResist;

import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class SpeciesRegistry extends JsonReloadListener
{
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	
	private static final List<Species> DEFAULT_SPECIES = Lists.newArrayList();
	
	public static final ResourceLocation SPECIES_AASIMAR		= new ResourceLocation(Reference.ModInfo.MOD_ID, "aasimar");
	public static final ResourceLocation SPECIES_ARCHFEY		= new ResourceLocation(Reference.ModInfo.MOD_ID, "archfey");
	public static final ResourceLocation SPECIES_CREEPER		= new ResourceLocation(Reference.ModInfo.MOD_ID, "creeper");
	public static final ResourceLocation SPECIES_DWARF			= new ResourceLocation(Reference.ModInfo.MOD_ID, "dwarf");
	public static final ResourceLocation SPECIES_DRAGON_GREEN	= new ResourceLocation(Reference.ModInfo.MOD_ID, "green_dragon");
	public static final ResourceLocation SPECIES_GNOME			= new ResourceLocation(Reference.ModInfo.MOD_ID, "gnome");
	public static final ResourceLocation SPECIES_GOBLIN			= new ResourceLocation(Reference.ModInfo.MOD_ID, "goblin");
	public static final ResourceLocation SPECIES_HALFLING		= new ResourceLocation(Reference.ModInfo.MOD_ID, "halfling");
	public static final ResourceLocation SPECIES_HALF_ORC		= new ResourceLocation(Reference.ModInfo.MOD_ID, "half_orc");
	public static final ResourceLocation SPECIES_KOBOLD			= new ResourceLocation(Reference.ModInfo.MOD_ID, "kobold");
	public static final ResourceLocation SPECIES_LIZARDFOLK		= new ResourceLocation(Reference.ModInfo.MOD_ID, "lizardfolk");
	public static final ResourceLocation SPECIES_ORC			= new ResourceLocation(Reference.ModInfo.MOD_ID, "orc");
	public static final ResourceLocation SPECIES_TIEFLING		= new ResourceLocation(Reference.ModInfo.MOD_ID, "tiefling");
	public static final ResourceLocation SPECIES_TROGLODYTE		= new ResourceLocation(Reference.ModInfo.MOD_ID, "troglodyte");
	
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
	
	private static void addSpecies(Species speciesIn)
	{
		DEFAULT_SPECIES.add(speciesIn);
	}
	
	static
	{
		/*
		 * Creeper
		 * Dragon, blue
		 * Dragon, ender
		 * Dragon, red
		 * Enderman
		 * Tiefling (3.5E)
		 * Tiefling, Lesser (5E)
		 */
		
		addSpecies(new Species(SPECIES_AASIMAR)
				.setDisplayName(new TranslationTextComponent("species."+Reference.ModInfo.MOD_ID+".aasimar"))
				.setPower(1)
				.addType(EnumCreatureType.OUTSIDER, EnumCreatureType.NATIVE)
				.addAbility(new AbilityDarkvision())
				.addAbility(new AbilityResistance(2, DamageType.ACID))
				.addAbility(new AbilityResistance(2, DamageType.COLD))
				.addAbility(new AbilityResistance(2, DamageType.LIGHTNING)));
		addSpecies(new Species(SPECIES_ARCHFEY)
				.setDisplayName(new TranslationTextComponent("species."+Reference.ModInfo.MOD_ID+".archfey"))
				.setPower(10)
				.addType(EnumCreatureType.FEY, EnumCreatureType.HOLY)
				.addAbility(new AbilityNaturalArmour(5D))
				.addAbility(new AbilityDamageReduction(10, DamageType.SILVER))
				.addAbility(new AbilityFastHealing(3F))
				.addAbility(new AbilityResistance(5, DamageType.MAGIC))
				.addAbility(new AbilityDamageCap(20F, 10F))
				.addAbility(new AbilityTeleportToPos(16D))
				.addAbility(new AbilityTeleportToHome()));
		addSpecies(new Species(SPECIES_CREEPER)
				.setDisplayName(new TranslationTextComponent("entity.minecraft.creeper"))
				.addType(EnumCreatureType.PLANT)
				.addAbility(new AbilityModifierStr(-4D))
				.addAbility(new AbilityExplode()));
		addSpecies(new Species(SPECIES_DWARF)
				.setDisplayName(new TranslationTextComponent("species."+Reference.ModInfo.MOD_ID+".dwarf"))
				.addType(EnumCreatureType.HUMANOID)
				.addAbility(new AbilitySize(Size.MEDIUM, 0.2F))
				.addAbility(new AbilityModifierCon(2D))
				.addAbility(new AbilityDarkvision())
				.addAbility(new AbilityStability()));
		addSpecies(new Species(SPECIES_DRAGON_GREEN)
				.setDisplayName(new TranslationTextComponent("species."+Reference.ModInfo.MOD_ID+".dragon_green"))
				.setPower(5)
				.addType(EnumCreatureType.DRAGON, EnumCreatureType.AIR)
				.addAbility(new AbilitySize(Size.MEDIUM, 0.7F))
				.addAbility(new AbilityModifierStr(3D))
				.addAbility(new AbilityModifierCon(10D))
				.addAbility(new AbilityNaturalArmour(7D))
				.addAbility(new AbilityDamageResistance(DamageType.ACID, DamageResist.IMMUNE))
				.addAbility(new AbilityBreatheWater())
				.addAbility(new AbilityFlight(Grade.POOR))
				.addAbility(new AbilityBreathWeapon(DamageType.ACID, BreathType.CONE, 9D, 4F, 24F).setParticle(ParticleTypes.DRAGON_BREATH)));
		addSpecies(new Species(SPECIES_GOBLIN)
				.setDisplayName(new TranslationTextComponent("entity."+Reference.ModInfo.MOD_ID+".goblin"))
				.addType(EnumCreatureType.HUMANOID, EnumCreatureType.GOBLIN)
				.addAbility(new AbilitySize(Size.SMALL))
				.addAbility(new AbilityModifierStr(-1D))
				.addAbility(new AbilityDarkvision()));
		addSpecies(new Species(SPECIES_GNOME)
				.setDisplayName(new TranslationTextComponent("species."+Reference.ModInfo.MOD_ID+".gnome"))
				.addType(EnumCreatureType.HUMANOID)
				.addAbility(new AbilityModifierCon(2D))
				.addAbility(new AbilitySize(Size.SMALL, 0.4F)));
		addSpecies(new Species(SPECIES_HALFLING)
				.setDisplayName(new TranslationTextComponent("species."+Reference.ModInfo.MOD_ID+".halfling"))
				.addType(EnumCreatureType.HUMANOID)
				.addAbility(new AbilitySize(Size.SMALL, 0.1F)));
		addSpecies(new Species(SPECIES_HALF_ORC)
				.setDisplayName(new TranslationTextComponent("species."+Reference.ModInfo.MOD_ID+".half_orc"))
				.addType(EnumCreatureType.HUMANOID)
				.addAbility(new AbilitySize(Size.MEDIUM, 0.6F))
				.addAbility(new AbilityModifierStr(1D))
				.addAbility(new AbilityDarkvision()));
		addSpecies(new Species(SPECIES_KOBOLD)
				.setDisplayName(new TranslationTextComponent("entity."+Reference.ModInfo.MOD_ID+".kobold"))
				.addType(EnumCreatureType.HUMANOID, EnumCreatureType.REPTILE)
				.addAbility(new AbilitySize(Size.SMALL))
				.addAbility(new AbilityModifierStr(-2D))
				.addAbility(new AbilityModifierCon(-2D))
				.addAbility(new AbilityDarkvision())
				.addAbility(new AbilityNaturalArmour(1D))
				.addAbility(new AbilityLightSensitivity()));
		addSpecies(new Species(SPECIES_LIZARDFOLK)
				.setDisplayName(new TranslationTextComponent("species."+Reference.ModInfo.MOD_ID+".lizardfolk"))
				.setPower(1)
				.addType(EnumCreatureType.HUMANOID, EnumCreatureType.REPTILE)
				.addAbility(new AbilityModifierStr(1D))
				.addAbility(new AbilityModifierCon(2D))
				.addAbility(new AbilityNaturalArmour(5D))
				.addAbility(new AbilityHoldBreath()));
		addSpecies(new Species(SPECIES_ORC)
				.setDisplayName(new TranslationTextComponent("species."+Reference.ModInfo.MOD_ID+".orc"))
				.addType(EnumCreatureType.HUMANOID)
				.addAbility(new AbilitySize(Size.MEDIUM, 0.7F))
				.addAbility(new AbilityModifierStr(2D))
				.addAbility(new AbilityDarkvision())
				.addAbility(new AbilityLightSensitivity()));
		addSpecies(new Species(SPECIES_TIEFLING)
				.setDisplayName(new TranslationTextComponent("species."+Reference.ModInfo.MOD_ID+".tiefling"))
				.setPower(1)
				.addType(EnumCreatureType.OUTSIDER, EnumCreatureType.NATIVE)
				.addAbility(new AbilityDarkvision())
				.addAbility(new AbilityResistance(2, DamageType.COLD))
				.addAbility(new AbilityResistance(2, DamageType.FIRE))
				.addAbility(new AbilityResistance(2, DamageType.LIGHTNING)));
		addSpecies(new Species(SPECIES_TROGLODYTE)
				.setDisplayName(new TranslationTextComponent("species."+Reference.ModInfo.MOD_ID+".troglodyte"))
				.setPower(2)
				.addType(EnumCreatureType.HUMANOID, EnumCreatureType.REPTILE)
				.addAbility(new AbilityModifierCon(4D))
				.addAbility(new AbilityDarkvision())
				.addAbility(new AbilityNaturalArmour(6D)));
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
