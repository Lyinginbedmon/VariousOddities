package com.lying.variousoddities.entity.mount;

import javax.annotation.Nullable;

import com.lying.variousoddities.entity.AbstractGoblinWolf;
import com.lying.variousoddities.entity.IMountInventory;
import com.lying.variousoddities.inventory.ContainerWarg;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarpetBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.IRideable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.DyeColor;
import net.minecraft.item.HorseArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Effects;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class EntityWarg extends AbstractGoblinWolf implements IRideable, IJumpingMount, IMountInventory, IInventoryChangedListener
{
	private static final DataParameter<Boolean> REARING	= EntityDataManager.<Boolean>createKey(EntityWarg.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> SITTING	= EntityDataManager.<Boolean>createKey(EntityWarg.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> CHEST	= EntityDataManager.<Boolean>createKey(EntityWarg.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> CARPET	= EntityDataManager.<Integer>createKey(EntityWarg.class, DataSerializers.VARINT);
	
	private float jumpPower = 0F;
	private boolean allowStandSliding;
	
	private boolean wargJumping;
	private int jumpRearingCounter;
	private float rearingAmount;
	@SuppressWarnings("unused")
	private float prevRearingAmount;
	
	public Inventory wargChest;
	
	public EntityWarg(EntityType<? extends EntityWarg> type, World worldIn)
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
		getDataManager().register(CARPET, -1);
	}
	
    public static AttributeModifierMap.MutableAttribute getAttributes()
    {
        return MobEntity.func_233666_p_()
        		.createMutableAttribute(Attributes.MAX_HEALTH, 40.0D)
        		.createMutableAttribute(Attributes.ARMOR, 7.0D)
        		.createMutableAttribute(Attributes.MOVEMENT_SPEED, (double)0.3002F)
        		.createMutableAttribute(Attributes.ATTACK_DAMAGE, 12.0D);
    }
	
	public void getAggressiveBehaviours()
	{
		this.addGeneticAI(3, new NearestAttackableTargetGoal<CowEntity>(this, CowEntity.class, true));
		this.addGeneticAI(3, new NearestAttackableTargetGoal<PigEntity>(this, PigEntity.class, true));
		this.addGeneticAI(3, new NearestAttackableTargetGoal<LlamaEntity>(this, LlamaEntity.class, true));
		this.addGeneticAI(3, new NearestAttackableTargetGoal<SheepEntity>(this, SheepEntity.class, true));
	}
	
	public AgeableEntity func_241840_a(ServerWorld arg0, AgeableEntity arg1)
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
    
    public void writeAdditional(CompoundNBT compound)
    {
    	super.writeAdditional(compound);
    	
    	compound.putBoolean("Sitting", isSitting());
    	compound.putBoolean("Chest", hasChest());
    	
		ListNBT inventory = new ListNBT();
		for(int i=0; i<this.wargChest.getSizeInventory(); ++i)
		{
			ItemStack stack = this.wargChest.getStackInSlot(i);
			if(!stack.isEmpty())
			{
				CompoundNBT stackData = new CompoundNBT();
				stackData.putByte("Slot", (byte)i);
				stack.write(stackData);
				inventory.add(stackData);
			}
		}
		compound.put("Inventory", inventory);
    }
    
    public void readAdditional(CompoundNBT compound)
    {
    	super.readAdditional(compound);
    	
    	func_233687_w_(compound.getBoolean("Sitting"));
    	setSleeping(isSitting());
    	
    	setChested(compound.getBoolean("Chest"));
		initWargChest();
		ListNBT inventory = compound.getList("Inventory", 10);
		for(int i=0; i<inventory.size(); ++i)
		{
			CompoundNBT stackData = inventory.getCompound(i);
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
	
	public void travelTowards(Vector3d travelVec)
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
	public boolean isSitting(){ return getDataManager().get(SITTING).booleanValue(); }
	
	@Override
	public void func_233687_w_(boolean sitting){ getDataManager().set(SITTING, sitting); }
	
	@Override
	public boolean isEntitySleeping() { return isSitting(); }
	
	public boolean isRiderControlling()
	{
		if(isSitting()) return false;
		if(getControllingPassenger() != null)
		{
			LivingEntity rider = (LivingEntity)getControllingPassenger();
			return Math.abs(rider.moveForward) > 0F || Math.abs(rider.moveStrafing) > 0F || this.jumpPower > 0F;
		}
		return false;
	}
	
	public void travel(Vector3d travelVector)
	{
		if(!this.isAlive())
			return;
		
		if(isBeingRidden() && canBeSteered())
		{
			LivingEntity entity = (LivingEntity)getControllingPassenger();
			
			// TODO Determine values to base motion on in lieu of direct rider control
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
				
				Vector3d motion = this.getMotion();
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
				super.travel(new Vector3d(strafe, travelVector.y, forward));
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
	
	public ActionResultType func_230254_b_(PlayerEntity player, Hand hand)
	{
		ItemStack heldStack = player.getHeldItem(hand);
		
		if(ItemTags.CARPETS.contains(heldStack.getItem()))
			if(this.wargChest != null && this.wargChest.getStackInSlot(1).isEmpty())
			{
				this.wargChest.setInventorySlotContents(1, heldStack.split(1));
				return ActionResultType.func_233537_a_(this.world.isRemote);
			}
		
		if(!isChild())
		{
			if(!isSaddled() && heldStack.getItem() == Items.SADDLE)
			{
				this.wargChest.setInventorySlotContents(0, heldStack.split(1));
				return ActionResultType.func_233537_a_(this.world.isRemote);
			}
			
			if(heldStack.getItem() instanceof HorseArmorItem)
				if(this.wargChest != null && this.wargChest.getStackInSlot(2).isEmpty())
				{
					this.wargChest.setInventorySlotContents(2, heldStack.split(1));
					return ActionResultType.func_233537_a_(this.world.isRemote);
				}
			
			if(!hasChest() && Block.getBlockFromItem(heldStack.getItem()) == Blocks.CHEST)
			{
				setChested(true);
				playChestEquipSound();
				if(!player.abilities.isCreativeMode)
					heldStack.shrink(1);
				
				initWargChest();
				return ActionResultType.func_233537_a_(this.world.isRemote);
			}
			
			if(!isBeingRidden() && !player.isSecondaryUseActive())
			{
				if(!this.getEntityWorld().isRemote && (isTamed() || player.isCreative()))
					player.startRiding(this);
				return ActionResultType.func_233537_a_(this.getEntityWorld().isRemote);
			}
		}
		
		return super.func_230254_b_(player, hand);
	}
	
	protected void playChestEquipSound()
	{
		playSound(SoundEvents.ENTITY_DONKEY_CHEST, 1F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1F);
	}
	
	public boolean isSaddled(){ return this.wargChest != null && !this.wargChest.getStackInSlot(0).isEmpty(); }
	
	@Nullable
	public Entity getControllingPassenger()
	{
		return this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
	}
	
	public boolean canBeSteered()
	{
		return getControllingPassenger() != null && getControllingPassenger().isAlive();
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
	
	public void openContainer(PlayerEntity playerIn)
	{
		playerIn.openContainer(new SimpleNamedContainerProvider((window, player, p1) -> new ContainerWarg(window, player, this.wargChest, this), this.getDisplayName()));
	}
	
	public void onInventoryChanged(IInventory invBasic)
	{
		ItemStack armour = this.getItemStackFromSlot(EquipmentSlotType.CHEST);
		updateArmour();
		if(this.ticksExisted > 20 && !getItemStackFromSlot(EquipmentSlotType.CHEST).isEmpty() && armour.getItem() != getItemStackFromSlot(EquipmentSlotType.CHEST).getItem())
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
		if(!this.world.isRemote)
			this.setItemStackToSlot(EquipmentSlotType.CHEST, this.wargChest.getStackInSlot(2).copy());
	}
	
	private void updateSaddle()
	{
		if(!this.world.isRemote)
			;
	}
	
	private void updateCarpet()
	{
		if(!this.world.isRemote)
		{
			ItemStack carpet = this.wargChest.getStackInSlot(1);
			int color = carpet.isEmpty() ? -1 : ((CarpetBlock)Block.getBlockFromItem(carpet.getItem())).getColor().getId(); 
			getDataManager().set(CARPET, color);
		}
	}
}
