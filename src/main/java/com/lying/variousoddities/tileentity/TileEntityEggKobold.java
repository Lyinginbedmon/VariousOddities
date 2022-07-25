package com.lying.variousoddities.tileentity;

import com.lying.variousoddities.entity.passive.EntityKobold;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.init.VOTileEntities;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class TileEntityEggKobold extends TileEntityEgg
{
	public TileEntityEggKobold()
	{
		super(VOTileEntities.EGG_KOBOLD);
	}
	
	public int getInitialHatchTime(){ return Reference.Values.TICKS_PER_MINUTE * 20; }
	public int getHatchingTime(){ return Reference.Values.TICKS_PER_MINUTE; }
	
	public LivingEntity getHatchling(Level worldIn)
	{
		EntityKobold baby = VOEntities.KOBOLD.create(worldIn);
		baby.setAge(-6000);
		return baby;
	}
}
