package com.lying.variousoddities.entity.passive;

import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.entity.AbstractRat;
import com.lying.variousoddities.entity.ai.EntityAIAvoidPlayer;
import com.lying.variousoddities.entity.ai.hostile.EntityAIRatFollowGiant;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.entity.ai.hostile.EntityAIRatAvoid;

import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public class EntityRat extends AbstractRat
{
	public static final DataParameter<Boolean> MINION = EntityDataManager.<Boolean>createKey(EntityRat.class, DataSerializers.BOOLEAN);
	
	public EntityRat(EntityType<? extends EntityRat> type, World worldIn)
	{
		super(type, worldIn, 0);
	}
	
    public static AttributeModifierMap.MutableAttribute getAttributes()
    {
        return MobEntity.func_233666_p_()
        		.createMutableAttribute(Attributes.MAX_HEALTH, 9.0D)
        		.createMutableAttribute(Attributes.ARMOR, 4.0D)
        		.createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.265D)
        		.createMutableAttribute(Attributes.ATTACK_DAMAGE, 3.5D);
    }
    
	protected void registerData()
	{
		super.registerData();
		getDataManager().register(MINION, false);
	}
    
    public void registerGoals()
    {
    	super.registerGoals();
    	this.goalSelector.addGoal(3, new EntityAIAvoidPlayer(this, 6.0F, 1.0D, 1.2D));
	    this.goalSelector.addGoal(3, new EntityAIRatAvoid<>(this, OcelotEntity.class, 6.0F, 1.0D, 1.2D));
	    this.goalSelector.addGoal(3, new EntityAIRatAvoid<>(this, CatEntity.class, 6.0F, 1.0D, 1.2D));
	    this.goalSelector.addGoal(4, new EntityAIRatFollowGiant(this, 1.25D));
	    
	    if(ConfigVO.MOBS.aiSettings.isOddityAIEnabled(VOEntities.RAT))
		    this.targetSelector.addGoal(1, new HurtByTargetGoal(this, AbstractRat.class, OcelotEntity.class, CatEntity.class));
    }
    
    public boolean isNoDespawnRequired(){ return !isMinion() || super.isNoDespawnRequired(); }
    
    /**
     * 1 in 20 rats are plague rats and deal poison damage.<br>
     * The rest are black, brown, or white.
     */
	public int getRandomBreed()
	{
		return getRNG().nextInt(20) == 0 ? EnumRatBreed.getID(EnumRatBreed.PLAGUE) : getRNG().nextInt(3);
	}
	
	protected EntitySize getStandingSize()
	{
		return EntitySize.fixed(0.3F, 0.5F);
	}
	
	protected EntitySize getCrouchingSize()
	{
		return EntitySize.fixed(0.3F, 0.2F);
	}
	
    protected float getSoundPitch()
    {
    	return super.getSoundPitch() * 1.25F;
    }
    
    public void setMinion(boolean par1Bool){ getDataManager().set(MINION, par1Bool); }
    public boolean isMinion(){ return getDataManager().get(MINION).booleanValue(); }
    
    public void writeAdditional(CompoundNBT compound)
    {
    	super.writeAdditional(compound);
    	if(isMinion())
    		compound.putBoolean("Minion", isMinion());
    }
    
    public void readAdditional(CompoundNBT compound)
    {
    	super.readAdditional(compound);
    	if(compound.contains("Minion") && compound.getBoolean("Minion"))
    		setMinion(true);
    }
}
