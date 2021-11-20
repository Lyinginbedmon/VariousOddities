package com.lying.variousoddities.entity;

import java.util.Optional;

import javax.annotation.Nullable;

import com.lying.variousoddities.inventory.ContainerBody;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public abstract class AbstractBody extends LivingEntity implements IInventoryChangedListener
{
    private static final DataParameter<CompoundNBT> ENTITY	= EntityDataManager.<CompoundNBT>createKey(AbstractBody.class, DataSerializers.COMPOUND_NBT);
    private static final DataParameter<CompoundNBT> EQUIPMENT	= EntityDataManager.<CompoundNBT>createKey(AbstractBody.class, DataSerializers.COMPOUND_NBT);
    private static final EntitySize BODY_SIZE = EntitySize.fixed(0.75F, 0.5F);
    
    protected final Inventory bodyInventory;
	
	public AbstractBody(EntityType<? extends AbstractBody> type, World worldIn)
	{
		super(type, worldIn);
		this.bodyInventory = new Inventory(12);
		this.bodyInventory.addListener(this);
	}
	
	public void copyFrom(LivingEntity living)
	{
		setBody(living);
		setPositionAndRotation(living.getPosX(), living.getPosY(), living.getPosZ(), living.rotationYaw, living.rotationPitch);
		setMotion(living.getMotion());
	}
	
	public void registerData()
	{
		super.registerData();
		getDataManager().register(ENTITY, new CompoundNBT());
		getDataManager().register(EQUIPMENT, new CompoundNBT());
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
		updateSize();
		readInventoryFromNBT(compound);
	}
	
	public void writeAdditional(CompoundNBT compound)
	{
		super.writeAdditional(compound);
		compound.put("Entity", getDataManager().get(ENTITY));
		writeInventoryToNBT(compound);
	}
	
	private CompoundNBT writeInventoryToNBT(CompoundNBT compound)
	{
		ListNBT armourList = new ListNBT();
		ListNBT handList = new ListNBT();
		for(EquipmentSlotType slot : EquipmentSlotType.values())
		{
			CompoundNBT stackData = new CompoundNBT();
			ItemStack stack = getItemStackFromSlot(slot);
			if(!stack.isEmpty())
				stack.write(stackData);
			
			if(slot.getSlotType() == EquipmentSlotType.Group.ARMOR)
				armourList.add(stackData);
			else
				handList.add(stackData);
		}
		compound.put("ArmorItems", armourList);
		compound.put("HandItems", handList);
		
		ListNBT inventoryList = new ListNBT();
		for(int i=EquipmentSlotType.values().length; i<this.bodyInventory.getSizeInventory(); i++)
		{
			CompoundNBT stackData = new CompoundNBT();
			ItemStack stack = this.bodyInventory.getStackInSlot(i);
			if(!stack.isEmpty())
				stack.write(stackData);
			inventoryList.add(stackData);
		}
		compound.put("Inventory", inventoryList);
		
		return compound;
	}
	
	private void readInventoryFromNBT(CompoundNBT compound)
	{
		ListNBT armourList = compound.getList("ArmorItems", 10);
		ListNBT handList = compound.getList("HandItems", 10);
		ListNBT inventoryList = compound.getList("Inventory", 10);
		
		for(int i=0; i<armourList.size(); i++)
			this.bodyInventory.setInventorySlotContents(i, ItemStack.read(armourList.getCompound(i)));
		
		for(int i=0; i<handList.size(); i++)
			this.bodyInventory.setInventorySlotContents(i+4, ItemStack.read(handList.getCompound(i)));
		
		for(int i=0; i<inventoryList.size(); i++)
			this.bodyInventory.setInventorySlotContents(i+6, ItemStack.read(inventoryList.getCompound(i)));
	}
	
	public void onInventoryChanged(IInventory invBasic)
	{
		if(!getEntityWorld().isRemote)
			getDataManager().set(EQUIPMENT, writeInventoryToNBT(new CompoundNBT()));
	}
	
	public boolean hasBody(){ return getBody() != null; }
	
	public void setBody(@Nullable LivingEntity living)
	{
		CompoundNBT data = new CompoundNBT();
		
		// Store entity equipment in body inventory
		for(EquipmentSlotType slot : EquipmentSlotType.values())
		{
			this.bodyInventory.setInventorySlotContents(slot.getSlotIndex() + (slot.getSlotType() == EquipmentSlotType.Group.HAND ? 4 : 0), living == null ? ItemStack.EMPTY : living.getItemStackFromSlot(slot).copy());
			if(living != null)
				living.setItemStackToSlot(slot, ItemStack.EMPTY);
		}
		
		if(living != null)
		{
			living.writeWithoutTypeId(data);
			data.putString("id", living.getEntityString());
			
			if(data.contains("Passengers"))
				data.remove("Passengers");
		}
		
		getDataManager().set(ENTITY, data);
		updateSize();
		onInventoryChanged(null);
	}
	
	@Nullable
	public LivingEntity getBody()
	{
		CompoundNBT data = getDataManager().get(ENTITY);
		if(data.isEmpty()) return null;
		
		Optional<EntityType<?>> type = EntityType.byKey(data.getString("id"));
		if(!type.isPresent()) return null;
		
		Entity entity = type.get().create(getEntityWorld());
		entity.read(data);
		
		// Equip entity with equipment from body inventory
		for(EquipmentSlotType slot : EquipmentSlotType.values())
			entity.setItemStackToSlot(slot, getItemStackFromSlot(slot));
		
		return (LivingEntity)entity;
	}
	
	public boolean isPlayer(){ return getBody() != null && getBody().getType() == EntityType.PLAYER; }
	
	private void updateSize()
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
		for(int i=4; i<6; i++)
			stacks.set(i, getInventory().getStackInSlot(i));
		return stacks;
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
		return this.bodyInventory.getStackInSlot(slotIn.getSlotIndex() + (slotIn.getSlotType() == Group.HAND ? 4 : 0));
	}
	
	public void setItemStackToSlot(EquipmentSlotType slotIn, ItemStack stack)
	{
		playEquipSound(stack);
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
			LivingEntity body = getBody();
			if(body.getType() == EntityType.PLAYER && getEntityWorld().getPlayerByUuid(body.getUniqueID()) != null)
				setDead();
			
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
				this.setBody((LivingEntity)entity);
				
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
