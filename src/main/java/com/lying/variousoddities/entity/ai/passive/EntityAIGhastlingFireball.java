package com.lying.variousoddities.entity.ai.passive;

import java.util.EnumSet;

import com.lying.variousoddities.entity.passive.EntityGhastling;
import com.lying.variousoddities.entity.passive.EntityGhastling.Emotion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class EntityAIGhastlingFireball extends Goal
{
	private final EntityGhastling parentEntity;
	public int attackTimer = 0;
	
	public EntityAIGhastlingFireball(EntityGhastling entity)
	{
		this.parentEntity = entity;
		this.setMutexFlags(EnumSet.of(Flag.LOOK));
	}
	
	public boolean shouldExecute()
	{
		return this.parentEntity.getAttackTarget() != null && this.parentEntity.getAttackTarget().isAlive();
	}
	
	public void startExecuting()
	{
		this.attackTimer = 0;
	}
	
	public void resetTask()
	{
		this.parentEntity.setEmotion(Emotion.NEUTRAL);
	}
	
	public void tick()
	{
		LivingEntity target = this.parentEntity.getAttackTarget();
		this.parentEntity.getLookController().setLookPositionWithEntity(target, 30F, 30F);
		if(target.getDistanceSq(parentEntity) < 4096D && this.parentEntity.canEntityBeSeen(target))
		{
			World world = this.parentEntity.getEntityWorld();
			++this.attackTimer;
			if(this.attackTimer == 10 && !this.parentEntity.isSilent())
				world.playEvent((PlayerEntity)null, 1015, this.parentEntity.getPosition(), 0);
			
			if(this.attackTimer == 20)
			{
				Vector3d dirToTarget = new Vector3d(target.getPosX() - this.parentEntity.getPosX(), target.getPosYHeight(0.5D) - this.parentEntity.getPosYHeight(0.5D), target.getPosZ() - this.parentEntity.getPosZ()).normalize();
				double moveX = dirToTarget.x * 4D;
				double moveY = dirToTarget.y * 4D;
				double moveZ = dirToTarget.z * 4D; 
				if(!this.parentEntity.isSilent())
					world.playEvent((PlayerEntity)null, 1016, this.parentEntity.getPosition(), 0);
				
				Vector3d direction = this.parentEntity.getLook(1F);
				SmallFireballEntity fireball = new SmallFireballEntity(world, this.parentEntity, moveX, moveY, moveZ);
				fireball.setPosition(this.parentEntity.getPosX() + direction.x * this.parentEntity.getWidth(), this.parentEntity.getPosYHeight(0.5D) + 0.5D, this.parentEntity.getPosZ() + direction.z * this.parentEntity.getWidth());
				fireball.setShooter(parentEntity);
				world.addEntity(fireball);
				this.attackTimer -= 40;
			}
		}
		else if(this.attackTimer > 0)
			--this.attackTimer;
		
		if(this.attackTimer > 10)
			this.parentEntity.setEmotion(Emotion.ANGRY2);
		else
			this.parentEntity.setEmotion(Emotion.ANGRY);
	}
}
