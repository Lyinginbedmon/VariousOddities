package com.lying.variousoddities.api.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.lying.variousoddities.types.EnumCreatureType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Event;

public class CreatureTypeEvent extends Event
{
	private final EnumCreatureType type;
	
	protected CreatureTypeEvent(EnumCreatureType type)
	{
		this.type = type;
	}
	
	public EnumCreatureType getType(){ return this.type; }
	
	/**
	 * Fired by TypesManager when an entity's types are retrieved.<br>
	 * This allows for situational modification of a creature's types.<br>
	 * @author Lying
	 */
	public static class TypeGetEntityTypesEvent extends Event
	{
		private final World world;
		private final LivingEntity entity;
		private final List<EnumCreatureType> types = new ArrayList<>();
		
		public TypeGetEntityTypesEvent(World worldIn, LivingEntity entityIn, Collection<EnumCreatureType> typesIn)
		{
			this.world = worldIn;
			this.entity = entityIn;
			setTypes(typesIn);
		}
		
		public World getWorld(){ return this.world; }
		public LivingEntity getEntity(){ return this.entity; }
		public List<EnumCreatureType> getTypes(){ return this.types; }
		
		public void setTypes(Collection<EnumCreatureType> typesIn)
		{
			this.types.clear();
			this.types.addAll(typesIn);
		}
	}
	
	/**
	 * Fired by LivingData when a type is first applied to a creature.
	 * @author Lying
	 */
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
	
	/**
	 * Fired by LivingData when a type is removed from a creature.
	 * @author Lying
	 */
	public static class TypeRemoveEvent extends TypeApplyEvent
	{
		public TypeRemoveEvent(LivingEntity entity, EnumCreatureType type)
		{
			super(entity, type);
		}
	}
}
