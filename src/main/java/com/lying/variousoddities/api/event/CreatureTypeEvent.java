package com.lying.variousoddities.api.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import com.google.common.collect.Lists;
import com.lying.variousoddities.types.EnumCreatureType;
import com.lying.variousoddities.types.EnumCreatureType.ActionSet;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
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
			set(typesIn);
		}
		
		public World getWorld(){ return this.world; }
		public LivingEntity getEntity(){ return this.entity; }
		public List<EnumCreatureType> getTypes(){ return this.types; }
		
		public void add(EnumCreatureType typeIn)
		{
			if(!this.types.contains(typeIn))
				this.types.add(typeIn);
		}
		
		public void remove(EnumCreatureType typeIn)
		{
			if(this.types.contains(typeIn))
				this.types.remove(typeIn);
		}
		
		public void set(Collection<EnumCreatureType> typesIn)
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
	
	public static class GetTypeActionsEvent extends LivingEvent
	{
		private final List<EnumCreatureType> types = Lists.newArrayList();
		private ActionSet actions;
		
		public GetTypeActionsEvent(LivingEntity living, Collection<EnumCreatureType> typesIn, EnumSet<EnumCreatureType.Action> actionsIn)
		{
			this(living, typesIn, new ActionSet(actionsIn));
		}
		
		public GetTypeActionsEvent(LivingEntity living, Collection<EnumCreatureType> typesIn, ActionSet actionSetIn)
		{
			super(living);
			this.types.addAll(typesIn);
			this.actions = actionSetIn;
		}
		
		public List<EnumCreatureType> types()
		{
			List<EnumCreatureType> typesOut = Lists.newArrayList();
			typesOut.addAll(this.types);
			return typesOut;
		}
		
		public ActionSet getActions(){ return this.actions; }
		
		public boolean hasAction(EnumCreatureType.Action action){ return actions.contains(action); }
		
		public void add(EnumCreatureType.Action action){ if(!hasAction(action)) actions.add(action); }
		public void remove(EnumCreatureType.Action action){ if(hasAction(action)) actions.remove(action); }
	}
}
