package com.lying.variousoddities.entity.wip;

import com.lying.variousoddities.entity.EntityOddityAgeable;
import com.lying.variousoddities.entity.passive.IChangeling;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class EntityChangeling extends EntityOddityAgeable implements IChangeling
{
    public static final DataParameter<Integer>	FLAP_TIME	= EntityDataManager.<Integer>createKey(EntityChangeling.class, DataSerializers.VARINT);
    
	public EntityChangeling(EntityType<? extends AgeableMob> type, Level worldIn)
	{
		super(type, worldIn);
	}
	
	protected void registerData()
	{
		super.registerData();
		getDataManager().register(FLAP_TIME, 0);
	}
	
	public AgeableMob getBreedOffspring(ServerLevel p_241840_1_, AgeableMob p_241840_2_)
	{
		return null;
	}
	
	public BlockPos getParentHivePos(){ return BlockPos.ZERO; }
	
	public void setParentHive(BlockPos hivePos) { }
	
	public void livingTick()
	{
		super.livingTick();
		
//        if(this.openJawCounter > 0 && ++this.openJawCounter > Reference.Values.TICKS_PER_SECOND)
//        {
//            this.openJawCounter = 0;
//            setJawOpen(false);
//        }
		
		if(!isFlapping())
		{
			if(getRandom().nextInt(80) == 0)
				getDataManager().set(FLAP_TIME, Reference.Values.TICKS_PER_SECOND);
		}
		else
			getDataManager().set(FLAP_TIME, getFlappingTime() - 1);
	}
    
    public int getFlappingTime(){ return getDataManager().get(FLAP_TIME).intValue(); }
}
