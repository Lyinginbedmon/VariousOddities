package com.lying.variousoddities.entity.ai.passive;

import java.util.Random;

import com.lying.variousoddities.entity.passive.EntityGhastling;
import com.lying.variousoddities.entity.passive.EntityGhastling.Emotion;

import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class EntityAIGhastlingWander extends Goal
{
	private final EntityGhastling creature;
	
	private final int probability;
	private final double range = 4D;
	
	public EntityAIGhastlingWander(EntityGhastling entity, int probability)
	{
		this.creature = entity;
		this.probability = probability;
	}
	
	public boolean shouldExecute()
	{
		if(this.creature.getEmotion() != Emotion.SLEEP)
			return false;
		
		if(this.creature.getRNG().nextInt(probability) != 0)
			return false;
		
		if(this.creature.isTamed() && this.creature.getOwner() != null)
			if(this.creature.getOwner().getDistance(this.creature) > 16D)
				return true;
		
		MovementController controller = this.creature.getMoveHelper();
		if(!controller.isUpdating())
			return true;
		else
		{
			double dist = new BlockPos(controller.getX(), controller.getY(), controller.getZ()).distanceSq(creature.getPosition());
			return dist < 1D || dist > 3600D;
		}
	}
	
	public boolean shouldContinueExecuting(){ return false; }
	
	public void startExecuting()
	{
		if(this.creature.isTamed() && this.creature.getOwner() != null)
		{
			Vector3d ownerPos = this.creature.getOwner().getPositionVec().add(0D, this.creature.getHeight() * 0.5D, 0D);
			this.creature.getMoveHelper().setMoveTo(ownerPos.x, ownerPos.y, ownerPos.z, this.creature.getAttributeValue(Attributes.FLYING_SPEED));
		}
		else
		{
			Random rand = this.creature.getRNG();
			double x = this.creature.getPosX() + ((rand.nextDouble() * 2D - 1D) * range);
			double y = this.creature.getPosY() + ((rand.nextDouble() * 2D - 1D) * range);
			double z = this.creature.getPosZ() + ((rand.nextDouble() * 2D - 1D) * range);
			this.creature.getMoveHelper().setMoveTo(x, y, z, this.creature.getAttributeValue(Attributes.FLYING_SPEED));
		}
	}
}
