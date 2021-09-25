package com.lying.variousoddities.entity.wip;

import java.util.Random;

import com.lying.variousoddities.entity.EntityOddityAgeable;
import com.lying.variousoddities.entity.passive.IChangeling;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.utility.DataHelper;

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

public class EntityPatronWitch extends EntityOddityAgeable implements IChangeling
{
    public static final DataParameter<Byte>		JAW_STYLE	= EntityDataManager.<Byte>createKey(EntityPatronWitch.class, DataSerializers.BYTE);
    public static final DataParameter<Integer>	FLAP_TIME	= EntityDataManager.<Integer>createKey(EntityPatronWitch.class, DataSerializers.VARINT);
    
    public float prevCameraYaw;
    public float cameraYaw;
    /** Previous X position of the player's cape */
    public double prevChasingPosX;
    /** Previous Y position of the player's cape */
    public double prevChasingPosY;
    /** Previous Z position of the player's cape */
    public double prevChasingPosZ;
    /** Current X position of the player's cape */
    public double chasingPosX;
    /** Current Y position of the player's cape */
    public double chasingPosY;
    /** Current Z position of the player's cape */
    public double chasingPosZ;
    
    private int openJawCounter;
    private float jawOpenness;
    private float prevJawOpenness;
    
	public EntityPatronWitch(EntityType<? extends EntityOddityAgeable> type, World worldIn)
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
    
    public void livingTick()
    {
    	super.livingTick();
        if(this.openJawCounter > 0 && ++this.openJawCounter > 20)
        {
            this.openJawCounter = 0;
            setJawOpen(false);
        }
        
        this.prevJawOpenness = this.jawOpenness;
        if(isJawOpen())
        	this.jawOpenness += (1.0F - this.jawOpenness) * 0.1F + 0.05F;
        else
        	this.jawOpenness += (-this.jawOpenness) * 0.1F - 0.05F;
        
        this.jawOpenness = Math.max(0F, Math.min(1F, this.jawOpenness));
        
    	updatePonytail();
		
		if(!isFlapping())
		{
			if(getRNG().nextInt(80) == 0)
				getDataManager().set(FLAP_TIME, Reference.Values.TICKS_PER_SECOND);
		}
		else
			getDataManager().set(FLAP_TIME, getFlappingTime() - 1);
    }
    
    public int getFlappingTime(){ return getDataManager().get(FLAP_TIME).intValue(); }
    
    private void updatePonytail()
    {
        this.prevChasingPosX = this.chasingPosX;
        this.prevChasingPosY = this.chasingPosY;
        this.prevChasingPosZ = this.chasingPosZ;
        double d0 = this.getPosX() - this.chasingPosX;
        double d1 = this.getPosY() - this.chasingPosY;
        double d2 = this.getPosZ() - this.chasingPosZ;
        
        if (d0 > 10.0D)
        {
            this.chasingPosX = this.getPosX();
            this.prevChasingPosX = this.chasingPosX;
        }
        
        if (d2 > 10.0D)
        {
            this.chasingPosZ = this.getPosZ();
            this.prevChasingPosZ = this.chasingPosZ;
        }

        if (d1 > 10.0D)
        {
            this.chasingPosY = this.getPosY();
            this.prevChasingPosY = this.chasingPosY;
        }

        if (d0 < -10.0D)
        {
            this.chasingPosX = this.getPosX();
            this.prevChasingPosX = this.chasingPosX;
        }

        if (d2 < -10.0D)
        {
            this.chasingPosZ = this.getPosZ();
            this.prevChasingPosZ = this.chasingPosZ;
        }

        if (d1 < -10.0D)
        {
            this.chasingPosY = this.getPosY();
            this.prevChasingPosY = this.chasingPosY;
        }
        
        this.chasingPosX += d0 * 0.25D;
        this.chasingPosZ += d2 * 0.25D;
        this.chasingPosY += d1 * 0.25D;
    }
    
	public BlockPos getParentHivePos(){ return BlockPos.ZERO; }
	
	public void setParentHive(BlockPos hivePos){ }
    
	public float getJawState(float partialTicks)
	{
        return this.prevJawOpenness + (this.jawOpenness - this.prevJawOpenness) * partialTicks;
	}
    
    public void openJaw()
    {
        if(!this.world.isRemote)
        {
            this.openJawCounter = 1;
            setJawOpen(true);
		    DataHelper.Booleans.setBooleanByte(getDataManager(), getRNG().nextInt(30) == 0, JAW_STYLE);
        }
    }
    
    public void setJawOpen(boolean par1Bool)
    {
    	super.setJawOpen(par1Bool);
    	if(!par1Bool)
		    DataHelper.Booleans.setBooleanByte(getDataManager(), false, JAW_STYLE);
    }
    
    public boolean isJawSplit()
    {
    	return DataHelper.Booleans.getBooleanByte(getDataManager(), JAW_STYLE);
    }
}
