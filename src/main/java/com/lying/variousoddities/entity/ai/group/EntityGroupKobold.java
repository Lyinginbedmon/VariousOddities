package com.lying.variousoddities.entity.ai.group;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.passive.EntityKobold;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class EntityGroupKobold extends EntityGroup
{
	public boolean isObserved(LivingEntity entity)
	{
		return !entity.hasEffect(MobEffects.INVISIBILITY) ? super.isObserved(entity) : false;
	}
	
	/** Called every tick by the world to update group logic */
	public void tick()
	{
		super.tick();
		
		reassignAttackers();
		recruitNearby();
	}
	
	/**
	 * For each unoccupied member,<br>
	 * 	Find the nearest pathable target based on their last known location<br>
	 * 		If target is observed, attack them<br>
	 * 		If target is unobserved, move towards their last known location<br>
	 */
	public void reassignAttackers()
	{
		List<Mob> unoccupied = new ArrayList<>();
		for(Mob entity : members.keySet())
			if(entity.isAlive() && (entity.getTarget() == null || !entity.getTarget().isAlive()))
				unoccupied.add(entity);
		
		for(Mob entity : unoccupied)
		{
			BlockPos entPos = entity.blockPosition();
			PathNavigation navigator = entity.getNavigation();
			
			LivingEntity nearest = null;
			double minDist = Double.MAX_VALUE;
			for(LivingEntity target : targets.keySet())
			{
				BlockPos targetPos = targets.get(target).location();
				if(navigator.createPath(targetPos, (int)entity.getAttribute(Attributes.FOLLOW_RANGE).getValue()) != null)
				{
					double dist = targetPos.distSqr(entPos);
					if(nearest == null || dist < minDist)
					{
						nearest = target;
						minDist = dist;
					}
				}
			}
			
			if(nearest != null)
			{
				if(isObserved(nearest))
					entity.setTarget(nearest);
				else if(navigator.isDone())
				{
					BlockPos location = targets.get(nearest).location();
					navigator.moveTo(location.getX(), location.getY(), location.getZ(), 1.0D);
				}
			}
		}
	}
	
	/**
	 * Find unaffiliated kobolds within proximity and sight of members and repopulate the group with them.
	 */
	public void recruitNearby()
	{
		if(members.size() < (targets.size() * 3))
		{
			List<EntityKobold> kobolds = new ArrayList<>();
			for(Mob member : members.keySet())
				if(member.isAlive())
				{
					List<EntityKobold> nearby = member.getLevel().getEntitiesOfClass(EntityKobold.class, member.getBoundingBox().inflate(6, 2, 6), new Predicate<EntityKobold>()
					{
						public boolean apply(EntityKobold input)
						{
							return input.isAlive() && !input.isBaby() && isObserved(input) && input.getTarget() == null && GroupHandler.getEntityMemberGroup(input) == null;
						}
					});
					nearby.removeAll(kobolds);
					kobolds.addAll(nearby);
				}
			
			if(!kobolds.isEmpty())
				do
				{
					addMember(kobolds.get(0));
					kobolds.remove(0);
				}
				while(members.size() < (targets.size() * 3) && !kobolds.isEmpty());
		}
	}
	
	public void onMemberHarmed(LivingHurtEvent event, Mob victim, LivingEntity attacker)
	{
		if(isObserved(victim))
			if(attacker.hasEffect(MobEffects.INVISIBILITY))
				targets.put(attacker, new Sighting(victim));
			else
				addTarget(attacker);
	}
	
	public void onEntityKilled(LivingDeathEvent event)
	{
		LivingEntity living = event.getEntity();
		if(isObserved(living))
			super.onEntityKilled(event);
	}
}
