package com.lying.variousoddities.api.event;

import java.util.EnumSet;

import com.lying.variousoddities.species.abilities.DamageType;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.eventbus.api.Event;

public class DamageTypesEvent extends Event
{
	private final DamageSource source;
	private EnumSet<DamageType> types = EnumSet.noneOf(DamageType.class);
	
	public DamageTypesEvent(DamageSource sourceIn, EnumSet<DamageType> baseAppraisal)
	{
		this.source = sourceIn;
		types = baseAppraisal;
	}
	
	public DamageSource getSource(){ return source; }
	
	public void addType(DamageType type)
	{
		if(!types.contains(type))
			types.add(type);
	}
	
	public void removeType(DamageType type)
	{
		if(types.contains(type))
			types.remove(type);
	}
	
	public EnumSet<DamageType> getTypes(){ return this.types; }
}
