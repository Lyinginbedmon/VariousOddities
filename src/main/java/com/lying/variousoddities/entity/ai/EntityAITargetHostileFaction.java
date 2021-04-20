package com.lying.variousoddities.entity.ai;

import java.util.EnumSet;

import com.google.common.base.Predicate;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.faction.FactionReputation.EnumAttitude;
import com.lying.variousoddities.faction.FactionReputation.EnumInteraction;
import com.lying.variousoddities.world.savedata.FactionManager;
import com.lying.variousoddities.world.savedata.FactionManager.Faction;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class EntityAITargetHostileFaction extends TargetGoal
{
	private final MobEntity entity;
	private final int targetChance;
	
	private final EntityPredicate targetEntitySelector;
	
	private LivingEntity nearestTarget = null;
	
	public EntityAITargetHostileFaction(MobEntity goalOwnerIn, boolean checkSight)
	{
		this(goalOwnerIn, checkSight, false);
	}
	
	public EntityAITargetHostileFaction(MobEntity goalOwnerIn, boolean checkSight, boolean nearbyOnlyIn)
	{
		this(goalOwnerIn, 10, checkSight, nearbyOnlyIn);
	}
	
	public EntityAITargetHostileFaction(MobEntity goalOwnerIn, int targetChanceIn, boolean checkSight, boolean nearbyOnlyIn)
	{
		super(goalOwnerIn, checkSight, nearbyOnlyIn);
		this.entity = goalOwnerIn;
		this.targetChance = targetChanceIn;
		this.setMutexFlags(EnumSet.of(Goal.Flag.TARGET));
		
		Predicate<LivingEntity> targetPredicate = new Predicate<LivingEntity>()
			{
				public boolean apply(LivingEntity input)
				{
					FactionManager manager = FactionManager.get(input.getEntityWorld());
					Faction ownerFaction = manager.getFaction(entity);
					if(ownerFaction != null)
						if(input.getType() == EntityType.PLAYER)
						{
							PlayerData data = PlayerData.forPlayer((PlayerEntity)input);
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
		
	    this.targetEntitySelector = (new EntityPredicate()).setDistance(this.getTargetDistance()).setCustomPredicate(targetPredicate);
	}
	
	public boolean shouldExecute()
	{
		if(this.targetChance > 0 && entity.getRNG().nextInt(targetChance) != 0)
			return false;
		
		AxisAlignedBB targetArea = entity.getBoundingBox().grow(getTargetDistance(), 4D, getTargetDistance());
		this.nearestTarget = entity.getEntityWorld().func_225318_b(LivingEntity.class, this.targetEntitySelector, entity, entity.getPosX(), entity.getPosYEye(), entity.getPosZ(), targetArea);
		return this.nearestTarget != null;
	}
	
	public void startExecuting()
	{
		this.entity.setAttackTarget(this.nearestTarget);
		super.startExecuting();
	}
}
