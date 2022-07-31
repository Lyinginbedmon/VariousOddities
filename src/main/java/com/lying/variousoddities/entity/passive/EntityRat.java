package com.lying.variousoddities.entity.passive;

import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.entity.AbstractRat;
import com.lying.variousoddities.entity.ai.EntityAIAvoidPlayer;
import com.lying.variousoddities.entity.ai.hostile.EntityAIRatAvoid;
import com.lying.variousoddities.entity.ai.hostile.EntityAIRatFollowGiant;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.level.Level;

public class EntityRat extends AbstractRat
{
	public static final DataParameter<Boolean> MINION = EntityDataManager.<Boolean>createKey(EntityRat.class, DataSerializers.BOOLEAN);
	
	public EntityRat(EntityType<? extends EntityRat> type, Level worldIn)
	{
		super(type, worldIn, 0);
	}

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
        		.add(Attributes.MAX_HEALTH, 9.0D)
        		.add(Attributes.ARMOR, 4.0D)
        		.add(Attributes.MOVEMENT_SPEED, 0.265D)
        		.add(Attributes.ATTACK_DAMAGE, 3.5D);
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
	    this.goalSelector.addGoal(3, new EntityAIRatAvoid<>(this, Ocelot.class, 6.0F, 1.0D, 1.2D));
	    this.goalSelector.addGoal(3, new EntityAIRatAvoid<>(this, Cat.class, 6.0F, 1.0D, 1.2D));
	    this.goalSelector.addGoal(4, new EntityAIRatFollowGiant(this, 1.25D));
	    
	    if(ConfigVO.MOBS.aiSettings.isOddityAIEnabled(VOEntities.RAT))
		    this.targetSelector.addGoal(1, new HurtByTargetGoal(this, AbstractRat.class, Ocelot.class, Cat.class));
    }
    
    public boolean isPersistenceRequired(){ return !isMinion() || super.isPersistenceRequired(); }
    
    /**
     * 1 in 20 rats are plague rats and deal poison damage.<br>
     * The rest are black, brown, or white.
     */
	public int getRandomBreed()
	{
		return getRandom().nextInt(20) == 0 ? EnumRatBreed.getID(EnumRatBreed.PLAGUE) : getRandom().nextInt(3);
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
    	return super.getVoicePitch() * 1.25F;
    }
    
    public void setMinion(boolean par1Bool){ getDataManager().set(MINION, par1Bool); }
    public boolean isMinion(){ return getDataManager().get(MINION).booleanValue(); }
    
    public void writeAdditional(CompoundTag compound)
    {
    	super.writeAdditional(compound);
    	if(isMinion())
    		compound.putBoolean("Minion", isMinion());
    }
    
    public void readAdditionalSaveData(CompoundTag compound)
    {
    	super.readAdditionalSaveData(compound);
    	if(compound.contains("Minion") && compound.getBoolean("Minion"))
    		setMinion(true);
    }
}
