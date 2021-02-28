package com.lying.variousoddities.entity.passive;

import java.util.Collections;
import java.util.Random;

import com.lying.variousoddities.api.entity.IMysticSource;
import com.lying.variousoddities.entity.EntityOddity;
import com.lying.variousoddities.init.VOItems;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.utility.DataHelper;

import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class EntityMarimo extends EntityOddity implements IMysticSource
{
    private static final DataParameter<Integer> DYE_COLOR	= EntityDataManager.<Integer>createKey(EntityMarimo.class, DataSerializers.VARINT);
    public static final DataParameter<Byte>		MYSTIC		= EntityDataManager.<Byte>createKey(EntityMarimo.class, DataSerializers.BYTE);
    public static final DataParameter<Integer>	RECHARGE	= EntityDataManager.<Integer>createKey(EntityMarimo.class, DataSerializers.VARINT);
	private float targetYaw;
	private float targetPitch;
    
	private Random random;
	
	public EntityMarimo(EntityType<? extends EntityMarimo> type, World worldIn)
	{
		super(type, worldIn);
		random = new Random(this.getUniqueID().getLeastSignificantBits());
	}
	
	public Iterable<ItemStack> getArmorInventoryList() { return Collections.emptyList(); }
	public ItemStack getItemStackFromSlot(EquipmentSlotType slotIn) { return ItemStack.EMPTY; }
	public void setItemStackToSlot(EquipmentSlotType slotIn, ItemStack stack){ }
	public HandSide getPrimaryHand() { return HandSide.RIGHT; }
	
	protected void registerData()
	{
		super.registerData();
		random = new Random(this.getUniqueID().getLeastSignificantBits());
		
		getDataManager().register(DYE_COLOR, DyeColor.GREEN.getId());
		DataHelper.Booleans.registerBooleanByte(getDataManager(), MYSTIC, random.nextInt(50) == 0);
		getDataManager().register(RECHARGE, 0);
	}
	
    public static AttributeModifierMap.MutableAttribute getAttributes()
    {
        return MobEntity.func_233666_p_().createMutableAttribute(Attributes.MAX_HEALTH, 5.0D);
    }
    
    public static boolean canSpawnAt(EntityType<?> animal, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
        return world.getBlockState(pos).getBlock() instanceof FlowingFluidBlock;
    }
    
    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeAdditional(CompoundNBT compound)
    {
        super.writeAdditional(compound);
        compound.putInt("Color", getColor().getId());
        compound.putBoolean("Mystic", isMagical());
    }
    
    public void readAdditional(CompoundNBT compound)
    {
        super.readAdditional(compound);
        setColor(compound.getInt("Color"));
        setMagical(compound.getBoolean("Mystic"));
    }
    
    public boolean isNoDespawnRequired(){ return true; }
	
    public ActionResultType applyPlayerInteraction(PlayerEntity player, Hand hand)
    {
        ItemStack itemstack = player.getHeldItem(hand);
        if(itemstack.getItem() == Items.POTION && PotionUtils.getPotionFromItem(itemstack) == Potions.WATER)
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
    	CompoundNBT stackData = bottle.hasTag() ? bottle.getTag() : new CompoundNBT();
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
    	if(!this.isInWater() && !this.isInvulnerable() && this.hurtResistantTime == 0) this.attackEntityFrom(DamageSource.DROWN, 1.0F);
    	else if(this.isInWater() && this.getHealth() < this.getMaxHealth() && getEntityWorld().getGameTime()%Reference.Values.TICKS_PER_MINUTE == 0) this.heal(1.0F);
    	
    	if(this.isInWater() && this.isAlive() && !getEntityWorld().isRemote)
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
		    	if(getEntityWorld().isRemote && getRNG().nextInt(50) == 0)
		            for (int i = 0; i < 2; ++i)
		            	getEntityWorld().addParticle(ParticleTypes.PORTAL, getPosX() + (this.random.nextDouble() - 0.5D) * (double)getWidth(), getPosY() + this.random.nextDouble() * (double)getHeight() - 0.25D, getPosZ() + (this.random.nextDouble() - 0.5D) * (double)getWidth(), (this.random.nextDouble() - 0.5D) * 2.0D, -this.random.nextDouble(), (this.random.nextDouble() - 0.5D) * 2.0D);
        	}
        	else
        	{
		    	int charge = getDataManager().get(RECHARGE).intValue();
		    	if(!getEntityWorld().isRemote && charge != 0)
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
        float f = MathHelper.wrapDegrees(targetAngle - sourceAngle);
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
