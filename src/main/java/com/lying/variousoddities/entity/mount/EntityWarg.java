package com.lying.variousoddities.entity.mount;

import javax.annotation.Nullable;

import com.lying.variousoddities.entity.AbstractGoblinWolf;
import com.lying.variousoddities.entity.IMountInventory;
import com.lying.variousoddities.inventory.ContainerWarg;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.phys.Vec3;

public class EntityWarg extends AbstractGoblinWolf implements PlayerRideableJumping, IMountInventory, ContainerListener
{
	private static final EntityDataAccessor<Boolean> REARING	= SynchedEntityData.defineId(EntityWarg.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> SITTING	= SynchedEntityData.defineId(EntityWarg.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> CHEST	= SynchedEntityData.defineId(EntityWarg.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> SADDLE	= SynchedEntityData.defineId(EntityWarg.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> CARPET	= SynchedEntityData.defineId(EntityWarg.class, EntityDataSerializers.INT);
	
	private float playerJumpPendingScale = 0F;
	private boolean allowStandSliding;
	
	private boolean wargJumping;
	private int standCounter;
	private float standingAmount;
	@SuppressWarnings("unused")
	private float prevStandingAmount;
	
	public SimpleContainer wargChest;
	
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
		SimpleContainer inventory = this.wargChest;
		this.wargChest = new SimpleContainer(getContainerSize());
		if(inventory != null)
		{
			inventory.removeListener(this);
			int i = Math.min(inventory.getContainerSize(), this.wargChest.getContainerSize());
			for(int j = 0; j < i; ++j)
			{
				ItemStack stack = inventory.getItem(j);
				if(!stack.isEmpty())
					this.wargChest.setItem(j, stack.copy());
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
		for(int i=0; i<this.wargChest.getContainerSize(); ++i)
		{
			ItemStack stack = this.wargChest.getItem(i);
			if(!stack.isEmpty())
			{
				CompoundTag stackData = new CompoundTag();
				stackData.putByte("Slot", (byte)i);
				stack.save(stackData);
				inventory.add(stackData);
			}
		}
		compound.put("Inventory", inventory);
    }
    
    public void readAdditionalSaveData(CompoundTag compound)
    {
    	super.readAdditionalSaveData(compound);
    	
    	setOrderedToSit(compound.getBoolean("Sitting"));
    	setInSittingPose(isOrderedToSit());
    	
    	setChested(compound.getBoolean("Chest"));
		initWargChest();
		ListTag inventory = compound.getList("Inventory", 10);
		for(int i=0; i<inventory.size(); ++i)
		{
			CompoundTag stackData = inventory.getCompound(i);
			int slot = stackData.getByte("Slot") & 255;
			if(slot >= 0 && slot < this.wargChest.getContainerSize())
				this.wargChest.setItem(slot, ItemStack.of(stackData));
		}
    }
    
    public boolean isTamed(){ return true; }
	
	public boolean hasChest(){ return getEntityData().get(CHEST).booleanValue(); }
	
	public void setChested(boolean bool){ getEntityData().set(CHEST, bool); }
	
	public int getContainerSize(){ return 3 + inventoryColumns() * 3; }
	
	public int inventoryColumns(){ return 5; }
	
	protected void dropEquipment()
	{
		super.dropEquipment();
		if(this.wargChest != null)
			for(int i=0; i<this.wargChest.getContainerSize(); ++i)
			{
				ItemStack itemStack = this.wargChest.getItem(i);
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
	
	public boolean isStanding()
	{
		return getEntityData().get(REARING).booleanValue();
	}
	
	public void setStanding(boolean rearing)
	{
		getEntityData().set(REARING, rearing);
	}
	
	private void stand()
	{
		if(isControlledByLocalInstance() || this.isEffectiveAi())
		{
			this.standCounter = 1;
			this.setStanding(true);
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
			return Math.abs(rider.zza) > 0F || Math.abs(rider.xxa) > 0F || this.playerJumpPendingScale > 0F;
		}
		return false;
	}
	
	public void travel(Vec3 travelVector)
	{
		if(!this.isAlive())
			return;
		
		if(isVehicle() && canBeSteered())
		{
			LivingEntity entity = (LivingEntity)getControllingPassenger();
			
			float forward = 0F;
			float strafe = 0F;
			
			if(isRiderControlling())
			{
				forward = entity.zza;
				strafe = entity.xxa * 0.5F;
			}
			this.setYRot(entity.getYRot());
			this.yRotO = this.getYRot();
			this.setXRot(entity.getXRot() * 0.5F);
			this.setRot(this.getYRot(), this.getXRot());
			this.yBodyRot = this.getYRot();
			this.yHeadRot = this.yBodyRot;
			
			if(this.onGround && this.playerJumpPendingScale == 0F && this.isStanding() && !this.allowStandSliding)
				strafe = forward = 0F;
			
			if(this.playerJumpPendingScale > 0F && !this.isJumping() && this.onGround)
			{
				double jump = this.playerJumpPendingScale * this.getBlockJumpFactor();
				double boost;
				if(hasEffect(MobEffects.JUMP))
					boost = jump + (double)((float)(getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.1F);
				else
					boost = jump;
				
				Vec3 motion = this.getDeltaMovement();
				this.setDeltaMovement(motion.x, boost, motion.z);
				this.setJumping(true);
				this.hasImpulse = true;
				net.minecraftforge.common.ForgeHooks.onLivingJump(this);
				if(forward > 0F || !isRiderControlling())
				{
					float sine = Mth.sin(this.getYRot() * ((float)Math.PI / 180F));
					float cosine = Mth.cos(this.getYRot() * ((float)Math.PI / 180F));
					this.setDeltaMovement(this.getDeltaMovement().add(-0.4F * sine * this.playerJumpPendingScale, 0D, 0.4F * cosine * this.playerJumpPendingScale));
				}
				
				this.playerJumpPendingScale = 0F;
			}
			
			this.flyingSpeed = this.getSpeed() * 0.1F;
			if(isControlledByLocalInstance())
			{
				this.setSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED));
				super.travel(new Vec3(strafe, travelVector.y, forward));
			}
			
			if(this.onGround)
			{
				this.playerJumpPendingScale = 0F;
				this.setJumping(false);
			}
			this.calculateEntityAnimation(this, false);
			return;
		}
		
		this.flyingSpeed = 0.02F;
		super.travel(travelVector);
	}
	
	public float getMountedSpeed()
	{
		return (float)getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.225F;
	}
	
	public InteractionResult mobInteract(Player player, InteractionHand hand)
	{
		ItemStack heldStack = player.getItemInHand(hand);
		
		if(heldStack.is(ItemTags.WOOL_CARPETS))
			if(this.wargChest != null && this.wargChest.getItem(1).isEmpty())
			{
				this.wargChest.setItem(1, heldStack.split(1));
				return InteractionResult.sidedSuccess(this.level.isClientSide);
			}
		
		if(!isBaby())
		{
			if(!isSaddled() && heldStack.getItem() == Items.SADDLE)
			{
				this.wargChest.setItem(0, heldStack.split(1));
				return InteractionResult.sidedSuccess(this.level.isClientSide);
			}
			
			if(heldStack.getItem() instanceof HorseArmorItem)
				if(this.wargChest != null && this.wargChest.getItem(2).isEmpty())
				{
					this.wargChest.setItem(2, heldStack.split(1));
					return InteractionResult.sidedSuccess(this.level.isClientSide);
				}
			
			if(!hasChest() && Block.byItem(heldStack.getItem()) == Blocks.CHEST)
			{
				setChested(true);
				playChestEquipSound();
				if(!player.canUseGameMasterBlocks())
					heldStack.shrink(1);
				
				initWargChest();
				return InteractionResult.sidedSuccess(this.level.isClientSide);
			}
			
			if(!isVehicle() && !player.isSecondaryUseActive())
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
		return isSaddled() && rider != null && rider.isAlive() && (rider.zza != 0F || rider.xxa != 0F);
	}
	
	public void tick()
	{
		super.tick();
		
		if((this.isControlledByLocalInstance() || this.isEffectiveAi()) && this.standCounter > 0 && ++this.standCounter > 20)
		{
			this.standCounter = 0;
			this.setStanding(false);
		}
		
		this.prevStandingAmount = this.standingAmount;
		if(this.isStanding())
		{
			this.standingAmount += (1F - this.standingAmount) * 0.4F + 0.05F;
			if(this.standingAmount > 1F)
				this.standingAmount = 1F;
		}
		else
		{
			this.allowStandSliding = false;
			this.standingAmount += (0.8F * this.standingAmount * this.standingAmount * this.standingAmount - this.standingAmount) * 0.6F - 0.5F;
			if(this.standingAmount < 0F)
				this.standingAmount = 0F;
		}
	}
	
	public void onPlayerJump(int jumpPowerIn)
	{
		if(this.isSaddled())
		{
			if(jumpPowerIn < 0)
				jumpPowerIn = 0;
			else
			{
				this.allowStandSliding = true;
				this.stand();
			}
			
			if(jumpPowerIn >= 90)
				this.playerJumpPendingScale = 1F;
			else
				this.playerJumpPendingScale = 0.4F + 0.4F * (float)jumpPowerIn / 90F;
		}
	}
	
	public boolean canJump()
	{
		return isControlledByLocalInstance();
	}
	
	public void handleStartJump(int jumpPower)
	{
		this.allowStandSliding = true;
		this.stand();
	}
	
	public void handleStopJump()
	{
		
	}
	
	public void openContainer(Player playerIn)
	{
		Container container = this.wargChest;
		EntityWarg warg = this;
		Component displayName = this.getDisplayName();
		playerIn.openMenu(new MenuProvider()
		{
			public AbstractContainerMenu createMenu(int window, Inventory player, Player p1){ return new ContainerWarg(window, player, container, warg); }
			public Component getDisplayName(){ return displayName; }
		});
	}
	
	public void containerChanged(Container inv)
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
			this.setItemSlot(EquipmentSlot.CHEST, this.wargChest.getItem(2).copy());
	}
	
	private void updateSaddle()
	{
		if(!this.level.isClientSide)
			getEntityData().set(SADDLE, !this.wargChest.getItem(0).isEmpty());
	}
	
	private void updateCarpet()
	{
		if(!this.level.isClientSide)
		{
			ItemStack carpet = this.wargChest.getItem(1);
			int color = carpet.isEmpty() ? -1 : ((WoolCarpetBlock)Block.byItem(carpet.getItem())).getColor().getId(); 
			getEntityData().set(CARPET, color);
		}
	}
}
