package com.lying.variousoddities.types.abilities;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.api.event.GatherAbilitiesEvent;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.types.EnumCreatureType;
import com.lying.variousoddities.world.savedata.TypesManager;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

public class AbilityRegistry
{
	public static final IForgeRegistry<Ability.Builder> ABILITIES;
	
	/*
	 * Enderman
	 * 	Silk Touch
	 * Enderdragon
	 * 	Flight
	 * Vex
	 * 	Etherealness
	 * Blaze
	 * 	#Fireballs
	 * Spider
	 * 	#Web
	 * 	Climbing
	 * Rat
	 * 	Disease (# when used on held item)
	 * 
	 * Darkvision
	 * Low-light Vision
	 * Light Sensitivity
	 * Blindsense
	 */
	
	public static void initAbilities()
	{
		registerAbility(AbilityBlind.REGISTRY_NAME, new AbilityBlind.Builder());
		registerAbility(AbilityBlindsight.REGISTRY_NAME, new AbilityBlindsight.Builder());
		registerAbility(AbilityBreathWeapon.REGISTRY_NAME, new AbilityBreathWeapon.Builder());
		registerAbility(AbilityDamageReduction.REGISTRY_NAME, new AbilityDamageReduction.Builder());
		registerAbility(AbilityDamageResistance.REGISTRY_NAME, new AbilityDamageResistance.Builder());
		registerAbility(AbilityExplode.REGISTRY_NAME, new AbilityExplode.Builder());
		registerAbility(AbilityFastHealing.REGISTRY_NAME, new AbilityFastHealing.Builder());
		registerAbility(AbilityGhostForm.REGISTRY_NAME, new AbilityGhostForm.Builder());
		registerAbility(AbilityHeat.REGISTRY_NAME, new AbilityHeat.Builder());
		registerAbility(AbilityHoldBreath.REGISTRY_NAME, new AbilityHoldBreath.Builder());
		registerAbility(AbilityIncorporeality.REGISTRY_NAME, new AbilityIncorporeality.Builder());
		registerAbility(AbilityInvisibility.REGISTRY_NAME, new AbilityInvisibility.Builder());
		registerAbility(AbilityNaturalArmour.REGISTRY_NAME, new AbilityNaturalArmour.Builder());
		registerAbility(AbilityNaturalRegen.REGISTRY_NAME, new AbilityNaturalRegen.Builder());
		registerAbility(AbilityPoison.REGISTRY_NAME, new AbilityPoison.Builder());
		registerAbility(AbilityStatusEffect.REGISTRY_NAME, new AbilityStatusEffect.Builder());
		registerAbility(AbilityTeleportToHome.REGISTRY_NAME, new AbilityTeleportToHome.Builder());
		registerAbility(AbilityTeleportToPos.REGISTRY_NAME, new AbilityTeleportToPos.Builder());
		registerAbility(AbilityWaterWalking.REGISTRY_NAME, new AbilityWaterWalking.Builder());
		
		VariousOddities.log.info("Initialised "+ABILITIES.getEntries().size()+" abilities");
		if(ConfigVO.GENERAL.verboseLogs())
			for(ResourceLocation name : ABILITIES.getKeys())
				VariousOddities.log.info("#   "+name.toString());
	}
	
	private static Ability.Builder registerAbility(ResourceLocation registryName, Ability.Builder builderIn)
	{
		builderIn.setRegistryName(registryName);
		ABILITIES.register(builderIn);
		
		return builderIn;
	}
	
	public static Ability getAbility(ResourceLocation registryName, CompoundNBT nbt)
	{
		if(ABILITIES.containsKey(registryName))
			return ABILITIES.getValue(registryName).create(nbt);
		return null;
	}
	
	public static Ability getAbility(CompoundNBT compound)
	{
		ResourceLocation registryName = new ResourceLocation(compound.getString("Name"));
		
		CompoundNBT abilityData = compound.getCompound("Tag");
		Ability ability = AbilityRegistry.getAbility(registryName, abilityData);
		
		if(compound.contains("CustomName", 8))
			ability.setDisplayName(ITextComponent.Serializer.getComponentFromJson(compound.getString("CustomName")));
		
		return ability;
	}
	
	public static void registerAbilityListeners()
	{
		for(Ability.Builder builder : ABILITIES.getValues())
			builder.create(new CompoundNBT()).addListeners(MinecraftForge.EVENT_BUS);
	}
	
	public static Collection<ResourceLocation> getAbilityNames(){ return ABILITIES.getKeys(); }
	
	/** Returns a map of the given creature's abilities based on their types and LivingData */
	public static Map<ResourceLocation, Ability> getCreatureAbilities(@Nonnull LivingEntity entity)
	{
		Map<ResourceLocation, Ability> abilityMap = new HashMap<>();
		
		if(entity != null)
		{
			// Collect abilities from creature's types
			TypesManager manager = TypesManager.get(entity.getEntityWorld());
			if(manager != null)
				for(EnumCreatureType type : manager.getMobTypes(entity))
					abilityMap = type.addAbilitiesToMap(abilityMap);
			
			// Collect abilities from creature's LivingData
			LivingData data = LivingData.forEntity(entity);
			if(data != null)
				abilityMap = data.getAbilities().addToMap(abilityMap);
			
			GatherAbilitiesEvent event = new GatherAbilitiesEvent(entity, abilityMap);
			MinecraftForge.EVENT_BUS.post(event);
			
			abilityMap = event.getAbilityMap();
		}
		
		return abilityMap;
	}
	
	public static boolean hasAbility(LivingEntity entity, ResourceLocation mapName)
	{
		return getCreatureAbilities(entity).containsKey(mapName);
	}
	
	/** Returns the first ability of the given creature with the given map name. */
	@Nullable
	public static Ability getAbilityByName(LivingEntity entity, ResourceLocation mapName)
	{
		for(Ability ability : getCreatureAbilities(entity).values())
			if(ability.getMapName().equals(mapName))
				return ability;
		return null;
	}
	
	/** Returns a list of all abilities of the given entity with the given registry name. */
	public static List<Ability> getAbilitiesOfType(LivingEntity entity, ResourceLocation registryName)
	{
		List<Ability> abilities = Lists.newArrayList();
		for(Ability ability : getCreatureAbilities(entity).values())
			if(ability.getRegistryName().equals(registryName))
				abilities.add(ability);
		
		return abilities;
	}
	
	private static <T extends IForgeRegistryEntry<T>> IForgeRegistry<T> makeRegistry(ResourceLocation name, Class<T> type, int max)
	{
        return new RegistryBuilder<T>().setName(name).setType(type).setMaxID(max).create();
    }
	
	static
	{
		ABILITIES = makeRegistry(new ResourceLocation(Reference.ModInfo.MOD_ID, "abilities"), Ability.Builder.class, Integer.MAX_VALUE >> 5);
	}
}
