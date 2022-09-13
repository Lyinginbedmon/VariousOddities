package com.lying.variousoddities.entity.mount;

import javax.annotation.Nullable;

import com.lying.variousoddities.entity.AbstractGoblinWolf;
import com.lying.variousoddities.entity.IMountInventory;
import com.lying.variousoddities.inventory.ContainerWarg;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.phys.Vec3;

public class EntityWarg extends AbstractGoblinWolf implements ItemSteerable, PlayerRideableJumping, IMountInventory, ContainerListener
{
	private static final EntityDataAccessor<Boolean> REARING	= SynchedEntityData.defineId(EntityWarg.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> SITTING	= SynchedEntityData.defineId(EntityWarg.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> CHEST	= SynchedEntityData.defineId(EntityWarg.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> SADDLE	= SynchedEntityData.defineId(EntityWarg.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> CARPET	= SynchedEntityData.defineId(EntityWarg.class, EntityDataSerializers.INT);
	
	private float jumpPower = 0F;
	private boolean allowStandSliding;
	
	private boolean wargJumping;
	private int jumpRearingCounter;
	private float rearingAmount;
	@SuppressWarnings("unused")
	private float prevRearingAmount;
	
	public Inventory wargChest;
	
	public EntityWarg(EntityType<? extends EntityWarg> type, Level worldIn)
	{
		super(type, worldIn);
	    this.initWargChest();
	}
	
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		getEntityData().define(REARING, false);
		getEntityData().define(SITTING, false);
		getEntityData().define(CHEST, false);
		getEntityData().define(SADDLE, false);
		getEntityData().define(CARPET, -1);
	}
	
    public static AttributeSupplier.Builder createAttributes()
    {
        return Monster.createMobAttributes()
        		.add(Attributes.MAX_HEALTH, 40.0D)
        		.add(Attributes.ARMOR, 7.0D)
        		.add(Attributes.MOVEMENT_SPEED, (double)0.3002F)
        		.add(Attributes.ATTACK_DAMAGE, 12.0D);
    }
	
	public void getAggressiveBehaviours()
	{
		this.addGeneticAI(3, new NearestAttackableTargetGoal<Cow>(this, Cow.class, true));
		this.addGeneticAI(3, new NearestAttackableTargetGoal<Pig>(this, Pig.class, true));
		this.addGeneticAI(3, new NearestAttackableTargetGoal<Llama>(this, Llama.class, true));
		this.addGeneticAI(3, new NearestAttackableTargetGoal<Sheep>(this, Sheep.class, true));
	}
	
	public AgeableMob getBreedOffspring(ServerLevel arg0, AgeableMob arg1)
	{
		return null;
	}
	
	public float getStepHeight() { return 1F; }
	
	public void initWargChest()
	{
		Inventory inventory = this.wargChest;
		this.wargChest = new Inventory(getSizeInventory());
		if(inventory != null)
		{
			inventory.removeListener(this);
			int i = Math.min(inventory.getSizeInventory(), this.wargChest.getSizeInventory());
			for(int j = 0; j < i; ++j)
			{
				ItemStack stack = inventory.getStackInSlot(j);
				if(!stack.isEmpty())
					this.wargChest.setInventorySlotContents(j, stack.copy());
			}
		}
		
		this.wargChest.addListener(this);
	}
    
    public void addAdditionalSaveData(CompoundTag compound)
    {
    	super.addAdditionalSaveData(compound);
    	
    	compound.putBoolean("Sitting", isOrderedToSit());
    	compound.putBoolean("Chest", hasChest());
    	
		ListTag inventory = new ListTag();
		for(int i=0; i<this.wargChest.getSizeInventory(); ++i)
		{
			ItemStack stack = this.wargChest.getStackInSlot(i);
			if(!stack.isEmpty())
			{
				CompoundTag stackData = new CompoundTag();
				stackData.putByte("Slot", (byte)i);
				stack.write(stackData);
				inventory.add(stackData);
			}
		}
		compound.put("Inventory", inventory);
    }
    
    public void readAdditionalSaveData(CompoundTag compound)
    {
    	super.readAdditionalSaveData(compound);
    	
    	setOrderedToSit(compound.getBoolean("Sitting"));
    	setSleeping(isOrderedToSit());
    	
    	setChested(compound.getBoolean("Chest"));
		initWargChest();
		ListTag inventory = compound.getList("Inventory", 10);
		for(int i=0; i<inventory.size(); ++i)
		{
			CompoundTag stackData = inventory.getCompound(i);
			int slot = stackData.getByte("Slot") & 255;
			if(slot >= 0 && slot < this.wargChest.getSizeInventory())
				this.wargChest.setInventorySlotContents(slot, ItemStack.read(stackData));
		}
    }
    
    public boolean isTamed(){ return true; }
	
	public boolean hasChest(){ return getEntityData().get(CHEST).booleanValue(); }
	
	public void setChested(boolean bool){ getEntityData().set(CHEST, bool); }
	
	public int getSizeInventory(){ return 3 + inventoryColumns() * 3; }
	
	public int inventoryColumns(){ return 5; }
	
	protected void dropEquipment()
	{
		super.dropEquipment();
		if(this.wargChest != null)
			for(int i=0; i<this.wargChest.getSizeInventory(); ++i)
			{
				ItemStack itemStack = this.wargChest.getStackInSlot(i);
				if(!itemStack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemStack))
					spawnAtLocation(itemStack);
			};
	}
	
	public boolean isJumping()
	{
		return this.wargJumping;
	}
	
	public void setJumping(boolean jumping)
	{
		this.wargJumping = jumping;
	}
	
	public boolean isRearing()
	{
		return getEntityData().get(REARING).booleanValue();
	}
	
	public void setRearing(boolean rearing)
	{
		getEntityData().set(REARING, rearing);
	}
	
	private void makeWargRear()
	{
		if(canPassengerSteer() || this.isServerWorld())
		{
			this.jumpRearingCounter = 1;
			this.setRearing(true);
		}
	}
	
	public boolean boost()
	{
		return false;
	}
	
	public void travelTowards(Vec3 travelVec)
	{
		super.travel(travelVec);
	}
	
	public DyeColor getCarpetColor()
	{
		int id = getEntityData().get(CARPET).intValue();
		return id < 0 ? null : DyeColor.byId(id);
	}
	
	public void setCarpetColor(DyeColor colorIn){ getEntityData().set(CARPET, colorIn == null ? -1 : colorIn.getId()); }
	
	@Override
	public boolean isOrderedToSit(){ return getEntityData().get(SITTING).booleanValue(); }
	
	@Override
	public void setOrderedToSit(boolean sitting){ getEntityData().set(SITTING, sitting); }
	
	@Override
	public boolean isSleeping() { return isOrderedToSit(); }
	
	public boolean isRiderControlling()
	{
		if(isOrderedToSit()) return false;
		if(getControllingPassenger() != null)
		{
			LivingEntity rider = (LivingEntity)getControllingPassenger();
			return Math.abs(rider.moveForward) > 0F || Math.abs(rider.moveStrafing) > 0F || this.jumpPower > 0F;
		}
		return false;
	}
	
	public void travel(Vec3 travelVector)
	{
		if(!this.isAlive())
			return;
		
		if(isBeingRidden() && canBeSteered())
		{
			LivingEntity entity = (LivingEntity)getControllingPassenger();
			
			float forward = 0F;
			float strafe = 0F;
			
			if(isRiderControlling())
			{
				forward = entity.moveForward;
				strafe = entity.moveStrafing * 0.5F;
			}
			
			this.rotationYaw = entity.rotationYaw;
			this.prevRotationYaw = this.rotationYaw;
			this.rotationPitch = entity.rotationPitch * 0.5F;
			this.setRotation(this.rotationYaw, this.rotationPitch);
			this.renderYawOffset = this.rotationYaw;
			this.rotationYawHead = this.renderYawOffset;
			
			if(this.onGround && this.jumpPower == 0F && this.isRearing() && !this.allowStandSliding)
				strafe = forward = 0F;
			
			if(this.jumpPower > 0F && !this.isJumping() && this.onGround)
			{
				double jump = this.jumpPower * this.getJumpFactor();
				double boost;
				if(hasEffect(MobEffects.JUMP))
					boost = jump + (double)((float)(getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.1F);
				else
					boost = jump;
				
				Vec3 motion = this.getDeltaMovement();
				this.setDeltaMovement(motion.x, boost, motion.z);
				this.setJumping(true);
				this.isAirBorne = true;
				net.minecraftforge.common.ForgeHooks.onLivingJump(this);
				if(forward > 0F || !isRiderControlling())
				{
					float sine = Mth.sin(this.rotationYaw * ((float)Math.PI / 180F));
					float cosine = Mth.cos(this.rotationYaw * ((float)Math.PI / 180F));
					this.setDeltaMovement(this.getDeltaMovement().add(-0.4F * sine * this.jumpPower, 0D, 0.4F * cosine * this.jumpPower));
				}
				
				this.jumpPower = 0F;
			}
			
			this.jumpMovementFactor = this.getSpeed() * 0.1F;
			if(canPassengerSteer())
			{
				this.setAIMoveSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED));
				super.travel(new Vec3(strafe, travelVector.y, forward));
			}
			
			if(this.onGround)
			{
				this.jumpPower = 0F;
				this.setJumping(false);
			}
			this.func_233629_a_(this, false);
			return;
		}
		
		this.jumpMovementFactor = 0.02F;
		super.travel(travelVector);
	}
	
	public float getMountedSpeed()
	{
		return (float)getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.225F;
	}
	
	public InteractionResult mobInteract(Player player, InteractionHand hand)
	{
		ItemStack heldStack = player.getItemInHand(hand);
		
		if(ItemTags.WOOL_CARPETS.contains(heldStack.getItem()))
			if(this.wargChest != null && this.wargChest.getStackInSlot(1).isEmpty())
			{
				this.wargChest.setInventorySlotContents(1, heldStack.split(1));
				return InteractionResult.sidedSuccess(this.level.isClientSide);
			}
		
		if(!isBaby())
		{
			if(!isSaddled() && heldStack.getItem() == Items.SADDLE)
			{
				this.wargChest.setInventorySlotContents(0, heldStack.split(1));
				return InteractionResult.sidedSuccess(this.level.isClientSide);
			}
			
			if(heldStack.getItem() instanceof HorseArmorItem)
				if(this.wargChest != null && this.wargChest.getStackInSlot(2).isEmpty())
				{
					this.wargChest.setInventorySlotContents(2, heldStack.split(1));
					return InteractionResult.sidedSuccess(this.level.isClientSide);
				}
			
			if(!hasChest() && Block.getBlockFromItem(heldStack.getItem()) == Blocks.CHEST)
			{
				setChested(true);
				playChestEquipSound();
				if(!player.abilities.isCreativeMode)
					heldStack.shrink(1);
				
				initWargChest();
				return InteractionResult.sidedSuccess(this.level.isClientSide);
			}
			
			if(!isBeingRidden() && !player.isSecondaryUseActive())
			{
				if(!this.getLevel().isClientSide && (isTamed() || player.isCreative()))
					player.startRiding(this);
				return InteractionResult.sidedSuccess(this.getLevel().isClientSide);
			}
		}
		
		return super.mobInteract(player, hand);
	}
	
	protected void playChestEquipSound()
	{
		playSound(SoundEvents.DONKEY_CHEST, 1F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1F);
	}
	
	public boolean isSaddled(){ return getEntityData().get(SADDLE).booleanValue(); }
	
	@Nullable
	public Entity getControllingPassenger()
	{
		return this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
	}
	
	public boolean canBeSteered()
	{
		LivingEntity rider = (LivingEntity)getControllingPassenger();
		return isSaddled() && rider != null && rider.isAlive() && (rider.moveForward != 0F || rider.moveStrafing != 0F);
	}
	
	public void tick()
	{
		super.tick();
		
		if((canPassengerSteer() || this.isServerWorld()) && this.jumpRearingCounter > 0 && ++this.jumpRearingCounter > 20)
		{
			this.jumpRearingCounter = 0;
			this.setRearing(false);
		}
		
		this.prevRearingAmount = this.rearingAmount;
		if(this.isRearing())
		{
			this.rearingAmount += (1F - this.rearingAmount) * 0.4F + 0.05F;
			if(this.rearingAmount > 1F)
				this.rearingAmount = 1F;
		}
		else
		{
			this.allowStandSliding = false;
			this.rearingAmount += (0.8F * this.rearingAmount * this.rearingAmount * this.rearingAmount - this.rearingAmount) * 0.6F - 0.5F;
			if(this.rearingAmount < 0F)
				this.rearingAmount = 0F;
		}
	}
	
	public void setJumpPower(int jumpPowerIn)
	{
		if(this.canPassengerSteer())
		{
			if(jumpPowerIn < 0)
				jumpPowerIn = 0;
			else
			{
				this.allowStandSliding = true;
				this.makeWargRear();
			}
			
			if(jumpPowerIn >= 90)
				this.jumpPower = 1F;
			else
				this.jumpPower = 0.4F + 0.4F * (float)jumpPowerIn / 90F;
		}
	}
	
	public boolean canJump()
	{
		return canPassengerSteer();
	}
	
	public void handleStartJump(int jumpPower)
	{
		this.allowStandSliding = true;
		this.makeWargRear();
	}
	
	public void handleStopJump()
	{
		
	}
	
	public void openContainer(Player playerIn)
	{
		playerIn.openContainer(new SimpleNamedContainerProvider((window, player, p1) -> new ContainerWarg(window, player, this.wargChest, this), this.getDisplayName()));
	}
	
	public void slotChanged(AbstractContainerMenu invBasic, int slot, ItemStack stack)
	{
		ItemStack armour = this.getItemBySlot(EquipmentSlot.CHEST);
		updateArmour();
		if(this.tickCount > 20 && !getItemBySlot(EquipmentSlot.CHEST).isEmpty() && armour.getItem() != getItemBySlot(EquipmentSlot.CHEST).getItem())
			playSound(SoundEvents.HORSE_ARMOR, 0.5F, 1.0F);
		
		boolean saddled = isSaddled();
		updateSaddle();
		if(this.tickCount > 20 && !saddled && this.isSaddled())
			playSound(SoundEvents.HORSE_SADDLE, 0.5F, 1F);
		
		DyeColor carpet = getCarpetColor();
		updateCarpet();
		if(this.tickCount > 20 && getCarpetColor() != null && carpet != getCarpetColor())
			playSound(SoundEvents.LLAMA_SWAG, 0.5F, 1.0F);
	}
	
	private void updateArmour()
	{
		if(!this.level.isClientSide)
			this.setItemStackToSlot(EquipmentSlot.CHEST, this.wargChest.getStackInSlot(2).copy());
	}
	
	private void updateSaddle()
	{
		if(!this.level.isClientSide)
			getEntityData().set(SADDLE, !this.wargChest.getStackInSlot(0).isEmpty());
	}
	
	private void updateCarpet()
	{
		if(!this.level.isClientSide)
		{
			ItemStack carpet = this.wargChest.getStackInSlot(1);
			int color = carpet.isEmpty() ? -1 : ((CarpetBlock)Block.getBlockFromItem(carpet.getItem())).getColor().getId(); 
			getEntityData().set(CARPET, color);
		}
	}
}
