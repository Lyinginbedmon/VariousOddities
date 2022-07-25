package com.lying.variousoddities.api.event;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.lying.variousoddities.species.abilities.Ability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public class GatherAbilitiesEvent extends LivingEvent
{
	private final Map<ResourceLocation, Ability> oldMap = new HashMap<>();
	private Map<ResourceLocation, Ability> abilityMap = new HashMap<>();
	private Map<ResourceLocation, Ability> tempAbilities = new HashMap<>();
	
	public GatherAbilitiesEvent(LivingEntity entity, Map<ResourceLocation, Ability> abilityMap, Map<ResourceLocation, Ability> oldMapIn)
	{
		super(entity);
		setAbilityMap(abilityMap);
		
		this.oldMap.putAll(oldMapIn);
	}
	
	public Map<ResourceLocation, Ability> getAbilityMap(){ return this.abilityMap; }
	public Map<ResourceLocation, Ability> getTempAbilities(){ return this.tempAbilities; }
	public void setAbilityMap(Map<ResourceLocation, Ability> mapIn)
	{
		this.abilityMap.clear();
		mapIn.forEach((name,ability) -> { addAbility(ability); });
	}
	
	public void addAbility(Ability ability){ this.abilityMap.put(ability.getMapName(), ability.clone()); }
	/**
	 * Adds the given ability without a source ID.<br>
	 * Abilities without a source ID are removed in the next cache.
	 */
	public void addTempAbility(Ability ability){ this.tempAbilities.put(ability.getMapName(), ability.clone().setTemporary()); }
	
	public void removeAbility(Ability ability){ removeAbility(ability.getMapName()); }
	public void removeAbility(ResourceLocation mapName){ this.abilityMap.remove(mapName); }
	
	public boolean hasAbility(ResourceLocation mapName){ return this.abilityMap.containsKey(mapName); }
	@Nullable
	public Ability getAbility(ResourceLocation mapName){ return this.abilityMap.get(mapName); }
	/** Returns the ability with this map name in the current ability map */
	@Nullable
	public Ability getOriginalAbility(ResourceLocation mapName){ return this.oldMap.containsKey(mapName) && hasAbility(mapName) ? this.oldMap.get(mapName).clone() : null; }
}
