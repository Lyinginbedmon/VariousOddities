package com.lying.variousoddities.entity.ai;

import java.util.EnumSet;

import com.google.common.base.Predicate;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.faction.FactionReputation.EnumAttitude;
import com.lying.variousoddities.faction.FactionReputation.EnumInteraction;
import com.lying.variousoddities.world.savedata.FactionManager;
import com.lying.variousoddities.world.savedata.FactionManager.Faction;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class EntityAITargetHostileFaction extends TargetGoal
{
	private final PathfinderMob entity;
	private final int targetChance;
	
	private final TargetingConditions targetEntitySelector;
	
	private LivingEntity nearestTarget = null;
	
	public EntityAITargetHostileFaction(PathfinderMob goalOwnerIn, boolean checkSight)
	{
		this(goalOwnerIn, checkSight, false);
	}
	
	public EntityAITargetHostileFaction(PathfinderMob goalOwnerIn, boolean checkSight, boolean nearbyOnlyIn)
	{
		this(goalOwnerIn, 10, checkSight, nearbyOnlyIn);
	}
	
	public EntityAITargetHostileFaction(PathfinderMob goalOwnerIn, int targetChanceIn, boolean checkSight, boolean nearbyOnlyIn)
	{
		super(goalOwnerIn, checkSight, nearbyOnlyIn);
		this.entity = goalOwnerIn;
		this.targetChance = targetChanceIn;
		this.setFlags(EnumSet.of(Goal.Flag.TARGET));
		
		Predicate<LivingEntity> targetPredicate = new Predicate<LivingEntity>()
			{
				public boolean apply(LivingEntity input)
				{
					FactionManager manager = FactionManager.get(input.getLevel());
					Faction ownerFaction = manager.getFaction(entity);
					if(ownerFaction != null)
						if(input.getType() == EntityType.PLAYER)
						{
							PlayerData data = PlayerData.getCapability((Player)input);
							if(data != null)
							{
								int reputation = data.reputation.getReputation(ownerFaction.name);
								if(reputation == Integer.MIN_VALUE)
								{
									data.reputation.setReputation(ownerFaction.name, ownerFaction.startingRep);
									reputation = ownerFaction.startingRep;
								}
								return EnumAttitude.fromRep(reputation).allowsInteraction(EnumInteraction.ATTACK);
							}
						}
						else
						{
							Faction inputFaction = manager.getFaction(input);
							if(inputFaction == null)
								return false;
							else
								return ownerFaction.relationWith(inputFaction.name).allowsInteraction(EnumInteraction.ATTACK);
						}
					return false;
				}
			};
		
		this.targetEntitySelector = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(targetPredicate);
	}
	
	public boolean canUse()
	{
		if(this.targetChance > 0 && entity.getRandom().nextInt(targetChance) != 0)
			return false;
		
		AABB targetArea = entity.getBoundingBox().inflate(getFollowDistance(), 4D, getFollowDistance());
		this.nearestTarget = entity.getLevel().getNearestEntity(LivingEntity.class, this.targetEntitySelector, entity, entity.getX(), entity.getEyeY(), entity.getZ(), targetArea);
		return this.nearestTarget != null;
	}
	
	public void start()
	{
		this.entity.setTarget(this.nearestTarget);
		super.start();
	}
}
