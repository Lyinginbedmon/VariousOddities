package com.lying.variousoddities.api.event;

import com.lying.variousoddities.types.EnumCreatureType;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public class CreatureTypeEvent extends Event
{
	private final EnumCreatureType type;
	
	protected CreatureTypeEvent(EnumCreatureType type)
	{
		this.type = type;
	}
	
	public EnumCreatureType getType(){ return this.type; }
	
	public static class TypeApplyEvent extends CreatureTypeEvent
	{
		private final LivingEntity entity;
		
		public TypeApplyEvent(LivingEntity entity, EnumCreatureType type)
		{
			super(type);
			this.entity = entity;
		}
		
		public LivingEntity getEntityLiving(){ return entity; }
	}
	
	public static class TypeRemoveEvent extends TypeApplyEvent
	{
		public TypeRemoveEvent(LivingEntity entity, EnumCreatureType type)
		{
			super(entity, type);
		}
	}
}
