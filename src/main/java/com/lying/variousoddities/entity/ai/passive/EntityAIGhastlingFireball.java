package com.lying.variousoddities.entity.ai.passive;

import java.util.EnumSet;

import com.lying.variousoddities.entity.passive.EntityGhastling;
import com.lying.variousoddities.entity.passive.EntityGhastling.Emotion;
import com.lying.variousoddities.entity.projectile.EntityFireballGhastling;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class EntityAIGhastlingFireball extends Goal
{
	private final EntityGhastling theGhastling;
	public int attackTimer = 0;
	
	public EntityAIGhastlingFireball(EntityGhastling entity)
	{
		this.theGhastling = entity;
		this.setMutexFlags(EnumSet.of(Flag.LOOK));
	}
	
	public boolean shouldExecute()
	{
		return this.theGhastling.getAttackTarget() != null && this.theGhastling.getAttackTarget().isAlive();
	}
	
	public void startExecuting()
	{
		this.attackTimer = 0;
		this.theGhastling.setEmotion(Emotion.ANGRY);
	}
	
	public void resetTask()
	{
		this.theGhastling.setEmotion(Emotion.NEUTRAL);
	}
	
	public void tick()
	{
		LivingEntity target = this.theGhastling.getAttackTarget();
		this.theGhastling.getLookController().setLookPositionWithEntity(target, 30F, 30F);
		if(target.getDistanceSq(theGhastling) < 4096D && this.theGhastling.canEntityBeSeen(target))
		{
			World world = this.theGhastling.getEntityWorld();
			++this.attackTimer;
			if(this.attackTimer == 10 && !this.theGhastling.isSilent())
				world.playEvent((PlayerEntity)null, 1015, this.theGhastling.getPosition(), 0);
			
			if(this.attackTimer == 20)
			{
				Vector3d dirToTarget = new Vector3d(target.getPosX() - this.theGhastling.getPosX(), target.getPosYHeight(0.5D) - this.theGhastling.getPosYHeight(0.5D), target.getPosZ() - this.theGhastling.getPosZ()).normalize();
				double moveX = dirToTarget.x * 4D;
				double moveY = dirToTarget.y * 4D;
				double moveZ = dirToTarget.z * 4D; 
				if(!this.theGhastling.isSilent())
					world.playEvent((PlayerEntity)null, 1016, this.theGhastling.getPosition(), 0);
				
				Vector3d direction = this.theGhastling.getLook(1F);
				EntityFireballGhastling fireball = new EntityFireballGhastling(world, this.theGhastling, moveX, moveY, moveZ);
				fireball.setPosition(this.theGhastling.getPosX() + direction.x * this.theGhastling.getWidth(), this.theGhastling.getPosYHeight(0.5D), this.theGhastling.getPosZ() + direction.z * this.theGhastling.getWidth());
				fireball.setShooter(theGhastling);
				world.addEntity(fireball);
				this.attackTimer -= 40;
				this.theGhastling.setEmotion(Emotion.ANGRY);
			}
		}
		else if(this.attackTimer > 0)
			--this.attackTimer;
		
		if(this.attackTimer > 10)
			this.theGhastling.setEmotion(Emotion.ANGRY2);
	}
}
