package com.lying.variousoddities.species.abilities;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.species.abilities.Ability.Nature;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;

public class AbilityRegistry
{
	public static void init()
	{
		VORegistries.ABILITIES.register("amphibious", () -> new AbilityAmphibious.Builder());
		VORegistries.ABILITIES.register("blind", () -> new AbilityBlind.Builder());
		VORegistries.ABILITIES.register("blindsight", () -> new AbilityBlindsight.Builder());
		VORegistries.ABILITIES.register("fluid_breathing", () -> new AbilityBreatheFluid.Builder());
		VORegistries.ABILITIES.register("breath_weapon", () -> new AbilityBreathWeapon.Builder());
		VORegistries.ABILITIES.register("burrow", () -> new AbilityBurrow.Builder());
		VORegistries.ABILITIES.register("climb", () -> new AbilityClimb.Builder());
		VORegistries.ABILITIES.register("epic_resilience", () -> new AbilityDamageCap.Builder());
		VORegistries.ABILITIES.register("damage_reduction", () -> new AbilityDamageReduction.Builder());
		VORegistries.ABILITIES.register("damage_resistance", () -> new AbilityDamageResistance.Builder());
		VORegistries.ABILITIES.register("darkvision", () -> new AbilityDarkvision.Builder());
		VORegistries.ABILITIES.register("drain_health", () -> new AbilityDrainHealth.Builder());
		VORegistries.ABILITIES.register("etherealness", () -> new AbilityEtherealness.Builder());
		VORegistries.ABILITIES.register("explode", () -> new AbilityExplode.Builder());
		VORegistries.ABILITIES.register("fast_healing", () -> new AbilityFastHealing.Builder());
		VORegistries.ABILITIES.register("ghost_form", () -> new AbilityForm.Ghost.Builder());
		VORegistries.ABILITIES.register("mist_form", () -> new AbilityForm.Mist.Builder());
		VORegistries.ABILITIES.register("flight", () -> new AbilityFlight.Builder());
		VORegistries.ABILITIES.register("gaseous_form", () -> new AbilityGaseous.Builder());
		VORegistries.ABILITIES.register("charming_gaze", () -> new AbilityGaze.Charm.Builder());
		VORegistries.ABILITIES.register("dominating_gaze", () -> new AbilityGaze.Dominate.Builder());
		VORegistries.ABILITIES.register("fear_aura", () -> new AbilityFearAura.Builder());
		VORegistries.ABILITIES.register("petrifying_gaze", () -> new AbilityGaze.Petrify.Builder());
		VORegistries.ABILITIES.register("heat", () -> new AbilityHeat.Builder());
		VORegistries.ABILITIES.register("hold_breath", () -> new AbilityHoldBreath.Builder());
		VORegistries.ABILITIES.register("hurt_by_env", () -> new AbilityHurtByEnv.Builder());
		VORegistries.ABILITIES.register("critical_hit_immunity", () -> new AbilityImmunityCrits.Builder());
		VORegistries.ABILITIES.register("incorporeality", () -> new AbilityIncorporeality.Builder());
		VORegistries.ABILITIES.register("invisibility", () -> new AbilityInvisibility.Builder());
		VORegistries.ABILITIES.register("light_sensitivity", () -> new AbilityLightSensitivity.Builder());
		VORegistries.ABILITIES.register("constitution_modifier", () -> new AbilityModifierCon.Builder());
		VORegistries.ABILITIES.register("strength_modifier", () -> new AbilityModifierStr.Builder());
		VORegistries.ABILITIES.register("natural_armour", () -> new AbilityNaturalArmour.Builder());
		VORegistries.ABILITIES.register("natural_regen", () -> new AbilityNaturalRegen.Builder());
		VORegistries.ABILITIES.register("poison", () -> new AbilityPoison.Builder());
		VORegistries.ABILITIES.register("status_immunity", () -> new AbilityStatusImmunity.Configurable.Builder());
		VORegistries.ABILITIES.register("paralysis_immunity", () -> new AbilityStatusImmunity.Paralysis.Builder());
		VORegistries.ABILITIES.register("poison_immunity", () -> new AbilityStatusImmunity.Poison.Builder());
		VORegistries.ABILITIES.register("rend", () -> new AbilityRend.Builder());
		VORegistries.ABILITIES.register("resistance", () -> new AbilityResistance.Builder());
		VORegistries.ABILITIES.register("resistance_spell", () -> new AbilityResistanceSpell.Builder());
		VORegistries.ABILITIES.register("scent", () -> new AbilityScent.Builder());
		VORegistries.ABILITIES.register("size", () -> new AbilitySize.Builder());
		VORegistries.ABILITIES.register("smite", () -> new AbilitySmite.Builder());
		VORegistries.ABILITIES.register("stability", () -> new AbilityStability.Builder());
		VORegistries.ABILITIES.register("starting_item", () -> new AbilityStartingItem.Builder());
		VORegistries.ABILITIES.register("status_effect", () -> new AbilityStatusEffect.Builder());
		VORegistries.ABILITIES.register("sunburn", () -> new AbilitySunBurn.Builder());
		VORegistries.ABILITIES.register("swim", () -> new AbilitySwim.Builder());
		VORegistries.ABILITIES.register("teleport_to_home", () -> new AbilityTeleportToHome.Builder());
		VORegistries.ABILITIES.register("teleport_to_position", () -> new AbilityTeleportToPos.Builder());
		VORegistries.ABILITIES.register("tremorsense", () -> new AbilityTremorsense.Builder());
		VORegistries.ABILITIES.register("unarmed_strike", () -> new AbilityUnarmedStrike.Builder());
		VORegistries.ABILITIES.register("water_walking", () -> new AbilityWaterWalking.Builder());
	}
	
	@Nullable
	public static Ability getAbility(ResourceLocation registryName, CompoundTag nbt)
	{
		for(Entry<ResourceKey<Ability.Builder>, Ability.Builder> entry : VORegistries.ABILITIES_REGISTRY.get().getEntries())
			if(entry.getKey().location().equals(registryName))
				return entry.getValue().create(nbt);
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
		VORegistries.ABILITIES_REGISTRY.get().getEntries().forEach((entry) -> entry.getValue().create(new CompoundTag()).addListeners(MinecraftForge.EVENT_BUS));
	}
	
	public static Collection<ResourceLocation> getAbilityNames()
	{
		List<ResourceLocation> keys = Lists.newArrayList();
		VORegistries.ABILITIES_REGISTRY.get().getEntries().forEach((entry) -> keys.add(entry.getKey().location()));
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
