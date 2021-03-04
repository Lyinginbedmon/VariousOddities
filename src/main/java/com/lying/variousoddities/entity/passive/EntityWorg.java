package com.lying.variousoddities.entity.passive;

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

public class EntityWorg extends AbstractGoblinWolf
{
	public static final DataParameter<Integer>	COLOR		= EntityDataManager.<Integer>createKey(EntityWorg.class, DataSerializers.VARINT);
    
	public EntityWorg(EntityType<? extends EntityWorg> p_i48574_1_, World p_i48574_2_)
	{
		super(p_i48574_1_, p_i48574_2_);
	}
	
	protected void registerData()
	{
		super.registerData();
		getDataManager().register(COLOR, 0);
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
    
    /**
     * Returns the volume for the sounds this mob makes.
     */
    protected float getSoundVolume()
    {
        return 0.4F;
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