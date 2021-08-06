package com.lying.variousoddities.entity;

import java.util.Optional;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lying.variousoddities.init.VOEntities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class EntityCorpse extends LivingEntity
{
    private static final DataParameter<CompoundNBT> ENTITY	= EntityDataManager.<CompoundNBT>createKey(EntityCorpse.class, DataSerializers.COMPOUND_NBT);
    private static final EntitySize BODY_SIZE = EntitySize.fixed(0.75F, 0.5F);
    private final NonNullList<ItemStack> inventoryArmor = NonNullList.withSize(4, ItemStack.EMPTY);
	
	public EntityCorpse(EntityType<? extends EntityCorpse> type, World worldIn)
	{
		super(type, worldIn);
	}
    
    public static boolean canSpawnAt(EntityType<?> animal, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
	    return true;
    }
	
	@Nullable
	public static EntityCorpse createCorpseFrom(@Nonnull LivingEntity living)
	{
		if(living == null) return null;
		EntityCorpse corpse = new EntityCorpse(VOEntities.CORPSE, living.getEntityWorld());
		corpse.setBody(living);
		corpse.setPositionAndRotation(living.getPosX(), living.getPosY(), living.getPosZ(), living.rotationYaw, living.rotationPitch);
		corpse.setMotion(living.getMotion());
		return corpse;
	}
	
	public void registerData()
	{
		super.registerData();
		getDataManager().register(ENTITY, new CompoundNBT());
	}
	
    public boolean isNoDespawnRequired(){ return true; }
	
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
	}
	
	public void writeAdditional(CompoundNBT compound)
	{
		super.writeAdditional(compound);
		compound.put("Entity", getDataManager().get(ENTITY));
	}
	
	public boolean hasBody(){ return getBody() != null; }
	
	public void setBody(@Nullable LivingEntity living)
	{
		CompoundNBT data = new CompoundNBT();
		if(living != null)
		{
			living.writeWithoutTypeId(data);
			data.putString("id", living.getEntityString());
			
			if(data.contains("Passengers"))
				data.remove("Passengers");
		}
		getDataManager().set(ENTITY, data);
		updateSize();
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
		return (LivingEntity)entity;
	}
	
	private void updateSize()
	{
		recalculateSize();
		recenterBoundingBox();
	}
	
	public Iterable<ItemStack> getArmorInventoryList()
	{
		return inventoryArmor;
	}
	
	public ItemStack getItemStackFromSlot(EquipmentSlotType slotIn)
	{
		return ItemStack.EMPTY;
	}
	
	public void setItemStackToSlot(EquipmentSlotType slotIn, ItemStack stack)
	{
		
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
		return ActionResultType.PASS;
	}
}
