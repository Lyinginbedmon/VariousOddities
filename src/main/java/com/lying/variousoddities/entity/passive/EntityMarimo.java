package com.lying.variousoddities.entity.passive;

import java.util.Collections;
import java.util.Random;

import com.lying.variousoddities.api.entity.IMysticSource;
import com.lying.variousoddities.entity.EntityOddity;
import com.lying.variousoddities.init.VOItems;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.utility.DataHelper;
import com.mojang.math.Vector3d;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
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

public class EntityMarimo extends EntityOddity implements IMysticSource
{
    private static final DataParameter<Integer> DYE_COLOR	= EntityDataManager.<Integer>createKey(EntityMarimo.class, DataSerializers.VARINT);
    public static final DataParameter<Byte>		MYSTIC		= EntityDataManager.<Byte>createKey(EntityMarimo.class, DataSerializers.BYTE);
    public static final DataParameter<Integer>	RECHARGE	= EntityDataManager.<Integer>createKey(EntityMarimo.class, DataSerializers.VARINT);
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
	public HandSide getPrimaryHand() { return HandSide.RIGHT; }
	
	protected void registerData()
	{
		super.registerData();
		random = new Random(this.getUUID().getLeastSignificantBits());
		
		getDataManager().register(DYE_COLOR, DyeColor.GREEN.getId());
		DataHelper.Booleans.registerBooleanByte(getDataManager(), MYSTIC, random.nextInt(50) == 0);
		getDataManager().register(RECHARGE, 0);
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
    public void writeAdditional(CompoundTag compound)
    {
        super.writeAdditional(compound);
        compound.putInt("Color", getColor().getId());
        compound.putBoolean("Mystic", isMagical());
    }
    
    public void readAdditional(CompoundTag compound)
    {
        super.readAdditional(compound);
        setColor(compound.getInt("Color"));
        setMagical(compound.getBoolean("Mystic"));
    }
    
    public boolean isNoDespawnRequired(){ return true; }
	
    public ActionResultType applyPlayerInteraction(Player player, InteractionHand hand)
    {
        ItemStack itemstack = player.getHeldItem(hand);
        if(itemstack.getItem() == Items.POTION && PotionUtils.getPotion(itemstack) == Potions.WATER)
        {
        	itemstack.shrink(1);
        	player.addItemStackToInventory(getItemStack());
        	this.setDead();
        	return ActionResultType.SUCCESS;
        }
        
        return ActionResultType.FAIL;
    }
    
    public ItemStack getItemStack()
    {
    	ItemStack bottle = VOItems.MOSS_BOTTLE.getDefaultInstance();
    	if(this.hasCustomName()) bottle.setDisplayName(getCustomName());
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
    
    public void livingTick()
    {
    	super.livingTick();
    	if(!this.isInWater() && !this.isInvulnerable() && this.hurtResistantTime == 0) this.hurt(DamageSource.DROWN, 1.0F);
    	else if(this.isInWater() && this.getHealth() < this.getMaxHealth() && getLevel().getGameTime()%Reference.Values.TICKS_PER_MINUTE == 0) this.heal(1.0F);
    	
    	if(this.isInWater() && this.isAlive() && !getLevel().isClientSide)
    	{
    		
    		if(this.random.nextInt(Reference.Values.TICKS_PER_SECOND * 5) == 0)
    		{
    			Vector3d motion = getMotion();
    			double motionX = motion.x + (this.random.nextDouble() - 0.5D) * 0.05D;
    			double motionY = motion.y + (this.random.nextDouble() - 0.5D) * 0.025D;
    			double motionZ = motion.z + (this.random.nextDouble() - 0.5D) * 0.05D;
    			setMotion(new Vector3d(motionX, motionY, motionZ));
    		}
    		
    		if(this.random.nextInt(Reference.Values.TICKS_PER_MINUTE) == 0)
    		{
                this.targetYaw = limitAngle(this.rotationYawHead, this.random.nextFloat() * 360F, 180F);
                this.targetPitch = limitAngle(this.rotationPitch, this.random.nextFloat() * 360F, 180F);
    		}
    		
    		if(this.rotationYawHead != this.targetYaw) this.rotationYawHead = limitAngle(this.rotationYawHead, this.targetYaw, 0.5F);
    		if(this.rotationPitch != this.targetPitch) this.rotationPitch = limitAngle(this.rotationPitch, this.targetPitch, 0.5F);
    	}
    	
        if(isMagical())
        {
        	if(canProvidePower())
        	{
		    	if(getLevel().isClientSide && getRNG().nextInt(50) == 0)
		            for (int i = 0; i < 2; ++i)
		            	getLevel().addParticle(ParticleTypes.PORTAL, getPosX() + (this.random.nextDouble() - 0.5D) * (double)getWidth(), getPosY() + this.random.nextDouble() * (double)getHeight() - 0.25D, getPosZ() + (this.random.nextDouble() - 0.5D) * (double)getWidth(), (this.random.nextDouble() - 0.5D) * 2.0D, -this.random.nextDouble(), (this.random.nextDouble() - 0.5D) * 2.0D);
        	}
        	else
        	{
		    	int charge = getDataManager().get(RECHARGE).intValue();
		    	if(!getLevel().isClientSide && charge != 0)
		    	{
		    		charge -= Math.signum(charge);
		    		getDataManager().set(RECHARGE, charge);
		    	}
        	}
        }
    }
    
    public void setColor(int par1Int){ getDataManager().set(DYE_COLOR, par1Int); }
    public void setColor(DyeColor colorIn){ setColor(colorIn.getId()); }
    public DyeColor getColor(){ return DyeColor.byId(getDataManager().get(DYE_COLOR).intValue()); }
    
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
    
	public boolean canProvidePower(){ return isMagical() && getDataManager().get(RECHARGE).intValue() == 0; }
	
	public int getTotalPower(){ return 1; }
	
	public void setRecharge(){ getDataManager().set(RECHARGE, Reference.Values.TICKS_PER_DAY * 2); }
	
	public boolean isMagical(){ return DataHelper.Booleans.getBooleanByte(getDataManager(), MYSTIC); }
	public void setMagical(boolean par1Bool){ DataHelper.Booleans.setBooleanByte(getDataManager(), par1Bool, MYSTIC); }

}
