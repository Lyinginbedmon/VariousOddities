package com.lying.variousoddities.entity.ai.group;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.passive.EntityKobold;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class EntityGroupKobold extends EntityGroup
{
	public boolean isObserved(LivingEntity entity)
	{
		return !entity.isPotionActive(Effects.INVISIBILITY) ? super.isObserved(entity) : false;
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
		List<MobEntity> unoccupied = new ArrayList<>();
		for(MobEntity entity : members.keySet())
			if(entity.isAlive() && (entity.getAttackTarget() == null || !entity.getAttackTarget().isAlive()))
				unoccupied.add(entity);
		
		for(MobEntity entity : unoccupied)
		{
			BlockPos entPos = entity.getPosition();
			PathNavigator navigator = entity.getNavigator();
			
			LivingEntity nearest = null;
			double minDist = Double.MAX_VALUE;
			for(LivingEntity target : targets.keySet())
			{
				BlockPos targetPos = targets.get(target).location();
				if(navigator.getPathToPos(targetPos, (int)entity.getAttribute(Attributes.FOLLOW_RANGE).getValue()) != null)
				{
					double dist = targetPos.distanceSq(entPos);
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
					entity.setAttackTarget(nearest);
				else if(navigator.noPath())
				{
					BlockPos location = targets.get(nearest).location();
					navigator.tryMoveToXYZ(location.getX(), location.getY(), location.getZ(), 1.0D);
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
			for(MobEntity member : members.keySet())
				if(member.isAlive())
				{
					List<EntityKobold> nearby = member.getEntityWorld().getEntitiesWithinAABB(EntityKobold.class, member.getBoundingBox().grow(6, 2, 6), new Predicate<EntityKobold>()
					{
						public boolean apply(EntityKobold input)
						{
							return input.isAlive() && !input.isChild() && isObserved(input) && input.getAttackTarget() == null && GroupHandler.getEntityMemberGroup(input) == null;
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
	
	public void onMemberHarmed(LivingHurtEvent event, MobEntity victim, LivingEntity attacker)
	{
		if(isObserved(victim))
			if(attacker.isPotionActive(Effects.INVISIBILITY))
				targets.put(attacker, new Sighting(victim));
			else
				addTarget(attacker);
	}
	
	public void onEntityKilled(LivingDeathEvent event)
	{
		LivingEntity living = event.getEntityLiving();
		if(isObserved(living))
			super.onEntityKilled(event);
	}
}
