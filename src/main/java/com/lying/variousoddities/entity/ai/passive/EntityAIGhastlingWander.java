package com.lying.variousoddities.entity.ai.passive;

import java.util.EnumSet;
import java.util.Random;

import com.lying.variousoddities.entity.ai.MovementControllerGhastling;
import com.lying.variousoddities.entity.passive.EntityGhastling;
import com.lying.variousoddities.entity.passive.EntityGhastling.Emotion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class EntityAIGhastlingWander extends Goal
{
	private final World world;
	private final EntityGhastling creature;
	private MovementControllerGhastling controller;
	
	private final double RANGE = 3D;
	private final double RANGE_MAX = 8D * 8D;
	
	private final float probability;
	
	public EntityAIGhastlingWander(EntityGhastling entity, float probIn)
	{
		this.world = entity.getEntityWorld();
		this.creature = entity;
		this.probability = MathHelper.clamp(probIn, 0F, 1F);
		setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
	}
	
	public boolean shouldExecute()
	{
		if(this.creature.getEmotion() == Emotion.SLEEP || this.creature.isSitting())
			return false;
		
		if(this.creature.getRNG().nextFloat() <= probability)
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
	
	public boolean shouldContinueExecuting(){ return false; }
	
	public void startExecuting()
	{
		LivingEntity owner = this.creature.isTamed() ? this.creature.getOwner() : null;
		double ownerDist =  owner != null ? owner.getDistanceSq(this.creature) : Double.MAX_VALUE;
		
		if(owner != null && ownerDist > (16D * 16D))
			tryTeleportToOwner();
		else if(this.creature.getRNG().nextInt(16) == 0)
		{
			int attempts = 50;
			Vector3d dest = null;
			do
			{
				dest = getRandomPosition();
				
				AxisAlignedBB bounds = this.creature.getBoundingBox().offset(dest);
				if(!this.world.hasNoCollisions(this.creature, bounds))
					dest = null;
				else if(owner != null && owner.getDistanceSq(dest) > RANGE_MAX && owner.getDistanceSq(dest) > (ownerDist * 0.75D))
					dest = null;
			}
			while(dest == null && attempts-- > 0);
			
			if(dest != null)
				setMoveTo(dest.x, dest.y, dest.z);
		}
		else
			this.controller.clearMotion();;
	}
	
	private Vector3d getRandomPosition()
	{
		Random rand = this.creature.getRNG();
		double x = this.creature.getPosX() + ((rand.nextDouble() * 2D - 1D) * RANGE);
		double y = this.creature.getPosY() + ((rand.nextDouble() * 2D - 1D) * RANGE);
		double z = this.creature.getPosZ() + ((rand.nextDouble() * 2D - 1D) * RANGE);
		return new Vector3d(x, y, z);
	}
	
	private void setMoveTo(double x, double y, double z)
	{
		this.controller.setMoveTo(x, y, z, this.creature.getAttributeValue(Attributes.FLYING_SPEED) * 0.25D);
	}
	
	private void tryTeleportToOwner()
	{
		Random rand = this.creature.getRNG();
		BlockPos dest = this.creature.getOwner().getPosition();
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
		if(this.creature.getOwner().getDistanceSq(x, y, z) < (2D * 2D))
			return false;
		if(!canTeleportTo(new BlockPos(x, y, z)))
			return false;
		this.creature.setLocationAndAngles(x + 0.5D, y + 0.5D, z + 0.5D, this.creature.rotationYaw, this.creature.rotationPitch);
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
			BlockPos blockpos = pos.subtract(this.creature.getPosition());
			return this.world.hasNoCollisions(this.creature, this.creature.getBoundingBox().offset(blockpos));
		}
	}
}
