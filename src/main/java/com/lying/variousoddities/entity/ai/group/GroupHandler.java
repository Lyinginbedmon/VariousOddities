package com.lying.variousoddities.entity.ai.group;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GroupHandler
{
	private static List<EntityGroup> GROUPS = new ArrayList<>();
	
	public static void addGroup(EntityGroup group)
	{
		if(!GROUPS.contains(group))
			GROUPS.add(group);
	}
	
	public static void removeGroup(EntityGroup group)
	{
		GROUPS.remove(group);
	}
	
	public static EntityGroup getGroup(Predicate<EntityGroup> predicate)
	{
		for(EntityGroup group : GROUPS)
			if(predicate.apply(group))
				return group;
		return null;
	}
	
	public static EntityGroup getEntityMemberGroup(Mob entity)
	{
		return getGroup(new Predicate<EntityGroup>()
		{
			public boolean apply(EntityGroup input){ return input.isMember(entity); }
		});
	}
	
	public static EntityGroup getEntityTargetGroup(LivingEntity entity)
	{
		return getGroup(new Predicate<EntityGroup>()
		{
			public boolean apply(EntityGroup input){ return input.isTarget(entity); }
		});
	}
	
	@SubscribeEvent
	public static void onWorldTickEvent(LevelTickEvent event)
	{
		List<EntityGroup> groups = new ArrayList<>();
		groups.addAll(GROUPS);
		
		for(EntityGroup group : groups)
			if(group.isLoaded())
				group.tick();
	}
	
	@SubscribeEvent
	public static void onLivingHurtEvent(LivingHurtEvent event)
	{
		if(!GROUPS.isEmpty() && event.getEntity() instanceof Mob)
		{
			Mob victim = (Mob)event.getEntity();
			DamageSource source = event.getSource();
			LivingEntity attacker = null;
			if(source.getTrueSource() != null && source.getTrueSource() instanceof LivingEntity)
				attacker = (LivingEntity)source.getTrueSource();
			
			EntityGroup group = getEntityMemberGroup((Mob)event.getEntity());
			if(group != null && attacker != null)
				group.onMemberHarmed(event, victim, attacker);
		}
	}
	
	@SubscribeEvent
	public static void onLivingDeathEvent(LivingDeathEvent event)
	{
		for(EntityGroup group : GROUPS)
			if(group.isTracking(event.getEntity()))
				group.onEntityKilled(event);
	}
}
