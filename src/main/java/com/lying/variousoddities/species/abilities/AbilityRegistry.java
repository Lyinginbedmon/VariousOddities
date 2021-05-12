package com.lying.variousoddities.species.abilities;

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
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.world.savedata.TypesManager;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class AbilityRegistry
{
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
	
	public static void onRegisterAbilities(RegistryEvent.Register<Ability.Builder> event)
	{
		IForgeRegistry<Ability.Builder> registry = event.getRegistry();
		
		registry.register(new AbilityBlind.Builder());
		registry.register(new AbilityBlindsight.Builder());
		registry.register(new AbilityBreathWeapon.Builder());
		registry.register(new AbilityDamageReduction.Builder());
		registry.register(new AbilityDamageResistance.Builder());
		registry.register(new AbilityExplode.Builder());
		registry.register(new AbilityFastHealing.Builder());
		registry.register(new AbilityGhostForm.Builder());
		registry.register(new AbilityHeat.Builder());
		registry.register(new AbilityHoldBreath.Builder());
		registry.register(new AbilityIncorporeality.Builder());
		registry.register(new AbilityInvisibility.Builder());
		registry.register(new AbilityNaturalArmour.Builder());
		registry.register(new AbilityNaturalRegen.Builder());
		registry.register(new AbilityPoison.Builder());
		registry.register(new AbilityStatusEffect.Builder());
		registry.register(new AbilityTeleportToHome.Builder());
		registry.register(new AbilityTeleportToPos.Builder());
		registry.register(new AbilityWaterWalking.Builder());
		
		VariousOddities.log.info("Initialised "+registry.getEntries().size()+" abilities");
		if(ConfigVO.GENERAL.verboseLogs())
			for(ResourceLocation name : registry.getKeys())
				VariousOddities.log.info("#   "+name.toString());
		
		registerAbilityListeners();
	}
	
	public static Ability getAbility(ResourceLocation registryName, CompoundNBT nbt)
	{
		if(VORegistries.ABILITIES.containsKey(registryName))
			return VORegistries.ABILITIES.getValue(registryName).create(nbt);
		return null;
	}
	
	public static Ability getAbility(CompoundNBT compound)
	{
		ResourceLocation registryName = new ResourceLocation(compound.getString("Name"));
		
		CompoundNBT abilityData = compound.contains("Tag", 10) ? compound.getCompound("Tag") : new CompoundNBT();
		Ability ability = AbilityRegistry.getAbility(registryName, abilityData);
		
		if(compound.contains("CustomName", 8))
			ability.setDisplayName(ITextComponent.Serializer.getComponentFromJson(compound.getString("CustomName")));
		
		return ability;
	}
	
	public static void registerAbilityListeners()
	{
		for(Ability.Builder builder : VORegistries.ABILITIES.getValues())
			builder.create(new CompoundNBT()).addListeners(MinecraftForge.EVENT_BUS);
	}
	
	public static Collection<ResourceLocation> getAbilityNames(){ return VORegistries.ABILITIES.getKeys(); }
	
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
			{
				if(data.hasSpecies())
					abilityMap = data.getSpecies().addToMap(abilityMap);
				abilityMap = data.getAbilities().addToMap(abilityMap);
			}
			
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
}
