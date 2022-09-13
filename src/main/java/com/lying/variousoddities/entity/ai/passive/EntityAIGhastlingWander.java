package com.lying.variousoddities.entity.ai.passive;

import java.util.EnumSet;

import com.lying.variousoddities.entity.ai.MovementControllerGhastling;
import com.lying.variousoddities.entity.passive.EntityGhastling;
import com.lying.variousoddities.entity.passive.EntityGhastling.Emotion;

import net.minecraft.core.BlockPos;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityAIGhastlingWander extends Goal
{
	private final Level world;
	private final EntityGhastling creature;
	private MovementControllerGhastling controller;
	
	private final double RANGE = 3D;
	private final double RANGE_MAX = 8D * 8D;
	
	private final float probability;
	
	public EntityAIGhastlingWander(EntityGhastling entity, float probIn)
	{
		this.world = entity.getLevel();
		this.creature = entity;
		this.probability = Mth.clamp(probIn, 0F, 1F);
		setFlags(EnumSet.of(Goal.Flag.MOVE));
	}
	
	public boolean canUse()
	{
		if(this.creature.getEmotion() == Emotion.SLEEP || this.creature.isOrderedToSit())
			return false;
		
		if(this.creature.getRandom().nextFloat() <= probability)
			return false;
		
		MovementController moveHelper = this.creature.getMoveHelper();
		if(moveHelper instanceof MovementControllerGhastling)
		{
			this.controller = (MovementControllerGhastling)moveHelper;
			if(!controller.isUpdating())
				return true;
			else
			{
				double dist = new BlockPos(controller.getX(), controller.getY(), controller.getZ()).distanceSq(creature.getPosition());
				return dist < 1D || dist > RANGE_MAX;
			}
		}
		return false;
	}
	
	public boolean canContinueToUse(){ return false; }
	
	public void start()
	{
		LivingEntity owner = this.creature.isTame() ? this.creature.getOwner() : null;
		double ownerDist =  owner != null ? owner.distanceToSqr(this.creature) : Double.MAX_VALUE;
		
		if(owner != null && ownerDist > (16D * 16D))
			tryTeleportToOwner();
		else if(this.creature.getRandom().nextInt(16) == 0)
		{
			int attempts = 50;
			Vec3 dest = null;
			do
			{
				dest = getRandomPosition();
				
				AABB bounds = this.creature.getBoundingBox().move(dest);
				if(!this.world.noCollision(this.creature, bounds))
					dest = null;
				else if(owner != null && owner.distanceToSqr(dest) > RANGE_MAX && owner.distanceToSqr(dest) > (ownerDist * 0.75D))
					dest = null;
			}
			while(dest == null && attempts-- > 0);
			
			if(dest != null)
				setMoveTo(dest.x, dest.y, dest.z);
		}
		else
			this.controller.clearMotion();;
	}
	
	private Vec3 getRandomPosition()
	{
		RandomSource rand = this.creature.getRandom();
		double x = this.creature.getX() + ((rand.nextDouble() * 2D - 1D) * RANGE);
		double y = this.creature.getY() + ((rand.nextDouble() * 2D - 1D) * RANGE);
		double z = this.creature.getZ() + ((rand.nextDouble() * 2D - 1D) * RANGE);
		return new Vec3(x, y, z);
	}
	
	private void setMoveTo(double x, double y, double z)
	{
		this.controller.setMoveTo(x, y, z, this.creature.getAttributeValue(Attributes.FLYING_SPEED) * 0.25D);
	}
	
	private void tryTeleportToOwner()
	{
		RandomSource rand = this.creature.getRandom();
		BlockPos dest = this.creature.getOwner().blockPosition();
		for(int i=0; i<10; i++)
		{
			int x = (int)(((rand.nextDouble() - 0.5D) * 2D) * 3);
			int y = (int)(((rand.nextDouble() - 0.5D) * 2D) * 1);
			int z = (int)(((rand.nextDouble() - 0.5D) * 2D) * 3);
			if(tryTeleportToPos(dest.getX() + x, dest.getY() + y, dest.getZ() + z))
				return;
		}
	}
	
	private boolean tryTeleportToPos(int x, int y, int z)
	{
		if(this.creature.getOwner().distanceToSqr(x, y, z) < (2D * 2D))
			return false;
		if(!canTeleportTo(new BlockPos(x, y, z)))
			return false;
		this.creature.absMoveTo(x + 0.5D, y + 0.5D, z + 0.5D, this.creature.getYRot(), this.creature.getXRot());
		this.controller.clearMotion();
		return true;
	}
	
	private boolean canTeleportTo(BlockPos pos)
	{
		PathNodeType pathnodetype = WalkNodeProcessor.func_237231_a_(this.world, pos.toMutable());
		if(pathnodetype != PathNodeType.WALKABLE)
			return false;
		else
		{
			BlockPos blockpos = pos.subtract(this.creature.blockPosition());
			return this.world.noCollision(this.creature, this.creature.getBoundingBox().move(blockpos));
		}
	}
}
