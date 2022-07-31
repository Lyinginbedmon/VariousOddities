package com.lying.variousoddities.entity;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.capabilities.PlayerData.SoulCondition;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.inventory.ContainerBody;
import com.lying.variousoddities.utility.VOHelper;
import com.mojang.authlib.GameProfile;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public abstract class AbstractBody extends LivingEntity implements IInventoryChangedListener
{
    protected static final DataParameter<CompoundTag> ENTITY	= EntityDataManager.<CompoundTag>createKey(AbstractBody.class, DataSerializers.COMPOUND_NBT);
    protected static final DataParameter<CompoundTag> PROFILE	= EntityDataManager.<CompoundTag>createKey(AbstractBody.class, DataSerializers.COMPOUND_NBT);
    protected static final DataParameter<CompoundTag> EQUIPMENT	= EntityDataManager.<CompoundTag>createKey(AbstractBody.class, DataSerializers.COMPOUND_NBT);
    protected static final DataParameter<Optional<UUID>> SOUL_ID	= EntityDataManager.<Optional<UUID>>createKey(AbstractBody.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    protected static final EntitySize BODY_SIZE = EntitySize.fixed(0.75F, 0.5F);
    
    protected final Inventory bodyInventory;
	
	/** True if this corpse should not despawn, even if its bound entity is not dead. */
	protected boolean persistent = false;
	
	public AbstractBody(EntityType<? extends AbstractBody> type, Level worldIn)
	{
		super(type, worldIn);
		this.bodyInventory = new Inventory(12);
		this.bodyInventory.addListener(this);
	}
	
	public void copyFrom(LivingEntity living, boolean withDropChances)
	{
		setBody(living, withDropChances);
		moveTo(living.getX(), living.getY(), living.getZ(), living.getYRot(), living.getXRot());
		setMotion(living.getMotion());
	}
	
	public void registerData()
	{
		super.registerData();
		getDataManager().register(ENTITY, new CompoundTag());
		getDataManager().register(PROFILE, new CompoundTag());
		getDataManager().register(EQUIPMENT, new CompoundTag());
		getDataManager().register(SOUL_ID, Optional.<UUID>empty());
	}
	
    public static AttributeSupplier.Builder createAttributes()
    {
        return LivingEntity.createLivingAttributes()
        		.add(Attributes.MAX_HEALTH, 10.0D);
    }
	
	public void readAdditional(CompoundTag compound)
	{
		super.readAdditionalSaveData(compound);
		getDataManager().set(ENTITY, compound.getCompound("Entity"));
		getDataManager().set(PROFILE, compound.getCompound("Player"));
		updateSize();
		this.persistent = compound.getBoolean("PersistenceRequired");
		readInventoryFromNBT(compound);
		if(compound.contains("SoulUUID", 11))
			setSoulUUID(compound.getUUID("SoulUUID"));
	}
	
	public void writeAdditional(CompoundTag compound)
	{
		super.writeAdditional(compound);
		compound.put("Entity", getDataManager().get(ENTITY));
		compound.put("Player", getDataManager().get(PROFILE));
		compound.putBoolean("PersistenceRequired", this.persistent);
		writeInventoryToNBT(compound);
		if(hasSoul())
			compound.putUUID("SoulUUID", getSoulUUID());
	}
	
	private CompoundTag writeInventoryToNBT(CompoundTag compound)
	{
		ListTag armourList = new ListTag();
		for(int i=0; i<4; i++)
		{
			CompoundTag stackData = new CompoundTag();
			ItemStack stack = this.bodyInventory.getStackInSlot(i);
			if(!stack.isEmpty())
				stack.save(stackData);
			armourList.add(stackData);
		}
		compound.put("ArmorItems", armourList);
		
		ListTag handList = new ListTag();
		for(int i=0; i<2; i++)
		{
			CompoundTag stackData = new CompoundTag();
			ItemStack stack = this.bodyInventory.getStackInSlot(4+i);
			if(!stack.isEmpty())
				stack.save(stackData);
			handList.add(stackData);
		}
		compound.put("HandItems", handList);
		
		ListTag inventoryList = new ListTag();
		for(int i=0; i<6; i++)
		{
			CompoundTag stackData = new CompoundTag();
			ItemStack stack = this.bodyInventory.getStackInSlot(6+i);
			if(!stack.isEmpty())
				stack.save(stackData);
			inventoryList.add(stackData);
		}
		compound.put("Inventory", inventoryList);
		
		return compound;
	}
	
	protected static CompoundTag writeInventoryToNBT(CompoundTag compound, Inventory armour, Inventory hands, Inventory bag)
	{
		if(armour != null)
			compound.put("ArmorItems", writeInventoryToList(new ListTag(), armour));
		if(hands != null)
			compound.put("HandItems", writeInventoryToList(new ListTag(), hands));
		if(bag != null)
			compound.put("Inventory", writeInventoryToList(new ListTag(), bag));
		return compound;
	}
	
	protected static ListTag writeInventoryToList(ListTag list, Inventory inv)
	{
		for(int i=0; i<inv.getSizeInventory(); i++)
		{
			CompoundTag stackData = new CompoundTag();
			ItemStack stack = inv.getStackInSlot(i);
			if(!stack.isEmpty())
				stack.save(stackData);
			list.add(stackData);
		}
		return list;
	}
	
	private void readInventoryFromNBT(CompoundTag compound)
	{
		ListTag armourList = compound.getList("ArmorItems", 10);
		armourList.addAll(compound.getList("HandItems", 10));
		armourList.addAll(compound.getList("Inventory", 10));
		for(int i=0; i<this.bodyInventory.getSizeInventory(); i++)
			this.bodyInventory.setInventorySlotContents(i, ItemStack.of(armourList.getCompound(i)));
	}
	
	public void onInventoryChanged(Inventory invBasic)
	{
		if(!getLevel().isClientSide)
			getDataManager().set(EQUIPMENT, writeInventoryToNBT(new CompoundTag()));
	}
	
	public static void clearNearbyAttackTargetsOf(@Nonnull LivingEntity victim)
	{
		/** 
		 * Clear nearby mob attack targets if they were targetting the owner of this corpse<br>
		 * This stops mobs attacking players they've already "killed"
		 */
		for(Mob entity : victim.getLevel().getEntitiesOfClass(Mob.class, victim.getBoundingBox().inflate(64D)))
			if(entity.getTarget() != null && entity.getTarget().equals(victim))
				entity.setTarget(null);
	}
	
	public Component getDisplayName(){ return this.hasBody() ? getBody().getDisplayName() : super.getDisplayName(); }
	
	public boolean hasBody(){ return getDataManager().get(ENTITY).contains("id", 8); }
	
	public boolean isPlayer(){ return hasBody() && getDataManager().get(ENTITY).getString("id").equalsIgnoreCase("player"); }
	
	public void setBody(@Nullable LivingEntity living, boolean withDropChance)
	{
		CompoundTag data = new CompoundTag();
		
		// Store entity equipment in body inventory
		if(living != null)
		{
			setSoulUUID(living.getUUID());
			
			RandomSource rand = living.getRandom();
			boolean checkDrop = living instanceof Monster && withDropChance;
			if(checkDrop && (living.isBaby() || !living.getLevel().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)))
				checkDrop = false;
			
			float[] armorChances = new float[4];
			float[] handChances = new float[2];
			Arrays.fill(armorChances, 0F);
			Arrays.fill(handChances, 0F);
			// Preserve equipment for mobs and player corpses
			if(stealsGear() || living instanceof Monster)
			{
				int slot = 0;
				for(ItemStack stack : living.getArmorInventoryList())
				{
					if(!checkDrop || checkDrop && rand.nextFloat() <= armorChances[slot])
					{
						this.bodyInventory.setInventorySlotContents(slot, stack.copy());
						living.setItemStackToSlot(EquipmentSlot.fromSlotTypeAndIndex(EquipmentSlot.Type.ARMOR, slot), ItemStack.EMPTY);
					}
					slot++;
				}
				
				int handSlot = 0;
				for(ItemStack stack : living.getHeldEquipment())
				{
					if(!checkDrop || checkDrop && rand.nextFloat() <= handChances[handSlot++])
					{
						this.bodyInventory.setInventorySlotContents(slot, stack.copy());
						living.setItemStackToSlot(EquipmentSlot.fromSlotTypeAndIndex(EquipmentSlot.Type.HAND, handSlot), ItemStack.EMPTY);
					}
					slot++;
				}
			}
			// Preserve equipment if it passes a mob drop chance check
			else if(checkDrop)
			{
				Monster mob = (Monster)living;
				CompoundTag mobData = new CompoundTag();
				mob.writeAdditional(mobData);
				
				if(mobData.contains("ArmorDropChances", 9))
				{
					ListTag armorList = mobData.getList("ArmorDropChances", 5);
					for(int i=0; i<armorList.size(); ++i)
						armorChances[i] = armorList.getFloat(i);
				}
				
				if(mobData.contains("HandDropChances", 5))
				{
					ListTag handList = mobData.getList("HandDropChances", 5);
					for(int i=0; i<handList.size(); ++i)
						handChances[i] = handList.getFloat(i);
				}
			}
			
			
			living.writeWithoutTypeId(data);
			if(living.getType() == EntityType.PLAYER)
			{
				data.putString("id", "player");
				getDataManager().set(PROFILE, NbtUtils.writeGameProfile(new CompoundTag(), ((Player)living).getGameProfile()));
			}
			else
				data.putString("id", living.getEntityString());
			
			if(data.contains("Passengers"))
				data.remove("Passengers");
		}
		else
		{
			this.bodyInventory.clear();
			setSoulUUID(null);
		}
		
		getDataManager().set(ENTITY, data);
		updateSize();
		onInventoryChanged(null);
	}
	
	/** Returns true if this body should remove items from its associated entity */
	public boolean stealsGear(){ return true; }
	
	public GameProfile getGameProfile()
	{
		return NbtUtils.readGameProfile(getDataManager().get(PROFILE));
	}
	
	public void setSoulUUID(UUID idIn){ getDataManager().set(SOUL_ID, Optional.<UUID>of(idIn)); }
	
	public UUID getSoulUUID()
	{
		Optional<UUID> id = getDataManager().get(SOUL_ID);
		return id.isPresent() ? id.get() : null;
	}
	
	public boolean hasSoul(){ return getSoulUUID() != null; }
	
	/** Returns the original creature, if they can be found in the world currently */
	@Nullable
	public LivingEntity getSoul()
	{
		if(!hasSoul())
			return null;
		
		if(isPlayer())
			return getLevel().getPlayerByUUID(getSoulUUID());
		else
			return getBody();
	}
	
	/**
	 * Returns the full details of the body, including the equipment of this entity.<br>
	 * Note that this is always a separate entity to the original creature.
	 */
	@Nullable
	public LivingEntity getBody()
	{
		CompoundTag data = getDataManager().get(ENTITY);
		if(data.isEmpty()) return null;
		
		String id = data.getString("id");
		Entity entity = null;
		if(id.equalsIgnoreCase("player"))
			entity = VOEntities.DUMMY_BIPED.create(getLevel());
		else
		{
			if(!EntityType.byKey(id).isPresent())
				return null;
			else
				entity = EntityType.byKey(id).get().create(getLevel());
		}
		
		if(entity != null)
		{
			entity.load(data);
			
			if(entity.getType() == VOEntities.DUMMY_BIPED)
				((EntityDummyBiped)entity).setGameProfile(getGameProfile());
			
			for(EquipmentSlot slot : EquipmentSlot.values())
				entity.setItemSlot(slot, getItemStackFromSlot(slot));
			
			if(entity instanceof LivingEntity)
				LivingData.forEntity((LivingEntity)entity).setPocketInventory(getPocketInventory());
		}
		
		return (LivingEntity)entity;
	}
	
	@Nullable
	public LivingEntity getBodyForRender()
	{
		if(isPlayer())
		{
			EntityDummyBiped dummy = VOEntities.DUMMY_BIPED.create(getLevel());
			dummy.setGameProfile(getGameProfile());
			return dummy;
		}
		return getBody();
	}
	
	public boolean isPersistenceRequired(){ return this.persistent; }
	
	public abstract boolean shouldBindIfPersistent();
	
	public static void moveWithinRangeOf(Entity body, LivingEntity soul, double range)
	{
		if(VOHelper.isCreativeOrSpectator(soul))
			return;
		if(range < 0D || (body instanceof AbstractBody && ((AbstractBody)body).isPersistenceRequired() && !((AbstractBody)body).shouldBindIfPersistent()))
			return;
		
//		Vector3d pos = body.getPositionVec();
//		Vector3d dest = soul.getPositionVec();
//		double dist = pos.distanceTo(dest);
//		if(dist <= range)
//			return;
//		
//		range = Math.min(dist, range);
//		Vector3d toBody = dest.subtract(pos).normalize();
//		dest = pos.add(toBody.mul(range, range, range));
//		
//		// If range is greater than 0, rotate soul to face body
//		if(range > 0D)
//		{
//			Vector3d fromBody = pos.subtract(dest);
//			float yaw = (float)Math.toDegrees(MathHelper.atan2(fromBody.z, fromBody.x)) - 90.0F;
//			soul.setPositionAndRotation(dest.x, dest.y, dest.z, yaw, soul.rotationPitch);
//		}
//		// If range is zero, just reposition soul
//		else
//			soul.func_242281_f(dest.x, dest.y, dest.z);
	}
	
	protected final void updateSize()
	{
		recalculateSize();
		recenterBoundingBox();
	}
	
	public Iterable<ItemStack> getArmorInventoryList()
	{
		NonNullList<ItemStack> stacks = NonNullList.withSize(4, ItemStack.EMPTY);
		for(int i=0; i<4; i++)
			stacks.set(i, getInventory().getStackInSlot(i));
		return stacks;
	}
	
	public Iterable<ItemStack> getHeldEquipment()
	{
		NonNullList<ItemStack> stacks = NonNullList.withSize(2, ItemStack.EMPTY);
		for(int i=0; i<2; i++)
			stacks.set(i, getInventory().getStackInSlot(4+i));
		return stacks;
	}
	
	public NonNullList<ItemStack> getPocketInventory()
	{
		NonNullList<ItemStack> stacks = NonNullList.withSize(6, ItemStack.EMPTY);
		for(int i=0; i<6; i++)
			stacks.set(i, getInventory().getStackInSlot(6+i));
		return stacks;
	}
	
	public void setPocketInventory(NonNullList<ItemStack> inventory)
	{
		getInventory();
		for(int i=0; i<6; i++)
			this.bodyInventory.setInventorySlotContents(i+6, inventory.get(i));
	}
	
	public Inventory getInventory()
	{
		if(getLevel().isClientSide)
			readInventoryFromNBT(getDataManager().get(EQUIPMENT));
		return this.bodyInventory;
	}
	
	protected void dropInventory()
	{
		for(int i=0; i<getInventory().getSizeInventory(); i++)
			entityDropItem(getInventory().getStackInSlot(i));
	}
	
	public ItemStack getItemStackFromSlot(EquipmentSlot slotIn)
	{
		if(getLevel().isClientSide)
			readInventoryFromNBT(getDataManager().get(EQUIPMENT));
		
		switch(slotIn)
		{
			case FEET:	return getInventory().getStackInSlot(0);
			case LEGS:	return getInventory().getStackInSlot(1);
			case CHEST:	return getInventory().getStackInSlot(2);
			case HEAD:	return getInventory().getStackInSlot(3);
			case MAINHAND:	return getInventory().getStackInSlot(4);
			case OFFHAND:	return getInventory().getStackInSlot(5);
		}
		return ItemStack.EMPTY;
	}
	
	public void setItemStackToSlot(EquipmentSlot slotIn, ItemStack stack)
	{
		playEquipSound(stack);
		setEquipmentInSlot(slotIn, stack);
	}
	
	protected void setEquipmentInSlot(EquipmentSlot slotIn, ItemStack stack)
	{
		this.bodyInventory.setInventorySlotContents(slotIn.getIndex() + (slotIn.getType() == EquipmentSlot.Type.HAND ? 4 : 0), stack);
		onInventoryChanged(null);
	}
	
	public HandSide getPrimaryHand()
	{
		if(hasBody())
			return getBody().getPrimaryHand();
		return HandSide.RIGHT;
	}
	
	public void tick()
	{
		super.tick();
		
		if(isPlayer())
		{
			Player player = (Player)getSoul();
			if(player != null)
			{
				SoulCondition condition = PlayerData.forPlayer(player).getSoulCondition();
				double dist = player.distanceTo(this);
				if(dist > condition.getWanderRange() && condition.getWanderRange() >= 0)
					player.moveTo(getX(), getY(), getZ());
			}
		}
		
		if(getLevel().isClientSide)
			return;
		
		if(getBody() != null)
		{
			EntitySize bodySize = getSize(Pose.STANDING);
			if(getHeight() != bodySize.height || getWidth() != bodySize.width)
				updateSize();
		}
	}
	
	public EntitySize getSize(Pose poseIn)
	{
		return hasBody() ? BODY_SIZE : super.getSize(poseIn);
	}
	
	public final ActionResultType processInitialInteract(Player player, InteractionHand hand)
	{
		ItemStack heldStack = player.getHeldItem(hand);
		if(!heldStack.isEmpty() && heldStack.getItem() instanceof SpawnEggItem)
		{
			SpawnEggItem egg = (SpawnEggItem)heldStack.getItem();
			EntityType<?> entityType = egg.getType(heldStack.getTag());
			Entity entity = entityType.create(player.getLevel());
			if(entity instanceof LivingEntity && !player.getLevel().isClientSide)
			{
				this.setBody((LivingEntity)entity, false);
				
				if(!player.abilities.isCreativeMode)
					heldStack.shrink(1);
				return ActionResultType.SUCCESS;
			}
		}
		else
			openContainer(player);
		return ActionResultType.PASS;
	}
	
	public void openContainer(Player playerIn)
	{
		playerIn.openContainer(new SimpleNamedContainerProvider((window, player, p1) -> new ContainerBody(window, player, getInventory(), this), this.getDisplayName()));
	}
}
