package com.lying.variousoddities.entity.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MovementControllerGhastling extends MoveControl
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
		this.operation = Operation.WAIT;
	}
	
	public void tick()
	{
		if(this.operation == MoveControl.Operation.MOVE_TO)
			if(this.courseChangeCooldown-- <= 0)
			{
				this.courseChangeCooldown += this.parentEntity.getRandom().nextInt(5) + 2;
				Vec3 course = new Vec3(this.wantedX - this.parentEntity.getX(), this.wantedY - this.parentEntity.getY(), this.wantedZ - this.parentEntity.getZ());
				double dist = course.length();
				course = course.normalize();
				if(collides(course, Mth.ceil(dist)))
					this.parentEntity.setDeltaMovement(this.parentEntity.getDeltaMovement().add(course.scale(0.1D)));
				else
					this.operation = MoveControl.Operation.WAIT;
			}
	}
	
	private boolean collides(Vec3 course, int distance)
	{
		AABB entityBB = this.parentEntity.getBoundingBox();
		for(int i=1; i<distance; ++i)
		{
			entityBB = entityBB.move(course);
			if(!this.parentEntity.getLevel().noCollision(this.parentEntity, entityBB))
				return false;
		}
		
		return true;
	}
}
