package com.lying.variousoddities.entity.ai;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class MovementControllerGhastling extends MovementController
{
	private final MobEntity parentEntity;
	private int courseChangeCooldown;
	
	public MovementControllerGhastling(MobEntity ghast)
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
				this.courseChangeCooldown += this.parentEntity.getRNG().nextInt(5) + 2;
				Vector3d course = new Vector3d(this.posX - this.parentEntity.getPosX(), this.posY - this.parentEntity.getPosY(), this.posZ - this.parentEntity.getPosZ());
				double dist = course.length();
				course = course.normalize();
				if(collides(course, MathHelper.ceil(dist)))
					this.parentEntity.setMotion(this.parentEntity.getMotion().add(course.scale(0.1D)));
				else
					this.action = MovementController.Action.WAIT;
			}
	}
	
	private boolean collides(Vector3d course, int distance)
	{
		AxisAlignedBB entityBB = this.parentEntity.getBoundingBox();
		for(int i=1; i<distance; ++i)
		{
			entityBB = entityBB.offset(course);
			if(!this.parentEntity.getEntityWorld().hasNoCollisions(this.parentEntity, entityBB))
				return false;
		}
		
		return true;
	}
}
