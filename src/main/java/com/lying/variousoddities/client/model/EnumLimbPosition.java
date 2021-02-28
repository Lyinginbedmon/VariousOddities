package com.lying.variousoddities.client.model;

import net.minecraft.util.math.vector.Vector3d;

public enum EnumLimbPosition
{
	FRONT(0, 0, -1),
	REAR(0, 0, 1),
	LEFT(1, 0, 0),
	RIGHT(-1, 0, 0),
	
	UP(0, -1, 0),
	DOWN(0, 1, 0),
	
	FRONT_LEFT(LEFT.getX(), 0, FRONT.getZ()),
	FRONT_RIGHT(RIGHT.getX(), 0, FRONT.getZ()),
	REAR_LEFT(LEFT.getX(), 0, REAR.getZ()),
	REAR_RIGHT(RIGHT.getX(), 0, REAR.getZ());
	
	private final float xDirection, yDirection, zDirection;
	private final Vector3d vector;
	
	private EnumLimbPosition(float x, float y, float z)
	{
		xDirection = x;
		yDirection = y;
		zDirection = z;
		
		vector = new Vector3d(x, y, z);
	}
	
	public float getX(){ return xDirection; }
	public float getY(){ return yDirection; }
	public float getZ(){ return zDirection; }
	
	public Vector3d asVector()
	{
		return vector;
	}
}
