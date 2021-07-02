package com.lying.variousoddities.api.event;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Event;

public class FireworkExplosionEvent extends Event
{
	private final World world;
	private final double posX, posY, posZ;
	private final CompoundNBT nbt;
	
	public FireworkExplosionEvent(World worldIn, double x, double y, double z, @Nullable CompoundNBT dataIn)
	{
		this.world = worldIn;
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.nbt = dataIn == null ? new CompoundNBT() : dataIn;
	}
	
	public World world(){ return this.world; }
	public Vector3d position(){ return new Vector3d(this.posX, this.posY, this.posZ); }
	
	public CompoundNBT fireworkData(){ return this.nbt.copy(); }
}
