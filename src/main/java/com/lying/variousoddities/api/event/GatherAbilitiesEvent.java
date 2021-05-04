package com.lying.variousoddities.api.event;

import java.util.Map;

import com.lying.variousoddities.types.abilities.Ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingEvent;

public class GatherAbilitiesEvent extends LivingEvent
{
	private Map<ResourceLocation, Ability> map;
	
	public GatherAbilitiesEvent(LivingEntity entity, Map<ResourceLocation, Ability> abilityMap)
	{
		super(entity);
		this.map = abilityMap;
	}
	
	public Map<ResourceLocation, Ability> getAbilityMap(){ return this.map; }
	
	public void addAbility(Ability ability){ this.map.put(ability.getMapName(), ability); }
	
	public void removeAbility(Ability ability){ removeAbility(ability.getMapName()); }
	public void removeAbility(ResourceLocation mapName){ this.map.remove(mapName); }
	
	public boolean hasAbility(ResourceLocation mapName){ return this.map.containsKey(mapName); }
	public Ability getAbility(ResourceLocation mapName){ return this.map.get(mapName); }
}
