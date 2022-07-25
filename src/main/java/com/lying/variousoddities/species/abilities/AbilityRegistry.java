package com.lying.variousoddities.species.abilities;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.species.abilities.Ability.Nature;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;

public class AbilityRegistry
{
	private static final Map<ResourceLocation, Ability.Builder> ABILITY_MAP = new HashMap<>();
	
	public void init()
	{
		register(new AbilityAmphibious.Builder());
		register(new AbilityBlind.Builder());
		register(new AbilityBlindsight.Builder());
		register(new AbilityBreatheFluid.Builder());
		register(new AbilityBreathWeapon.Builder());
		register(new AbilityBurrow.Builder());
		register(new AbilityClimb.Builder());
		register(new AbilityDamageCap.Builder());
		register(new AbilityDamageReduction.Builder());
		register(new AbilityDamageResistance.Builder());
		register(new AbilityDarkvision.Builder());
		register(new AbilityDrainHealth.Builder());
		register(new AbilityEtherealness.Builder());
		register(new AbilityExplode.Builder());
		register(new AbilityFastHealing.Builder());
		register(new AbilityForm.Ghost.Builder());
		register(new AbilityForm.Mist.Builder());
		register(new AbilityFlight.Builder());
		register(new AbilityGaseous.Builder());
		register(new AbilityGaze.Charm.Builder());
		register(new AbilityGaze.Dominate.Builder());
		register(new AbilityFearAura.Builder());
		register(new AbilityGaze.Petrify.Builder());
		register(new AbilityHeat.Builder());
		register(new AbilityHoldBreath.Builder());
		register(new AbilityHurtByEnv.Builder());
		register(new AbilityImmunityCrits.Builder());
		register(new AbilityIncorporeality.Builder());
		register(new AbilityInvisibility.Builder());
		register(new AbilityLightSensitivity.Builder());
		register(new AbilityModifierCon.Builder());
		register(new AbilityModifierStr.Builder());
		register(new AbilityNaturalArmour.Builder());
		register(new AbilityNaturalRegen.Builder());
		register(new AbilityPoison.Builder());
		register(new AbilityStatusImmunity.Configurable.Builder());
		register(new AbilityStatusImmunity.Paralysis.Builder());
		register(new AbilityStatusImmunity.Poison.Builder());
		register(new AbilityRend.Builder());
		register(new AbilityResistance.Builder());
		register(new AbilityResistanceSpell.Builder());
		register(new AbilityScent.Builder());
		register(new AbilitySize.Builder());
		register(new AbilitySmite.Builder());
		register(new AbilityStability.Builder());
		register(new AbilityStartingItem.Builder());
		register(new AbilityStatusEffect.Builder());
		register(new AbilitySunBurn.Builder());
		register(new AbilitySwim.Builder());
		register(new AbilityTeleportToHome.Builder());
		register(new AbilityTeleportToPos.Builder());
		register(new AbilityTremorsense.Builder());
		register(new AbilityUnarmedStrike.Builder());
		register(new AbilityWaterWalking.Builder());
		
		registerAbilityListeners();
	}
	
	private static void register(Ability.Builder builderIn)
	{
		VORegistries.ABILITIES.register(builderIn.getRegistryName().toString(), () -> builderIn);
		ABILITY_MAP.put(builderIn.getRegistryName(), builderIn);
	}
	
	public static Ability getAbility(ResourceLocation registryName, CompoundTag nbt)
	{
		if(ABILITY_MAP.containsKey(registryName))
			return ABILITY_MAP.get(registryName).create(nbt);
		return null;
	}
	
	public static Ability getAbility(CompoundTag compound)
	{
		ResourceLocation registryName = new ResourceLocation(compound.getString("Name"));
		
		CompoundTag abilityData = compound.contains("Tag", 10) ? compound.getCompound("Tag") : new CompoundTag();
		Ability ability = AbilityRegistry.getAbility(registryName, abilityData);
		
		if(ability != null)
		{
			if(compound.contains("UUID", 8))
				ability.setSourceId(UUID.fromString(compound.getString("UUID")));
			
			if(compound.contains("CustomName", 8))
				ability.setDisplayName(Component.Serializer.fromJson(compound.getString("CustomName")));
			
			if(compound.contains("CustomDesc", 8))
				ability.setCustomDesc(Component.Serializer.fromJson(compound.getString("CustomDesc")));
			
			if(compound.contains("CustomNature", 8))
				ability.setCustomNature(Nature.fromString(compound.getString("CustomNature")));
		}
		
		return ability;
	}
	
	public static void registerAbilityListeners()
	{
		VORegistries.ABILITIES.getEntries().forEach((entry) -> entry.get().create(new CompoundTag()).addListeners(MinecraftForge.EVENT_BUS));
	}
	
	public static Collection<ResourceLocation> getAbilityNames()
	{
		List<ResourceLocation> keys = Lists.newArrayList();
		VORegistries.ABILITIES.getEntries().forEach((entry) -> keys.add(entry.getId()));
		return keys;
	}
	
	/** Returns a map of the given creature's abilities based on their types and LivingData */
	public static Map<ResourceLocation, Ability> getCreatureAbilities(@Nonnull LivingEntity entity)
	{
		if(entity != null)
		{
			LivingData data = LivingData.forEntity(entity);
			if(data != null)
				return data.getAbilities().getCachedAbilities();
		}
		
		return new HashMap<>();
	}
	
	public static boolean hasAbility(LivingEntity entity, ResourceLocation mapName)
	{
		Map<ResourceLocation, Ability> abilities = getCreatureAbilities(entity);
		return abilities.containsKey(mapName) && abilities.get(mapName) != null;
	}
	
	public static boolean hasAbility(LivingEntity entity, Class<?> classIn)
	{
		return !getAbilitiesOfType(entity, classIn).isEmpty();
	}
	
	/** Returns the first ability of the given creature with the given map name. */
	@Nullable
	public static Ability getAbilityByName(LivingEntity entity, ResourceLocation mapName)
	{
		return getCreatureAbilities(entity).get(mapName);
	}
	
	/** Returns a list of all abilities of the given entity with the given registry name. */
	public static Collection<Ability> getAbilitiesOfType(LivingEntity entity, ResourceLocation registryName)
	{
		List<Ability> list = Lists.newArrayList();
		if(entity == null)
			return list;
		
		Map<ResourceLocation, Ability> abilities = getCreatureAbilities(entity);
		if(abilities != null && !abilities.isEmpty())
			abilities.values().forEach((ability) -> { if(ability.getRegistryName().equals(registryName)) list.add(ability); });
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Collection<T> getAbilitiesOfType(LivingEntity entity, Class<T> classIn)
	{
		List<T> list = Lists.newArrayList();
		if(entity == null || classIn == null)
			return list;
		
		Map<ResourceLocation, Ability> abilities = AbilityRegistry.getCreatureAbilities(entity);
		if(abilities != null && !abilities.isEmpty())
			abilities.values().forEach((ability) -> {
				if(ability != null && classIn.isInstance(ability))
					list.add((T)ability); });
		return list;
	}
}
