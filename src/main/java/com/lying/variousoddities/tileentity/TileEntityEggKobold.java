package com.lying.variousoddities.tileentity;

import com.lying.variousoddities.entity.passive.EntityKobold;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.init.VOTileEntities;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class TileEntityEggKobold extends TileEntityEgg
{
	public TileEntityEggKobold()
	{
		super(VOTileEntities.EGG_KOBOLD);
	}
	
	public int getInitialHatchTime(){ return Reference.Values.TICKS_PER_MINUTE * 20; }
	public int getHatchingTime(){ return Reference.Values.TICKS_PER_MINUTE; }
	
	public LivingEntity getHatchling(World worldIn)
	{
		EntityKobold baby = VOEntities.KOBOLD.create(world);
		baby.setGrowingAge(-6000);
		return baby;
	}
}
