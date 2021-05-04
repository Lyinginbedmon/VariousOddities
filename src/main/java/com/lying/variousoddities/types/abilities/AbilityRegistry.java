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
import com.lying.variousoddities.types.EnumCreatureType;
import com.lying.variousoddities.world.savedata.TypesManager;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

public class AbilityRegistry
{
	private static final Map<ResourceLocation, Ability.Builder> REGISTRY = new HashMap<>();
	
	public static final Ability.Builder BLIND				= registerAbility(AbilityBlind.REGISTRY_NAME, new AbilityBlind.Builder());
	public static final Ability.Builder BLIND_SIGHT			= registerAbility(AbilityBlindsight.REGISTRY_NAME, new AbilityBlindsight.Builder());
	public static final Ability.Builder BREATH_WEAPON		= registerAbility(AbilityBreathWeapon.REGISTRY_NAME, new AbilityBreathWeapon.Builder());
	public static final Ability.Builder DAMAGE_REDUCTION	= registerAbility(AbilityDamageReduction.REGISTRY_NAME, new AbilityDamageReduction.Builder());
	public static final Ability.Builder DAMAGE_RESISTANCE	= registerAbility(AbilityDamageResistance.REGISTRY_NAME, new AbilityDamageResistance.Builder());
	public static final Ability.Builder EXPLODE				= registerAbility(AbilityExplode.REGISTRY_NAME, new AbilityExplode.Builder());
	public static final Ability.Builder GHOST_FORM			= registerAbility(AbilityGhostForm.REGISTRY_NAME, new AbilityGhostForm.Builder());
	public static final Ability.Builder HEAT				= registerAbility(AbilityHeat.REGISTRY_NAME, new AbilityHeat.Builder());
	public static final Ability.Builder HOLD_BREATH			= registerAbility(AbilityHoldBreath.REGISTRY_NAME, new AbilityHoldBreath.Builder());
	public static final Ability.Builder INCORPOREALITY		= registerAbility(AbilityIncorporeality.REGISTRY_NAME, new AbilityIncorporeality.Builder());
	public static final Ability.Builder INVISIBILITY		= registerAbility(AbilityInvisibility.REGISTRY_NAME, new AbilityInvisibility.Builder());
	public static final Ability.Builder SILK_TOUCH			= registerAbility(AbilitySilkTouch.REGISTRY_NAME, new AbilitySilkTouch.Builder());
	public static final Ability.Builder STATUS_EFFECT		= registerAbility(AbilityStatusEffect.REGISTRY_NAME, new AbilityStatusEffect.Builder());
	public static final Ability.Builder TELEPORT_HOME		= registerAbility(AbilityTeleportToHome.REGISTRY_NAME, new AbilityTeleportToHome.Builder());
	public static final Ability.Builder TELEPORT_POS		= registerAbility(AbilityTeleportToPos.REGISTRY_NAME, new AbilityTeleportToPos.Builder());
	
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
	 * 	Poison (# when used on held item)
	 * 	Climbing
	 * Rat
	 * 	Disease (# when used on held item)
	 * 
	 * Darkvision
	 * Low-light Vision
	 * Blindsense
	 */
	
	private static Ability.Builder registerAbility(ResourceLocation registryName, Ability.Builder builderIn)
	{
		if(REGISTRY.containsKey(registryName))
			VariousOddities.log.warn("Overwrote existing ability registry for "+registryName);
		REGISTRY.put(registryName, builderIn);
		
		return builderIn;
	}
	
	public static Ability getAbility(ResourceLocation registryName, CompoundNBT nbt)
	{
		if(REGISTRY.containsKey(registryName))
			return REGISTRY.get(registryName).create(nbt);
		return null;
	}
	
	public static Ability getAbility(CompoundNBT compound)
	{
		ResourceLocation registryName = new ResourceLocation(compound.getString("Name"));
		return AbilityRegistry.getAbility(registryName, compound.getCompound("Tag"));
	}
	
	public static void registerAbilityListeners()
	{
		for(Ability.Builder builder : REGISTRY.values())
			builder.create(new CompoundNBT()).addListeners(MinecraftForge.EVENT_BUS);
	}
	
	public static Collection<ResourceLocation> getAbilityNames(){ return REGISTRY.keySet(); }
	
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
}
