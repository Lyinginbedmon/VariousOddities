package com.lying.variousoddities.entity;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.inventory.ContainerBody;
import com.lying.variousoddities.utility.VOHelper;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.EquipmentSlotType.Group;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public abstract class AbstractBody extends LivingEntity implements IInventoryChangedListener
{
	public static final AxisAlignedBB ENTIRE_WORLD = new AxisAlignedBB(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
    protected static final DataParameter<CompoundNBT> ENTITY	= EntityDataManager.<CompoundNBT>createKey(AbstractBody.class, DataSerializers.COMPOUND_NBT);
    protected static final DataParameter<CompoundNBT> PROFILE	= EntityDataManager.<CompoundNBT>createKey(AbstractBody.class, DataSerializers.COMPOUND_NBT);
    protected static final DataParameter<CompoundNBT> EQUIPMENT	= EntityDataManager.<CompoundNBT>createKey(AbstractBody.class, DataSerializers.COMPOUND_NBT);
    protected static final DataParameter<Optional<UUID>> SOUL_ID	= EntityDataManager.<Optional<UUID>>createKey(AbstractBody.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    protected static final EntitySize BODY_SIZE = EntitySize.fixed(0.75F, 0.5F);
    
    protected final Inventory bodyInventory;
	
	/** True if this corpse should not despawn, even if its bound entity is not dead. */
	protected boolean persistent = false;
	
	public AbstractBody(EntityType<? extends AbstractBody> type, World worldIn)
	{
		super(type, worldIn);
		this.bodyInventory = new Inventory(12);
		this.bodyInventory.addListener(this);
	}
	
	public void copyFrom(LivingEntity living, boolean withDropChances)
	{
		setBody(living, withDropChances);
		setPositionAndRotation(living.getPosX(), living.getPosY(), living.getPosZ(), living.rotationYaw, living.rotationPitch);
		setMotion(living.getMotion());
	}
	
	public void registerData()
	{
		super.registerData();
		getDataManager().register(ENTITY, new CompoundNBT());
		getDataManager().register(PROFILE, new CompoundNBT());
		getDataManager().register(EQUIPMENT, new CompoundNBT());
		getDataManager().register(SOUL_ID, Optional.<UUID>empty());
	}
	
    public static AttributeModifierMap.MutableAttribute getAttributes()
    {
        return LivingEntity.registerAttributes()
        		.createMutableAttribute(Attributes.MAX_HEALTH, 10.0D);
    }
	
	public void readAdditional(CompoundNBT compound)
	{
		super.readAdditional(compound);
		getDataManager().set(ENTITY, compound.getCompound("Entity"));
		getDataManager().set(PROFILE, compound.getCompound("Player"));
		updateSize();
		this.persistent = compound.getBoolean("PersistenceRequired");
		readInventoryFromNBT(compound);
		if(compound.contains("SoulUUID", 11))
			setSoulUUID(compound.getUniqueId("SoulUUID"));
	}
	
	public void writeAdditional(CompoundNBT compound)
	{
		super.writeAdditional(compound);
		compound.put("Entity", getDataManager().get(ENTITY));
		compound.put("Player", getDataManager().get(PROFILE));
		compound.putBoolean("PersistenceRequired", this.persistent);
		writeInventoryToNBT(compound);
		if(hasSoul())
			compound.putUniqueId("SoulUUID", getSoulUUID());
	}
	
	private CompoundNBT writeInventoryToNBT(CompoundNBT compound)
	{
		ListNBT armourList = new ListNBT();
		for(int i=0; i<4; i++)
		{
			CompoundNBT stackData = new CompoundNBT();
			ItemStack stack = this.bodyInventory.getStackInSlot(i);
			if(!stack.isEmpty())
				stack.write(stackData);
			armourList.add(stackData);
		}
		compound.put("ArmorItems", armourList);
		
		ListNBT handList = new ListNBT();
		for(int i=0; i<2; i++)
		{
			CompoundNBT stackData = new CompoundNBT();
			ItemStack stack = this.bodyInventory.getStackInSlot(4+i);
			if(!stack.isEmpty())
				stack.write(stackData);
			handList.add(stackData);
		}
		compound.put("HandItems", handList);
		
		ListNBT inventoryList = new ListNBT();
		for(int i=0; i<6; i++)
		{
			CompoundNBT stackData = new CompoundNBT();
			ItemStack stack = this.bodyInventory.getStackInSlot(6+i);
			if(!stack.isEmpty())
				stack.write(stackData);
			inventoryList.add(stackData);
		}
		compound.put("Inventory", inventoryList);
		
		return compound;
	}
	
	protected static CompoundNBT writeInventoryToNBT(CompoundNBT compound, IInventory armour, IInventory hands, IInventory bag)
	{
		if(armour != null)
			compound.put("ArmorItems", writeInventoryToList(new ListNBT(), armour));
		if(hands != null)
			compound.put("HandItems", writeInventoryToList(new ListNBT(), hands));
		if(bag != null)
			compound.put("Inventory", writeInventoryToList(new ListNBT(), bag));
		return compound;
	}
	
	protected static ListNBT writeInventoryToList(ListNBT list, IInventory inv)
	{
		for(int i=0; i<inv.getSizeInventory(); i++)
		{
			CompoundNBT stackData = new CompoundNBT();
			ItemStack stack = inv.getStackInSlot(i);
			if(!stack.isEmpty())
				stack.write(stackData);
			list.add(stackData);
		}
		return list;
	}
	
	private void readInventoryFromNBT(CompoundNBT compound)
	{
		ListNBT armourList = compound.getList("ArmorItems", 10);
		armourList.addAll(compound.getList("HandItems", 10));
		armourList.addAll(compound.getList("Inventory", 10));
		for(int i=0; i<this.bodyInventory.getSizeInventory(); i++)
			this.bodyInventory.setInventorySlotContents(i, ItemStack.read(armourList.getCompound(i)));
	}
	
	public void onInventoryChanged(IInventory invBasic)
	{
		if(!getEntityWorld().isRemote)
			getDataManager().set(EQUIPMENT, writeInventoryToNBT(new CompoundNBT()));
	}
	
	public static void clearNearbyAttackTargetsOf(@Nonnull LivingEntity victim)
	{
		/** 
		 * Clear nearby mob attack targets if they were targetting the owner of this corpse<br>
		 * This stops mobs attacking players they've already "killed"
		 */
		for(MobEntity entity : victim.getEntityWorld().getEntitiesWithinAABB(MobEntity.class, victim.getBoundingBox().grow(64D)))
			if(entity.getAttackTarget() != null && entity.getAttackTarget().equals(victim))
				entity.setAttackTarget(null);
	}
	
	public ITextComponent getDisplayName(){ return this.hasBody() ? getBody().getDisplayName() : super.getDisplayName(); }
	
	public boolean hasBody(){ return getDataManager().get(ENTITY).contains("id", 8); }
	
	public boolean isPlayer(){ return hasBody() && getDataManager().get(ENTITY).getString("id").equalsIgnoreCase("player"); }
	
	public void setBody(@Nullable LivingEntity living, boolean withDropChance)
	{
		CompoundNBT data = new CompoundNBT();
		
		// Store entity equipment in body inventory
		if(living != null)
		{
			setSoulUUID(living.getUniqueID());
			
			Random rand = living.getRNG();
			boolean checkDrop = living instanceof MobEntity && withDropChance;
			if(checkDrop)
				if(!living.isChild() && living.getEntityWorld().getGameRules().getBoolean(GameRules.DO_MOB_LOOT))
					;
				else
					checkDrop = false;
			
			// FIXME Ensure equipment is preserved appropriately
			float[] armorChances = new float[4];
			float[] handChances = new float[2];
			Arrays.fill(armorChances, 0F);
			Arrays.fill(handChances, 0F);
			if(checkDrop)
			{
				MobEntity mob = (MobEntity)living;
				CompoundNBT mobData = new CompoundNBT();
				mob.writeAdditional(mobData);
				
				if(mobData.contains("ArmorDropChances", 9))
				{
					ListNBT armorList = mobData.getList("ArmorDropChances", 5);
					for(int i=0; i<armorList.size(); ++i)
						armorChances[i] = armorList.getFloat(i);
				}
				
				if(mobData.contains("HandDropChances", 5))
				{
					ListNBT handList = mobData.getList("HandDropChances", 5);
					for(int i=0; i<handList.size(); ++i)
						handChances[i] = handList.getFloat(i);
				}
			}
			
			if(stealsGear())
			{
				int slot = 0;
				for(ItemStack stack : living.getArmorInventoryList())
				{
					if(!checkDrop || checkDrop && rand.nextFloat() <= armorChances[slot])
					{
						this.bodyInventory.setInventorySlotContents(slot, stack.copy());
						living.setItemStackToSlot(EquipmentSlotType.fromSlotTypeAndIndex(Group.ARMOR, slot), ItemStack.EMPTY);
					}
					slot++;
				}
				
				int handSlot = 0;
				for(ItemStack stack : living.getHeldEquipment())
				{
					if(!checkDrop || checkDrop && rand.nextFloat() <= handChances[handSlot++])
					{
						this.bodyInventory.setInventorySlotContents(slot, stack.copy());
						living.setItemStackToSlot(EquipmentSlotType.fromSlotTypeAndIndex(Group.HAND, handSlot), ItemStack.EMPTY);
					}
					slot++;
				}
			}
			
			living.writeWithoutTypeId(data);
			if(living.getType() == EntityType.PLAYER)
			{
				data.putString("id", "player");
				getDataManager().set(PROFILE, NBTUtil.writeGameProfile(new CompoundNBT(), ((PlayerEntity)living).getGameProfile()));
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
	
	public boolean stealsGear(){ return true; }
	
	public GameProfile getGameProfile()
	{
		return NBTUtil.readGameProfile(getDataManager().get(PROFILE));
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
			return getEntityWorld().getPlayerByUuid(getSoulUUID());
		else
		{
			return getBody();
//			for(LivingEntity living : getEntityWorld().getLoadedEntitiesWithinAABB(LivingEntity.class, ENTIRE_WORLD))
//				if(living.getUniqueID() == getSoulUUID())
//					return living;
		}
		
//		return null;
	}
	
	/**
	 * Returns the full details of the body, including the equipment of this entity.<br>
	 * Note that this is always a separate entity to the original creature.
	 */
	@Nullable
	public LivingEntity getBody()
	{
		CompoundNBT data = getDataManager().get(ENTITY);
		if(data.isEmpty()) return null;
		
		String id = data.getString("id");
		Entity entity = null;
		if(id.equalsIgnoreCase("player"))
			entity = VOEntities.DUMMY_BIPED.create(getEntityWorld());
		else
		{
			if(!EntityType.byKey(id).isPresent())
				return null;
			else
				entity = EntityType.byKey(id).get().create(getEntityWorld());
		}
		
		if(entity != null)
		{
			entity.read(data);
			
			if(entity.getType() == VOEntities.DUMMY_BIPED)
				((EntityDummyBiped)entity).setGameProfile(getGameProfile());
			
			for(EquipmentSlotType slot : EquipmentSlotType.values())
				entity.setItemStackToSlot(slot, getItemStackFromSlot(slot));
			
			if(entity instanceof LivingEntity)
				LivingData.forEntity((LivingEntity)entity).setPocketInventory(getPocketInventory());
		}
		
		return (LivingEntity)entity;
	}
	
	@Nullable
	public LivingEntity getBodyForRender()
	{
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
		
		Vector3d pos = body.getPositionVec();
		Vector3d dest = soul.getPositionVec();
		double dist = pos.distanceTo(dest);
		if(dist <= range)
			return;
		
		range = Math.min(dist, range);
		Vector3d toBody = dest.subtract(pos).normalize();
		dest = pos.add(toBody.mul(range, range, range));
		
		// If range is greater than 0, rotate soul to face body
		if(range > 0D)
		{
			Vector3d fromBody = pos.subtract(dest);
			float yaw = (float)Math.toDegrees(MathHelper.atan2(fromBody.z, fromBody.x)) - 90.0F;
			soul.setPositionAndRotation(dest.x, dest.y, dest.z, yaw, soul.rotationPitch);
		}
		// If range is zero, just reposition soul
		else
			soul.func_242281_f(dest.x, dest.y, dest.z);
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
	
	public IInventory getInventory()
	{
		if(getEntityWorld().isRemote)
			readInventoryFromNBT(getDataManager().get(EQUIPMENT));
		return this.bodyInventory;
	}
	
	protected void dropInventory()
	{
		for(int i=0; i<getInventory().getSizeInventory(); i++)
			entityDropItem(getInventory().getStackInSlot(i));
	}
	
	public ItemStack getItemStackFromSlot(EquipmentSlotType slotIn)
	{
		if(getEntityWorld().isRemote)
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
	
	public void setItemStackToSlot(EquipmentSlotType slotIn, ItemStack stack)
	{
		playEquipSound(stack);
		setEquipmentInSlot(slotIn, stack);
	}
	
	protected void setEquipmentInSlot(EquipmentSlotType slotIn, ItemStack stack)
	{
		this.bodyInventory.setInventorySlotContents(slotIn.getSlotIndex() + (slotIn.getSlotType() == Group.HAND ? 4 : 0), stack);
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
		
		if(getEntityWorld().isRemote)
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
	
	public final ActionResultType processInitialInteract(PlayerEntity player, Hand hand)
	{
		ItemStack heldStack = player.getHeldItem(hand);
		if(!heldStack.isEmpty() && heldStack.getItem() instanceof SpawnEggItem)
		{
			SpawnEggItem egg = (SpawnEggItem)heldStack.getItem();
			EntityType<?> entityType = egg.getType(heldStack.getTag());
			Entity entity = entityType.create(player.getEntityWorld());
			if(entity instanceof LivingEntity && !player.getEntityWorld().isRemote)
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
	
	public void openContainer(PlayerEntity playerIn)
	{
		playerIn.openContainer(new SimpleNamedContainerProvider((window, player, p1) -> new ContainerBody(window, player, getInventory(), this), this.getDisplayName()));
	}
}
