package com.lying.variousoddities.entity.mount;

import javax.annotation.Nullable;

import com.lying.variousoddities.entity.AbstractGoblinWolf;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class EntityWarg extends AbstractGoblinWolf
{
	public static final DataParameter<Integer>	COLOR		= EntityDataManager.<Integer>createKey(EntityWarg.class, DataSerializers.VARINT);
	public static final DataParameter<Boolean>	SITTING		= EntityDataManager.<Boolean>createKey(EntityWarg.class, DataSerializers.BOOLEAN);
    
	public EntityWarg(EntityType<? extends EntityWarg> type, World worldIn)
	{
		super(type, worldIn);
	}
	
	protected void registerData()
	{
		super.registerData();
		getDataManager().register(COLOR, 0);
		
		getDataManager().register(SITTING, false);
	}
	
    public static AttributeModifierMap.MutableAttribute getAttributes()
    {
        return MobEntity.func_233666_p_()
        		.createMutableAttribute(Attributes.MAX_HEALTH, 30.0D)
        		.createMutableAttribute(Attributes.ARMOR, 4.0D)
        		.createMutableAttribute(Attributes.MOVEMENT_SPEED, (double)0.3F)
        		.createMutableAttribute(Attributes.ATTACK_DAMAGE, 10.0D);
    }
    
    public int getColor(){ return getDataManager().get(COLOR).intValue(); }
    public void setColor(int par1Int){ getDataManager().set(COLOR, MathHelper.clamp(par1Int, 0, 2)); }
    
    public boolean isSitting(){ return getDataManager().get(SITTING); }
    public void setSitting(boolean sitting)
    {
    	getDataManager().set(SITTING, sitting);
//    	if(this.sitTask != null)
//		{
////    		this.sitTask.setSitting(sitting);
//	    	if(sitting)
//	    	{
//		        this.isJumping = false;
//		        this.navigator.clearPath();
//		        this.setAttackTarget((EntityLivingBase)null);
//	    	}
//		}
//    	else
//    	{
////    		this.sitTask = new EntityAISit(this, false);
//    		setSitting(sitting);
//    	}
    }
	
	public AgeableEntity func_241840_a(ServerWorld arg0, AgeableEntity arg1)
	{
		return null;
	}
    
    @Nullable
    public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag)
    {
    	setColor(getRNG().nextInt(3));
		return spawnDataIn;
    }
}
