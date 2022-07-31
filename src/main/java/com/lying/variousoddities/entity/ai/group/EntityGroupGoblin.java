package com.lying.variousoddities.entity.ai.group;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.hostile.EntityGoblin;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class EntityGroupGoblin extends EntityGroup
{
	public boolean isObserved(LivingEntity entity)
	{
		return !entity.hasEffect(MobEffects.INVISIBILITY) ? super.isObserved(entity) : false;
	}
	
	/** Called every tick by the world to update group logic */
	public void tick()
	{
		super.tick();
		
		targetNearby();
		if(!targets.isEmpty())
		{
			reassignAttackers();
			recruitNearby();
		}
		
		if(targets.isEmpty() || members.isEmpty())
			GroupHandler.removeGroup(this);
	}
	
	/**
	 * Target all nearby entities that share a team with a current target.<br>
	 */
	public void targetNearby()
	{
		if(!targets.isEmpty())
		{
			List<LivingEntity> targets = new ArrayList<>();
			targets.addAll(this.targets.keySet());
			
			for(LivingEntity target : targets)
				for(LivingEntity nearby : target.getLevel().getEntitiesOfClass(Mob.class, target.getBoundingBox().inflate(6, 2, 6), new Predicate<LivingEntity>()
					{
						public boolean apply(LivingEntity input)
						{
							if(!input.isAlive() || !isObserved(input))
								return false;
							
							boolean teammate = input.getType() == target.getType() && input.getTeam().isAlliedTo(target.getTeam());
							boolean pet = false;
							if(input instanceof TamableAnimal)
							{
								TamableAnimal tameable = (TamableAnimal)input;
								pet = tameable.getOwnerUUID() != null && tameable.getOwnerUUID().equals(target.getUUID());
							}
							boolean aggressor = input instanceof Mob && members.keySet().contains(((Mob)input).getTarget());
							return teammate || pet || aggressor;
						}
					}))
						addTarget(nearby);
		}
	}
	
	/**
	 * For each target, assign nearest member to attack.<br>
	 * If more members than targets, assign to nearest target.
	 */
	public void reassignAttackers()
	{
		List<Mob> setTarget = new ArrayList<>();
		setTarget.addAll(members.keySet());
		
		for(LivingEntity target : targets.keySet())
		{
			if(isObserved(target))
			{
				Mob nearest = null;
				double minDist = Double.MAX_VALUE;
				for(Mob entity : setTarget)
				{
					double dist = target.distanceToSqr(entity);
					if(dist < minDist && entity.getNavigation().createPath(target, (int)entity.getAttributeValue(Attributes.FOLLOW_RANGE)) != null)
					{
						nearest = entity;
						minDist = dist;
					}
				}
				
				if(nearest != null)
				{
					nearest.setTarget(target);
					setTarget.remove(nearest);
				}
			}
		}
		
		/** Assign any remaining members to the nearest pathable target */
		if(!setTarget.isEmpty())
			for(Mob member : setTarget)
			{
				LivingEntity target = null;
				double minDist = Double.MAX_VALUE;
				for(LivingEntity entity : targets.keySet())
				{
					double dist = entity.distanceToSqr(member);
					if(dist < minDist && member.getNavigation().createPath(entity, (int)member.getAttributeValue(Attributes.FOLLOW_RANGE)) != null)
					{
						target = entity;
						minDist = dist;
					}
				}
				
				if(target != null)
					member.setTarget(target);
			}
	}
	
	/**
	 * Find unaffiliated kobolds within proximity and sight of members and repopulate the group with them.
	 */
	public void recruitNearby()
	{
		if(members.size() < (targets.size() * 8))
		{
			List<EntityGoblin> goblins = new ArrayList<>();
			for(Mob member : members.keySet())
				if(member.isAlive())
				{
					List<EntityGoblin> nearby = member.getLevel().getEntitiesOfClass(EntityGoblin.class, member.getBoundingBox().inflate(6, 2, 6), new Predicate<EntityGoblin>()
					{
						public boolean apply(EntityGoblin input)
						{
							return input.isAlive() && !input.isBaby() && isObserved(input) && GroupHandler.getEntityMemberGroup(input) == null;
						}
					});
					nearby.removeAll(goblins);
					goblins.addAll(nearby);
				}
			
			if(!goblins.isEmpty())
				do
				{
					addMember(goblins.get(0));
					goblins.remove(0);
				}
				while(members.size() < (targets.size() * 8) && !goblins.isEmpty());
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
