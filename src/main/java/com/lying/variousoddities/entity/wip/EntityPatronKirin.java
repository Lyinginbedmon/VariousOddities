package com.lying.variousoddities.entity.wip;

import com.lying.variousoddities.entity.EntityOddityAgeable;

import net.minecraft.entity.IChargeableMob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(
   value = Dist.CLIENT,
   _interface = IChargeableMob.class
)
public class EntityPatronKirin extends EntityOddityAgeable implements IChargeableMob
{
	public EntityPatronKirin(EntityType<? extends EntityOddityAgeable> type, Level worldIn)
	{
		super(type, worldIn);
	}
    
	public boolean isCharged(){ return true; }
    
    public boolean isNoDespawnRequired(){ return true; }
	
	public AgeableMob getBreedOffspring(ServerLevel p_241840_1_, AgeableMob p_241840_2_)
	{
		return null;
	}
}
