package com.lying.variousoddities.api.event;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;

public class FireworkExplosionEvent extends Event
{
	private final Level world;
	private final double posX, posY, posZ;
	private final CompoundTag nbt;
	
	public FireworkExplosionEvent(Level worldIn, double x, double y, double z, @Nullable CompoundTag dataIn)
	{
		this.world = worldIn;
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.nbt = dataIn == null ? new CompoundTag() : dataIn;
	}
	
	public Level world(){ return this.world; }
	public Vec3 position(){ return new Vec3(this.posX, this.posY, this.posZ); }
	
	public CompoundTag fireworkData(){ return this.nbt.copy(); }
}
