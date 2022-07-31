package com.lying.variousoddities.entity.wip;

import com.lying.variousoddities.entity.EntityOddityAgeable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.level.Level;

public class EntityPatronKirin extends EntityOddityAgeable implements PowerableMob
{
	public EntityPatronKirin(EntityType<? extends EntityOddityAgeable> type, Level worldIn)
	{
		super(type, worldIn);
	}
    
	public boolean isPowered(){ return true; }
    
    public boolean isNoDespawnRequired(){ return true; }
	
	public AgeableMob getBreedOffspring(ServerLevel p_241840_1_, AgeableMob p_241840_2_)
	{
		return null;
	}
}
