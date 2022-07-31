package com.lying.variousoddities.entity.mount;

import javax.annotation.Nullable;

import com.lying.variousoddities.entity.AbstractGoblinWolf;
import com.lying.variousoddities.entity.IMountInventory;
import com.lying.variousoddities.inventory.ContainerWarg;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarpetBlock;

public class EntityWarg extends AbstractGoblinWolf implements IRideable, IJumpingMount, IMountInventory, IInventoryChangedListener
{
	private static final DataParameter<Boolean> REARING	= EntityDataManager.<Boolean>createKey(EntityWarg.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> SITTING	= EntityDataManager.<Boolean>createKey(EntityWarg.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> CHEST	= EntityDataManager.<Boolean>createKey(EntityWarg.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> SADDLE	= EntityDataManager.<Boolean>createKey(EntityWarg.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> CARPET	= EntityDataManager.<Integer>createKey(EntityWarg.class, DataSerializers.VARINT);
	
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
	    this.stepHeight = 1.0F;
	    this.initWargChest();
	}
	
	protected void registerData()
	{
		super.registerData();
		getDataManager().register(REARING, false);
		getDataManager().register(SITTING, false);
		getDataManager().register(CHEST, false);
		getDataManager().register(SADDLE, false);
		getDataManager().register(CARPET, -1);
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
    
    public void writeAdditional(CompoundTag compound)
    {
    	super.writeAdditional(compound);
    	
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
    
    public void readAdditional(CompoundTag compound)
    {
    	super.readAdditional(compound);
    	
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
	
	public boolean hasChest(){ return getDataManager().get(CHEST).booleanValue(); }
	
	public void setChested(boolean bool){ getDataManager().set(CHEST, bool); }
	
	public int getSizeInventory(){ return 3 + inventoryColumns() * 3; }
	
	public int inventoryColumns(){ return 5; }
	
	protected void dropInventory()
	{
		super.dropInventory();
		if(this.wargChest != null)
			for(int i=0; i<this.wargChest.getSizeInventory(); ++i)
			{
				ItemStack itemStack = this.wargChest.getStackInSlot(i);
				if(!itemStack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemStack))
					this.entityDropItem(itemStack);
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
		return getDataManager().get(REARING).booleanValue();
	}
	
	public void setRearing(boolean rearing)
	{
		getDataManager().set(REARING, rearing);
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
	
	public void travelTowards(Vec3d travelVec)
	{
		super.travel(travelVec);
	}
	
	public DyeColor getCarpetColor()
	{
		int id = getDataManager().get(CARPET).intValue();
		return id < 0 ? null : DyeColor.byId(id);
	}
	
	public void setCarpetColor(DyeColor colorIn){ getDataManager().set(CARPET, colorIn == null ? -1 : colorIn.getId()); }
	
	@Override
	public boolean isOrderedToSit(){ return getDataManager().get(SITTING).booleanValue(); }
	
	@Override
	public void setOrderedToSit(boolean sitting){ getDataManager().set(SITTING, sitting); }
	
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
	
	public void travel(Vec3d travelVector)
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
				if(isPotionActive(Effects.JUMP_BOOST))
					boost = jump + (double)((float)(getActivePotionEffect(Effects.JUMP_BOOST).getAmplifier() + 1) * 0.1F);
				else
					boost = jump;
				
				Vec3d motion = this.getMotion();
				this.setMotion(motion.x, boost, motion.z);
				this.setJumping(true);
				this.isAirBorne = true;
				net.minecraftforge.common.ForgeHooks.onLivingJump(this);
				if(forward > 0F || !isRiderControlling())
				{
					float sine = MathHelper.sin(this.rotationYaw * ((float)Math.PI / 180F));
					float cosine = MathHelper.cos(this.rotationYaw * ((float)Math.PI / 180F));
					this.setMotion(this.getMotion().add(-0.4F * sine * this.jumpPower, 0D, 0.4F * cosine * this.jumpPower));
				}
				
				this.jumpPower = 0F;
			}
			
			this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;
			if(canPassengerSteer())
			{
				this.setAIMoveSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED));
				super.travel(new Vec3d(strafe, travelVector.y, forward));
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
	
	public ActionResultType func_230254_b_(Player player, Hand hand)
	{
		ItemStack heldStack = player.getHeldItem(hand);
		
		if(ItemTags.CARPETS.contains(heldStack.getItem()))
			if(this.wargChest != null && this.wargChest.getStackInSlot(1).isEmpty())
			{
				this.wargChest.setInventorySlotContents(1, heldStack.split(1));
				return ActionResultType.func_233537_a_(this.level.isClientSide);
			}
		
		if(!isChild())
		{
			if(!isSaddled() && heldStack.getItem() == Items.SADDLE)
			{
				this.wargChest.setInventorySlotContents(0, heldStack.split(1));
				return ActionResultType.func_233537_a_(this.level.isClientSide);
			}
			
			if(heldStack.getItem() instanceof HorseArmorItem)
				if(this.wargChest != null && this.wargChest.getStackInSlot(2).isEmpty())
				{
					this.wargChest.setInventorySlotContents(2, heldStack.split(1));
					return ActionResultType.func_233537_a_(this.level.isClientSide);
				}
			
			if(!hasChest() && Block.getBlockFromItem(heldStack.getItem()) == Blocks.CHEST)
			{
				setChested(true);
				playChestEquipSound();
				if(!player.abilities.isCreativeMode)
					heldStack.shrink(1);
				
				initWargChest();
				return ActionResultType.func_233537_a_(this.level.isClientSide);
			}
			
			if(!isBeingRidden() && !player.isSecondaryUseActive())
			{
				if(!this.getEntityWorld().isClientSide && (isTamed() || player.isCreative()))
					player.startRiding(this);
				return ActionResultType.func_233537_a_(this.getEntityWorld().isClientSide);
			}
		}
		
		return super.func_230254_b_(player, hand);
	}
	
	protected void playChestEquipSound()
	{
		playSound(SoundEvents.ENTITY_DONKEY_CHEST, 1F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1F);
	}
	
	public boolean isSaddled(){ return getDataManager().get(SADDLE).booleanValue(); }
	
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
	
	public void onInventoryChanged(IInventory invBasic)
	{
		ItemStack armour = this.getItemStackFromSlot(EquipmentSlot.CHEST);
		updateArmour();
		if(this.ticksExisted > 20 && !getItemStackFromSlot(EquipmentSlot.CHEST).isEmpty() && armour.getItem() != getItemStackFromSlot(EquipmentSlot.CHEST).getItem())
			playSound(SoundEvents.ENTITY_HORSE_ARMOR, 0.5F, 1.0F);
		
		boolean saddled = isSaddled();
		updateSaddle();
		if(this.ticksExisted > 20 && !saddled && this.isSaddled())
			playSound(SoundEvents.ENTITY_HORSE_SADDLE, 0.5F, 1F);
		
		DyeColor carpet = getCarpetColor();
		updateCarpet();
		if(this.ticksExisted > 20 && getCarpetColor() != null && carpet != getCarpetColor())
			playSound(SoundEvents.ENTITY_LLAMA_SWAG, 0.5F, 1.0F);
	}
	
	private void updateArmour()
	{
		if(!this.level.isClientSide)
			this.setItemStackToSlot(EquipmentSlot.CHEST, this.wargChest.getStackInSlot(2).copy());
	}
	
	private void updateSaddle()
	{
		if(!this.level.isClientSide)
			getDataManager().set(SADDLE, !this.wargChest.getStackInSlot(0).isEmpty());
	}
	
	private void updateCarpet()
	{
		if(!this.level.isClientSide)
		{
			ItemStack carpet = this.wargChest.getStackInSlot(1);
			int color = carpet.isEmpty() ? -1 : ((CarpetBlock)Block.getBlockFromItem(carpet.getItem())).getColor().getId(); 
			getDataManager().set(CARPET, color);
		}
	}
}
