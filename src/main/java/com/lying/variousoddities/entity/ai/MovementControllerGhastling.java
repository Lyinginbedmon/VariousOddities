package com.lying.variousoddities.entity.ai;

import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MovementControllerGhastling extends MovementController
{
	private final Mob parentEntity;
	private int courseChangeCooldown;
	
	public MovementControllerGhastling(Mob ghast)
	{
		super(ghast);
		this.parentEntity = ghast;
	}
	
	public void clearMotion()
	{
		this.action = Action.WAIT;
	}
	
	public void tick()
	{
		if(this.action == MovementController.Action.MOVE_TO)
			if(this.courseChangeCooldown-- <= 0)
			{
				this.courseChangeCooldown += this.parentEntity.getRandom().nextInt(5) + 2;
				Vec3 course = new Vec3(this.posX - this.parentEntity.getX(), this.posY - this.parentEntity.getY(), this.posZ - this.parentEntity.getZ());
				double dist = course.length();
				course = course.normalize();
				if(collides(course, Mth.ceil(dist)))
					this.parentEntity.setMotion(this.parentEntity.getMotion().add(course.scale(0.1D)));
				else
					this.action = MovementController.Action.WAIT;
			}
	}
	
	private boolean collides(Vec3 course, int distance)
	{
		AABB entityBB = this.parentEntity.getBoundingBox();
		for(int i=1; i<distance; ++i)
		{
			entityBB = entityBB.move(course);
			if(!this.parentEntity.getLevel().hasNoCollisions(this.parentEntity, entityBB))
				return false;
		}
		
		return true;
	}
}
