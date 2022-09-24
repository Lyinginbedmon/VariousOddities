package com.lying.variousoddities.entity.passive;

import java.util.Collections;
import java.util.Random;

import com.lying.variousoddities.api.entity.IMysticSource;
import com.lying.variousoddities.entity.EntityOddity;
import com.lying.variousoddities.init.VOItems;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.utility.DataHelper;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

public class EntityMarimo extends EntityOddity implements IMysticSource
{
    private static final EntityDataAccessor<Integer>	 DYE_COLOR	= SynchedEntityData.defineId(EntityMarimo.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Byte>		MYSTIC		= SynchedEntityData.defineId(EntityMarimo.class, EntityDataSerializers.BYTE);
    public static final EntityDataAccessor<Integer>		RECHARGE	= SynchedEntityData.defineId(EntityMarimo.class, EntityDataSerializers.INT);
	private float targetYaw;
	private float targetPitch;
    
	private Random random;
	
	public EntityMarimo(EntityType<? extends EntityMarimo> type, Level worldIn)
	{
		super(type, worldIn);
		random = new Random(this.getUUID().getLeastSignificantBits());
	}
	
	public Iterable<ItemStack> getArmorInventoryList() { return Collections.emptyList(); }
	public ItemStack getItemStackFromSlot(EquipmentSlot slotIn) { return ItemStack.EMPTY; }
	public void setItemStackToSlot(EquipmentSlot slotIn, ItemStack stack){ }
	public HumanoidArm getMainArm() { return HumanoidArm.RIGHT; }
	
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		random = new Random(this.getUUID().getLeastSignificantBits());
		
		getEntityData().define(DYE_COLOR, DyeColor.GREEN.getId());
		DataHelper.Booleans.registerBooleanByte(getEntityData(), MYSTIC, random.nextInt(50) == 0);
		getEntityData().define(RECHARGE, 0);
	}
	
    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 5.0D);
    }
    
    public boolean checkSpawnRules(LevelAccessor world, MobSpawnType reason)
    {
        return world.getBlockState(blockPosition()).getFluidState().is(Fluids.WATER);
    }
    
    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void addAdditionalSaveData(CompoundTag compound)
    {
        super.addAdditionalSaveData(compound);
        compound.putInt("Color", getColor().getId());
        compound.putBoolean("Mystic", isMagical());
    }
    
    public void readAdditionalSaveData(CompoundTag compound)
    {
        super.readAdditionalSaveData(compound);
        setColor(compound.getInt("Color"));
        setMagical(compound.getBoolean("Mystic"));
    }
    
    public boolean isNoDespawnRequired(){ return true; }
	
    public InteractionResult applyPlayerInteraction(Player player, InteractionHand hand)
    {
        ItemStack itemstack = player.getItemInHand(hand);
        if(itemstack.getItem() == Items.POTION && PotionUtils.getPotion(itemstack) == Potions.WATER)
        {
        	itemstack.shrink(1);
        	player.addItem(getItemStack());
        	this.remove(RemovalReason.DISCARDED);
        	return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.FAIL;
    }
    
    public ItemStack getItemStack()
    {
    	ItemStack bottle = VOItems.MOSS_BOTTLE.getDefaultInstance();
    	if(this.hasCustomName()) bottle.setHoverName(getCustomName());
    	CompoundTag stackData = bottle.hasTag() ? bottle.getTag() : new CompoundTag();
    	stackData.putInt("Color", getColor().getId());
    	stackData.putBoolean("Mystic", isMagical());
    	bottle.setTag(stackData);
    	return bottle;
    }
    
    public boolean canBreatheUnderwater()
    {
        return true;
    }
    
    public void aiStep()
    {
    	super.aiStep();
    	if(!this.isInWater() && !this.isInvulnerable() && this.invulnerableTime == 0)
    		this.hurt(DamageSource.DROWN, 1.0F);
    	else if(this.isInWater() && this.getHealth() < this.getMaxHealth() && getLevel().getGameTime()%Reference.Values.TICKS_PER_MINUTE == 0)
    		this.heal(1.0F);
    	
    	if(this.isInWater() && this.isAlive() && !getLevel().isClientSide)
    	{
    		
    		if(this.random.nextInt(Reference.Values.TICKS_PER_SECOND * 5) == 0)
    		{
    			Vec3 motion = getDeltaMovement();
    			double motionX = motion.x + (this.random.nextDouble() - 0.5D) * 0.05D;
    			double motionY = motion.y + (this.random.nextDouble() - 0.5D) * 0.025D;
    			double motionZ = motion.z + (this.random.nextDouble() - 0.5D) * 0.05D;
    			setDeltaMovement(new Vec3(motionX, motionY, motionZ));
    		}
    		
    		if(this.random.nextInt(Reference.Values.TICKS_PER_MINUTE) == 0)
    		{
                this.targetYaw = limitAngle(this.getYHeadRot(), this.random.nextFloat() * 360F, 180F);
                this.targetPitch = limitAngle(this.getXRot(), this.random.nextFloat() * 360F, 180F);
    		}
    		
    		if(getYHeadRot() != this.targetYaw) this.setYHeadRot(limitAngle(getYHeadRot(), this.targetYaw, 0.5F));
    		if(getXRot() != this.targetPitch) this.setXRot(limitAngle(getXRot(), this.targetPitch, 0.5F));
    	}
    	
        if(isMagical())
        {
        	if(canProvidePower())
        	{
		    	if(getLevel().isClientSide && getRandom().nextInt(50) == 0)
		            for (int i = 0; i < 2; ++i)
		            	getLevel().addParticle(ParticleTypes.PORTAL, getX() + (this.random.nextDouble() - 0.5D) * (double)getBbWidth(), getY() + this.random.nextDouble() * (double)getBbHeight() - 0.25D, getZ() + (this.random.nextDouble() - 0.5D) * (double)getBbWidth(), (this.random.nextDouble() - 0.5D) * 2.0D, -this.random.nextDouble(), (this.random.nextDouble() - 0.5D) * 2.0D);
        	}
        	else
        	{
		    	int charge = getEntityData().get(RECHARGE).intValue();
		    	if(!getLevel().isClientSide && charge != 0)
		    	{
		    		charge -= Math.signum(charge);
		    		getEntityData().set(RECHARGE, charge);
		    	}
        	}
        }
    }
    
    public void setColor(int par1Int){ getEntityData().set(DYE_COLOR, par1Int); }
    public void setColor(DyeColor colorIn){ setColor(colorIn.getId()); }
    public DyeColor getColor(){ return DyeColor.byId(getEntityData().get(DYE_COLOR).intValue()); }
    
    protected float limitAngle(float sourceAngle, float targetAngle, float maximumChange)
    {
        float f = Mth.wrapDegrees(targetAngle - sourceAngle);
        f = Math.max(-maximumChange, Math.min(maximumChange, f));
        
        float f1 = sourceAngle + f;
        if (f1 < 0.0F) f1 += 360.0F;
        else if (f1 > 360.0F) f1 -= 360.0F;
        
        return f1;
    }
    
    protected boolean canTriggerWalking()
    {
        return false;
    }
    
	public boolean canProvidePower(){ return isMagical() && getEntityData().get(RECHARGE).intValue() == 0; }
	
	public int getTotalPower(){ return 1; }
	
	public void setRecharge(){ getEntityData().set(RECHARGE, Reference.Values.TICKS_PER_DAY * 2); }
	
	public boolean isMagical(){ return DataHelper.Booleans.getBooleanByte(getEntityData(), MYSTIC); }
	public void setMagical(boolean par1Bool){ DataHelper.Booleans.setBooleanByte(getEntityData(), par1Bool, MYSTIC); }

}
