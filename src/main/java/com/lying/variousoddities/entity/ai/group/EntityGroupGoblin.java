package com.lying.variousoddities.entity.ai.group;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.hostile.EntityGoblin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class EntityGroupGoblin extends EntityGroup
{
	public boolean isObserved(LivingEntity entity)
	{
		return !entity.isPotionActive(Effects.INVISIBILITY) ? super.isObserved(entity) : false;
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
				for(LivingEntity nearby : target.getEntityWorld().getEntitiesWithinAABB(LivingEntity.class, target.getBoundingBox().grow(6, 2, 6), new Predicate<LivingEntity>()
					{
						public boolean apply(LivingEntity input)
						{
							if(!input.isAlive() || !isObserved(input))
								return false;
							
							boolean teammate = input.getType() == target.getType() && input.isOnSameTeam(target);
							boolean pet = false;
							if(input instanceof TameableEntity)
							{
								TameableEntity tameable = (TameableEntity)input;
								pet = tameable.getOwnerId() != null && tameable.getOwnerId().equals(target.getUniqueID());
							}
							boolean aggressor = input instanceof MobEntity && members.keySet().contains(((MobEntity)input).getAttackTarget());
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
		List<MobEntity> setTarget = new ArrayList<>();
		setTarget.addAll(members.keySet());
		
		for(LivingEntity target : targets.keySet())
		{
			if(isObserved(target))
			{
				MobEntity nearest = null;
				double minDist = Double.MAX_VALUE;
				for(MobEntity entity : setTarget)
				{
					double dist = target.getDistance(entity);
					if(dist < minDist && entity.getNavigator().getPathToEntity(target, (int)entity.getAttributeValue(Attributes.FOLLOW_RANGE)) != null)
					{
						nearest = entity;
						minDist = dist;
					}
				}
				
				if(nearest != null)
				{
					nearest.setAttackTarget(target);
					setTarget.remove(nearest);
				}
			}
		}
		
		/** Assign any remaining members to the nearest pathable target */
		if(!setTarget.isEmpty())
			for(MobEntity member : setTarget)
			{
				LivingEntity target = null;
				double minDist = Double.MAX_VALUE;
				for(LivingEntity entity : targets.keySet())
				{
					double dist = entity.getDistance(member);
					if(dist < minDist && member.getNavigator().getPathToEntity(entity, (int)member.getAttributeValue(Attributes.FOLLOW_RANGE)) != null)
					{
						target = entity;
						minDist = dist;
					}
				}
				
				if(target != null)
					member.setAttackTarget(target);
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
			for(MobEntity member : members.keySet())
				if(member.isAlive())
				{
					List<EntityGoblin> nearby = member.getEntityWorld().getEntitiesWithinAABB(EntityGoblin.class, member.getBoundingBox().grow(6, 2, 6), new Predicate<EntityGoblin>()
					{
						public boolean apply(EntityGoblin input)
						{
							return input.isAlive() && !input.isChild() && isObserved(input) && GroupHandler.getEntityMemberGroup(input) == null;
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
