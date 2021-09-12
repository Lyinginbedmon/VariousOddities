package com.lying.variousoddities.entity.wip;

import java.util.Random;

import com.lying.variousoddities.entity.EntityOddityAgeable;
import com.lying.variousoddities.entity.passive.IChangeling;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class EntityChangeling extends EntityOddityAgeable implements IChangeling
{
    public static final DataParameter<Integer>	FLAP_TIME	= EntityDataManager.<Integer>createKey(EntityChangeling.class, DataSerializers.VARINT);
    
	public EntityChangeling(EntityType<? extends AgeableEntity> type, World worldIn)
	{
		super(type, worldIn);
	}
	
	protected void registerData()
	{
		super.registerData();
		getDataManager().register(FLAP_TIME, 0);
	}
	
    public static boolean canSpawnAt(EntityType<? extends MobEntity> animal, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
        return CreatureEntity.canSpawnOn(animal, world, reason, pos, random);
    }
	
	public AgeableEntity func_241840_a(ServerWorld p_241840_1_, AgeableEntity p_241840_2_)
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
			if(getRNG().nextInt(80) == 0)
				getDataManager().set(FLAP_TIME, Reference.Values.TICKS_PER_SECOND);
		}
		else
			getDataManager().set(FLAP_TIME, getFlappingTime() - 1);
	}
    
    public int getFlappingTime(){ return getDataManager().get(FLAP_TIME).intValue(); }
}
