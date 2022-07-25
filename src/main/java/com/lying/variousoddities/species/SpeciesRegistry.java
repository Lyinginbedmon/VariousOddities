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
import com.lying.variousoddities.species.abilities.AbilityBlindsight;
import com.lying.variousoddities.species.abilities.AbilityBreathWeapon;
import com.lying.variousoddities.species.abilities.AbilityBreathWeapon.BreathType;
import com.lying.variousoddities.species.abilities.AbilityBreatheFluid;
import com.lying.variousoddities.species.abilities.AbilityClimb;
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
import com.lying.variousoddities.species.abilities.AbilityPoison;
import com.lying.variousoddities.species.abilities.AbilityResistance;
import com.lying.variousoddities.species.abilities.AbilityScent;
import com.lying.variousoddities.species.abilities.AbilitySize;
import com.lying.variousoddities.species.abilities.AbilitySize.Size;
import com.lying.variousoddities.species.abilities.AbilityStability;
import com.lying.variousoddities.species.abilities.AbilityStatusImmunity;
import com.lying.variousoddities.species.abilities.AbilitySwim;
import com.lying.variousoddities.species.abilities.AbilityTeleportToHome;
import com.lying.variousoddities.species.abilities.AbilityTeleportToPos;
import com.lying.variousoddities.species.abilities.AbilityTremorsense;
import com.lying.variousoddities.species.abilities.DamageType;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.species.types.TypeHandler.DamageResist;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SpeciesRegistry extends JsonReloadListener
{
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	
	private static final List<Species> DEFAULT_SPECIES = Lists.newArrayList();
	
	public static final ResourceLocation SPECIES_AASIMAR		= new ResourceLocation(Reference.ModInfo.MOD_ID, "aasimar");
	public static final ResourceLocation SPECIES_ARCHFEY		= new ResourceLocation(Reference.ModInfo.MOD_ID, "archfey");
	public static final ResourceLocation SPECIES_BAT			= new ResourceLocation("minecraft", "bat");
	public static final ResourceLocation SPECIES_CAT			= new ResourceLocation("minecraft", "cat");
	public static final ResourceLocation SPECIES_CAVE_SPIDER	= new ResourceLocation("minecraft", "cave_spider");
	public static final ResourceLocation SPECIES_CREEPER		= new ResourceLocation("minecraft", "creeper");
	public static final ResourceLocation SPECIES_DONKEY			= new ResourceLocation("minecraft", "donkey");
	public static final ResourceLocation SPECIES_DWARF			= new ResourceLocation(Reference.ModInfo.MOD_ID, "dwarf");
	public static final ResourceLocation SPECIES_DRAGON_GREEN	= new ResourceLocation(Reference.ModInfo.MOD_ID, "green_dragon");
	public static final ResourceLocation SPECIES_GIANT_RAT		= new ResourceLocation(Reference.ModInfo.MOD_ID, "giant_rat");
	public static final ResourceLocation SPECIES_GNOME			= new ResourceLocation(Reference.ModInfo.MOD_ID, "gnome");
	public static final ResourceLocation SPECIES_GOBLIN			= new ResourceLocation(Reference.ModInfo.MOD_ID, "goblin");
	public static final ResourceLocation SPECIES_HALFLING		= new ResourceLocation(Reference.ModInfo.MOD_ID, "halfling");
	public static final ResourceLocation SPECIES_HALF_ORC		= new ResourceLocation(Reference.ModInfo.MOD_ID, "half_orc");
	public static final ResourceLocation SPECIES_HORSE			= new ResourceLocation("minecraft", "horse");
	public static final ResourceLocation SPECIES_KOBOLD			= new ResourceLocation(Reference.ModInfo.MOD_ID, "kobold");
	public static final ResourceLocation SPECIES_LIZARDFOLK		= new ResourceLocation(Reference.ModInfo.MOD_ID, "lizardfolk");
	public static final ResourceLocation SPECIES_MERFOLK		= new ResourceLocation(Reference.ModInfo.MOD_ID, "merfolk");
	public static final ResourceLocation SPECIES_MULE			= new ResourceLocation("minecraft", "mule");
	public static final ResourceLocation SPECIES_ORC			= new ResourceLocation(Reference.ModInfo.MOD_ID, "orc");
	public static final ResourceLocation SPECIES_PIG			= new ResourceLocation("minecraft", "pig");
	public static final ResourceLocation SPECIES_RAT			= new ResourceLocation(Reference.ModInfo.MOD_ID, "rat");
	public static final ResourceLocation SPECIES_SKELETON		= new ResourceLocation("minecraft", "skeleton");
	public static final ResourceLocation SPECIES_SPIDER			= new ResourceLocation("minecraft", "spider");
	public static final ResourceLocation SPECIES_SQUID			= new ResourceLocation("minecraft", "squid");
	public static final ResourceLocation SPECIES_TIEFLING		= new ResourceLocation(Reference.ModInfo.MOD_ID, "tiefling");
	public static final ResourceLocation SPECIES_TROGLODYTE		= new ResourceLocation(Reference.ModInfo.MOD_ID, "troglodyte");
	public static final ResourceLocation SPECIES_WOLF			= new ResourceLocation("minecraft", "wolf");
	public static final ResourceLocation SPECIES_ZOMBIE			= new ResourceLocation("minecraft", "zombie");
	
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
		
		// If no species were found in the datapack, load the defaults
		if(loaded.isEmpty())
		{
			VariousOddities.log.warn("No species found, loading defaults");
			DEFAULT_SPECIES.forEach((species) -> { VORegistries.SPECIES.put(species.getRegistryName(), species); });
		}
		
		// Ensure a plain no-features human species always exists
		if(!loaded.containsKey(Species.HUMAN.getRegistryName()))
			loaded.put(Species.HUMAN.getRegistryName(), Species.HUMAN);
		
		loaded.forEach((name,species) -> { VORegistries.SPECIES.put(name, species); });
	}
	
	private static void addSpecies(Species speciesIn)
	{
		DEFAULT_SPECIES.add(speciesIn);
	}
	
	static
	{
		/*
		 * Dragon, blue
		 * Dragon, ender
		 * Dragon, red
		 * Enderman
		 * Tiefling (3.5E)
		 * Tiefling, Lesser (5E)
		 */
		
		addSpecies(new Species(SPECIES_AASIMAR)
				.setDisplayName(Component.translatable("species."+Reference.ModInfo.MOD_ID+".aasimar"))
				.setPower(1)
				.addType(EnumCreatureType.OUTSIDER, EnumCreatureType.NATIVE)
				.addAbility(new AbilityDarkvision())
				.addAbility(new AbilityResistance(2, DamageType.ACID))
				.addAbility(new AbilityResistance(2, DamageType.COLD))
				.addAbility(new AbilityResistance(2, DamageType.LIGHTNING)));
		addSpecies(new Species(SPECIES_ARCHFEY)
				.setDisplayName(Component.translatable("species."+Reference.ModInfo.MOD_ID+".archfey"))
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
				.setDisplayName(Component.translatable("entity.minecraft.creeper"))
				.addType(EnumCreatureType.PLANT)
				.addAbility(new AbilityModifierStr(-4D))
				.addAbility(new AbilityExplode()));
		addSpecies(new Species(SPECIES_DWARF)
				.setDisplayName(Component.translatable("species."+Reference.ModInfo.MOD_ID+".dwarf"))
				.addType(EnumCreatureType.HUMANOID)
				.addAbility(new AbilitySize(Size.MEDIUM, 0.2F))
				.addAbility(new AbilityModifierCon(2D))
				.addAbility(new AbilityDarkvision())
				.addAbility(new AbilityStability()));
		addSpecies(new Species(SPECIES_DRAGON_GREEN)
				.setDisplayName(Component.translatable("species."+Reference.ModInfo.MOD_ID+".dragon_green"))
				.setPower(5)
				.addType(EnumCreatureType.DRAGON, EnumCreatureType.AIR)
				.addAbility(new AbilitySize(Size.MEDIUM, 0.7F))
				.addAbility(new AbilityModifierStr(3D))
				.addAbility(new AbilityModifierCon(10D))
				.addAbility(new AbilityNaturalArmour(7D))
				.addAbility(new AbilityDamageResistance(DamageType.ACID, DamageResist.IMMUNE))
				.addAbility(AbilityBreatheFluid.water())
				.addAbility(new AbilityFlight(Grade.POOR))
				.addAbility(new AbilityBreathWeapon(DamageType.ACID, BreathType.CONE, 9D, 4F, 24F).setParticle(ParticleTypes.DRAGON_BREATH)));
		addSpecies(new Species(SPECIES_GOBLIN)
				.setDisplayName(Component.translatable("entity."+Reference.ModInfo.MOD_ID+".goblin"))
				.addType(EnumCreatureType.HUMANOID, EnumCreatureType.GOBLIN)
				.addAbility(new AbilitySize(Size.SMALL))
				.addAbility(new AbilityModifierStr(-1D))
				.addAbility(new AbilityDarkvision()));
		addSpecies(new Species(SPECIES_GNOME)
				.setDisplayName(Component.translatable("species."+Reference.ModInfo.MOD_ID+".gnome"))
				.addType(EnumCreatureType.HUMANOID)
				.addAbility(new AbilityModifierCon(2D))
				.addAbility(new AbilitySize(Size.SMALL, 0.4F)));
		addSpecies(new Species(SPECIES_HALFLING)
				.setDisplayName(Component.translatable("species."+Reference.ModInfo.MOD_ID+".halfling"))
				.addType(EnumCreatureType.HUMANOID)
				.addAbility(new AbilitySize(Size.SMALL, 0.1F)));
		addSpecies(new Species(SPECIES_HALF_ORC)
				.setDisplayName(Component.translatable("species."+Reference.ModInfo.MOD_ID+".half_orc"))
				.addType(EnumCreatureType.HUMANOID)
				.addAbility(new AbilitySize(Size.MEDIUM, 0.6F))
				.addAbility(new AbilityModifierStr(1D))
				.addAbility(new AbilityDarkvision()));
		addSpecies(new Species(SPECIES_KOBOLD)
				.setDisplayName(Component.translatable("entity."+Reference.ModInfo.MOD_ID+".kobold"))
				.addType(EnumCreatureType.HUMANOID, EnumCreatureType.REPTILE)
				.addAbility(new AbilitySize(Size.SMALL))
				.addAbility(new AbilityModifierStr(-2D))
				.addAbility(new AbilityModifierCon(-2D))
				.addAbility(new AbilityDarkvision())
				.addAbility(new AbilityNaturalArmour(1D))
				.addAbility(new AbilityLightSensitivity()));
		addSpecies(new Species(SPECIES_LIZARDFOLK)
				.setDisplayName(Component.translatable("species."+Reference.ModInfo.MOD_ID+".lizardfolk"))
				.setPower(1)
				.addType(EnumCreatureType.HUMANOID, EnumCreatureType.REPTILE)
				.addAbility(new AbilityModifierStr(1D))
				.addAbility(new AbilityModifierCon(2D))
				.addAbility(new AbilityNaturalArmour(5D))
				.addAbility(new AbilityHoldBreath()));
		addSpecies(new Species(SPECIES_MERFOLK)
				.setDisplayName(Component.translatable("species."+Reference.ModInfo.MOD_ID+".merfolk"))
				.setPower(1)
				.addType(EnumCreatureType.HUMANOID, EnumCreatureType.AQUATIC)
				.addAbility(new AbilitySwim())
				.addAbility(new AbilityNaturalArmour(3D)));
		addSpecies(new Species(SPECIES_ORC)
				.setDisplayName(Component.translatable("species."+Reference.ModInfo.MOD_ID+".orc"))
				.addType(EnumCreatureType.HUMANOID)
				.addAbility(new AbilitySize(Size.MEDIUM, 0.7F))
				.addAbility(new AbilityModifierStr(2D))
				.addAbility(new AbilityDarkvision())
				.addAbility(new AbilityLightSensitivity()));
		addSpecies(new Species(SPECIES_TIEFLING)
				.setDisplayName(Component.translatable("species."+Reference.ModInfo.MOD_ID+".tiefling"))
				.setPower(1)
				.addType(EnumCreatureType.OUTSIDER, EnumCreatureType.NATIVE)
				.addAbility(new AbilityDarkvision())
				.addAbility(new AbilityResistance(2, DamageType.COLD))
				.addAbility(new AbilityResistance(2, DamageType.FIRE))
				.addAbility(new AbilityResistance(2, DamageType.LIGHTNING)));
		addSpecies(new Species(SPECIES_TROGLODYTE)
				.setDisplayName(Component.translatable("species."+Reference.ModInfo.MOD_ID+".troglodyte"))
				.setPower(2)
				.addType(EnumCreatureType.HUMANOID, EnumCreatureType.REPTILE)
				.addAbility(new AbilityModifierCon(4D))
				.addAbility(new AbilityDarkvision())
				.addAbility(new AbilityNaturalArmour(6D)));
		
		// Utility species used exclusively by mobs
		addSpecies(new Species(SPECIES_BAT)
				.notPlayerSelectable()
				.addType(EnumCreatureType.ANIMAL)
				.addAbility(new AbilityFlight(Grade.GOOD))
				.addAbility(new AbilityBlindsight(6D))
				.addAbility(new AbilitySize(Size.DIMINUTIVE)));
		addSpecies(new Species(SPECIES_CAT)
				.notPlayerSelectable()
				.addType(EnumCreatureType.ANIMAL)
				.addAbility(new AbilityDarkvision())
				.addAbility(new AbilityScent())
				.addAbility(new AbilitySize(Size.TINY)));
		addSpecies(new Species(SPECIES_CAVE_SPIDER)
				.setPower(1)
				.notPlayerSelectable()
				.addType(EnumCreatureType.VERMIN)
				.addAbility(new AbilityNaturalArmour(1D))
				.addAbility(new AbilityClimb())
				.addAbility(new AbilityTremorsense(16))
				.addAbility(new AbilityPoison())
				.addAbility(new AbilityStatusImmunity.Poison())
				.addAbility(new AbilitySize(Size.SMALL)));
		addSpecies(new Species(SPECIES_DONKEY)
				.notPlayerSelectable()
				.addType(EnumCreatureType.ANIMAL)
				.addAbility(new AbilityNaturalArmour(2D))
				.addAbility(new AbilityScent()));
		addSpecies(new Species(SPECIES_GIANT_RAT)
				.notPlayerSelectable()
				.addType(EnumCreatureType.ANIMAL)
				.addAbility(new AbilityNaturalArmour(1D))
				.addAbility(new AbilityClimb())
				.addAbility(new AbilityDarkvision())
				.addAbility(new AbilityScent())
				.addAbility(new AbilitySize(Size.SMALL)));
		addSpecies(new Species(SPECIES_HORSE)
				.setPower(1)
				.notPlayerSelectable()
				.addType(EnumCreatureType.ANIMAL)
				.addAbility(new AbilityNaturalArmour(3D))
				.addAbility(new AbilityModifierStr(1D))
				.addAbility(new AbilityScent())
				.addAbility(new AbilitySize(Size.LARGE)));
		addSpecies(new Species(SPECIES_MULE)
				.setPower(1)
				.notPlayerSelectable()
				.addType(EnumCreatureType.ANIMAL)
				.addAbility(new AbilityNaturalArmour(3D))
				.addAbility(new AbilityModifierStr(3D))
				.addAbility(new AbilityScent()));
		addSpecies(new Species(SPECIES_PIG)
				.notPlayerSelectable()
				.addType(EnumCreatureType.ANIMAL)
				.addAbility(new AbilityDarkvision())
				.addAbility(new AbilityScent()));
		addSpecies(new Species(SPECIES_RAT)
				.notPlayerSelectable()
				.addType(EnumCreatureType.ANIMAL)
				.addAbility(new AbilityClimb())
				.addAbility(new AbilityDarkvision())
				.addAbility(new AbilityScent())
				.addAbility(new AbilitySize(Size.TINY)));
		addSpecies(new Species(SPECIES_SKELETON)
				.notPlayerSelectable()
				.addType(EnumCreatureType.UNDEAD)
				.addAbility(new AbilityNaturalArmour(2D))
				.addAbility(new AbilityModifierStr(1D))
				.addAbility(new AbilityDamageReduction(2))
				.addAbility(new AbilityDamageResistance(DamageType.COLD, DamageResist.IMMUNE)));
		addSpecies(new Species(SPECIES_SPIDER)
				.setPower(1)
				.notPlayerSelectable()
				.addType(EnumCreatureType.VERMIN)
				.addAbility(new AbilityNaturalArmour(1D))
				.addAbility(new AbilityClimb())
				.addAbility(new AbilityTremorsense(16))
				.addAbility(new AbilityStatusImmunity.Poison()));
		addSpecies(new Species(SPECIES_SQUID)
				.setPower(1)
				.notPlayerSelectable()
				.addType(EnumCreatureType.ANIMAL, EnumCreatureType.AQUATIC)
				.addAbility(new AbilitySwim())
				.addAbility(new AbilityNaturalArmour(3D))
				.addAbility(new AbilityModifierStr(2D)));
		addSpecies(new Species(SPECIES_WOLF)
				.setPower(1)
				.notPlayerSelectable()
				.addType(EnumCreatureType.ANIMAL)
				.addAbility(new AbilityNaturalArmour(2D))
				.addAbility(new AbilityModifierStr(1D))
				.addAbility(new AbilityDarkvision())
				.addAbility(new AbilityScent()));
		addSpecies(new Species(SPECIES_ZOMBIE)
				.notPlayerSelectable()
				.addType(EnumCreatureType.UNDEAD)
				.addAbility(new AbilityNaturalArmour(2D))
				.addAbility(new AbilityModifierStr(1D))
				.addAbility(new AbilityDamageReduction(2)));
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
	public static SpeciesInstance instanceFromNBT(CompoundTag compound)
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
