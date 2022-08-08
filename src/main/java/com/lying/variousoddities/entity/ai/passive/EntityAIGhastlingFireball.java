package com.lying.variousoddities.entity.ai.passive;

import java.util.EnumSet;

import com.lying.variousoddities.entity.passive.EntityGhastling;
import com.lying.variousoddities.entity.passive.EntityGhastling.Emotion;
import com.lying.variousoddities.entity.projectile.EntityFireballGhastling;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityAIGhastlingFireball extends Goal
{
	private final EntityGhastling theGhastling;
	public int attackTimer = 0;
	
	public EntityAIGhastlingFireball(EntityGhastling entity)
	{
		this.theGhastling = entity;
		this.setFlags(EnumSet.of(Flag.LOOK));
	}
	
	public boolean canUse()
	{
		return this.theGhastling.getTarget() != null && this.theGhastling.getTarget().isAlive();
	}
	
	public void startg()
	{
		this.attackTimer = 0;
		this.theGhastling.setEmotion(Emotion.ANGRY);
	}
	
	public void stop()
	{
		this.theGhastling.setEmotion(Emotion.NEUTRAL);
	}
	
	public void tick()
	{
		LivingEntity target = this.theGhastling.getTarget();
		this.theGhastling.getLookController().setLookPositionWithEntity(target, 30F, 30F);
		if(target.distanceToSqr(theGhastling) < 4096D && this.theGhastling.canEntityBeSeen(target))
		{
			Level world = this.theGhastling.getLevel();
			++this.attackTimer;
			if(this.attackTimer == 10 && !this.theGhastling.isSilent())
				world.playEvent((Player)null, 1015, this.theGhastling.getPosition(), 0);
			
			if(this.attackTimer == 20)
			{
				Vec3 dirToTarget = new Vec3(target.getX() - this.theGhastling.getX(), target.getY(0.5D) - this.theGhastling.getY(0.5D), target.getZ() - this.theGhastling.getZ()).normalize();
				double moveX = dirToTarget.x * 4D;
				double moveY = dirToTarget.y * 4D;
				double moveZ = dirToTarget.z * 4D; 
				if(!this.theGhastling.isSilent())
					world.playEvent((Player)null, 1016, this.theGhastling.getPosition(), 0);
				
				Vec3 direction = this.theGhastling.getLook(1F);
				EntityFireballGhastling fireball = new EntityFireballGhastling(world, this.theGhastling, moveX, moveY, moveZ);
				fireball.setPosition(this.theGhastling.getX() + direction.x * this.theGhastling.getBbWidth(), this.theGhastling.getY(0.5D), this.theGhastling.getZ() + direction.z * this.theGhastling.getBbWidth());
				fireball.setShooter(theGhastling);
				world.addFreshEntity(fireball);
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
