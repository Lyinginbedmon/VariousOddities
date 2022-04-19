package com.lying.variousoddities.entity;

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.capabilities.PlayerData.BodyCondition;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.inventory.ContainerPlayerBody;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class EntityBodyUnconscious extends AbstractBody
{
    protected static final DataParameter<CompoundNBT> LAST_KNOWN_EQUIPMENT	= EntityDataManager.<CompoundNBT>createKey(EntityBodyUnconscious.class, DataSerializers.COMPOUND_NBT);
	private final NonNullList<ItemStack> lastKnownArmour = NonNullList.<ItemStack>withSize(4, ItemStack.EMPTY);
	private final NonNullList<ItemStack> lastKnownEquip = NonNullList.<ItemStack>withSize(2, ItemStack.EMPTY);
	
	public EntityBodyUnconscious(EntityType<? extends EntityBodyUnconscious> type, World worldIn)
	{
		super(type, worldIn);
	}
    
    public static boolean canSpawnAt(EntityType<?> animal, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
	    return true;
    }
	
	public void registerData()
	{
		super.registerData();
		getDataManager().register(LAST_KNOWN_EQUIPMENT, new CompoundNBT());
	}
	
	@Nullable
	public static EntityBodyUnconscious createBodyFrom(@Nonnull LivingEntity living)
	{
		if(living == null) return null;
		EntityBodyUnconscious body = new EntityBodyUnconscious(VOEntities.BODY, living.getEntityWorld());
		body.copyFrom(living, false);
		body.setSoulUUID(living.getUniqueID());
		
		if(living.getType() == EntityType.PLAYER)
		{
			PlayerEntity player = (PlayerEntity)living;
			PlayerData.forPlayer(player).setBodyUUID(body.getUniqueID());
		}
		
		return body;
	}
	
	public static EntityBodyUnconscious getBodyFromEntity(@Nonnull LivingEntity living)
	{
		World world = living.getEntityWorld();
		for(EntityBodyUnconscious body : world.getEntitiesWithinAABB(EntityBodyUnconscious.class, AbstractBody.ENTIRE_WORLD))
			if(body.getSoulUUID().equals(living.getUniqueID()))
				return body;
		return null;
	}
	
	public boolean stealsGear(){ return false; }
	
	public boolean shouldBindIfPersistent(){ return true; }
	
	public void setBody(@Nullable LivingEntity living)
	{
		CompoundNBT data = new CompoundNBT();
		
		// Store entity equipment in body inventory
		if(living != null)
		{
			setSoulUUID(living.getUniqueID());
			
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
	
	public LivingEntity getBody()
	{
		for(LivingEntity living : getEntityWorld().getLoadedEntitiesWithinAABB(LivingEntity.class, ENTIRE_WORLD))
			if(living.getUniqueID() == this.getSoulUUID())
				return living;
		
		return super.getBody();
	}
	
    public boolean isNoDespawnRequired(){ return true; }
	
	protected void dropInventory(){ }
	
	public void tick()
	{
		super.tick();
		
		if(hasBody())
		{
			if(isPlayer())
			{
				PlayerEntity soul = (PlayerEntity)getSoul();
				if(soul != null && !this.world.isRemote)
				{
					boolean needsUpdate = false;
					for(int slot=0; slot<4; slot++)
					{
						ItemStack equipped = soul.getItemStackFromSlot(EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.ARMOR, slot));
						if(!(ItemStack.areItemsEqual(equipped, lastKnownArmour.get(slot)) && ItemStack.areItemStackTagsEqual(equipped, lastKnownArmour.get(slot))))
						{
							needsUpdate = true;
							lastKnownArmour.set(slot, equipped.copy());
						}
					}
					
					for(int slot=0; slot<2; slot++)
					{
						ItemStack equipped = soul.getItemStackFromSlot(EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.HAND, slot));
						if(!(ItemStack.areItemsEqual(equipped, lastKnownEquip.get(slot)) && ItemStack.areItemStackTagsEqual(equipped, lastKnownEquip.get(slot))))
						{
							needsUpdate = true;
							lastKnownEquip.set(slot, equipped.copy());
						}
					}
					
					if(needsUpdate)
						getDataManager().set(LAST_KNOWN_EQUIPMENT, AbstractBody.writeInventoryToNBT(new CompoundNBT(), new Inventory(lastKnownArmour.toArray(new ItemStack[4])), new Inventory(lastKnownEquip.toArray(new ItemStack[2])), null));
					
					// If the player is online and not unconscious, remove body
					PlayerData data = PlayerData.forPlayer(soul);
					if(data.getBodyCondition() != BodyCondition.UNCONSCIOUS)
						this.onKillCommand();
				}
//				if(soul == null)
//				{
//					if(!isPlayer())
//						onKillCommand();
//				}
//				else
//					moveWithinRangeOf(this, soul, PlayerData.forPlayer((PlayerEntity)soul).getSoulCondition().getWanderRange());
				
				return;
			}
			else
			{
				LivingEntity body = getBody();
				LivingData bodyData = LivingData.forEntity(body);
				if(!body.isAlive() || !bodyData.isUnconscious())
				{
					respawnMob(body);
					return;
				}
				
				body.tick();
				setBody(body);
			}
		}
	}
	
	private void readLastKnownFromNBT(CompoundNBT compound)
	{
		ListNBT armourList = compound.getList("ArmorItems", 10);
		for(int i=0; i<this.lastKnownArmour.size(); i++)
			this.lastKnownArmour.set(i, ItemStack.read(armourList.getCompound(i)));
		
		ListNBT handList = compound.getList("HandItems", 10);
		for(int i=0; i<this.lastKnownEquip.size(); i++)
			this.lastKnownEquip.set(i, ItemStack.read(handList.getCompound(i)));
	}
	
	private ItemStack getSlotFromLastKnown(EquipmentSlotType.Group group, int index)
	{
		readLastKnownFromNBT(getDataManager().get(LAST_KNOWN_EQUIPMENT));
		switch(group)
		{
			case ARMOR:	return this.lastKnownArmour.get(index);
			case HAND:	return this.lastKnownEquip.get(index);
			default:	return ItemStack.EMPTY;
		}
	}
	
	@Nullable
	public LivingEntity getBodyForRender()
	{
		if(isPlayer())
		{
			LivingEntity body = super.getBodyForRender();
			
			for(int slot=0; slot<4; slot++)
				body.setItemStackToSlot(EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.ARMOR, slot), getSlotFromLastKnown(EquipmentSlotType.Group.ARMOR, slot));
			
			for(int slot=0; slot<2; slot++)
				body.setItemStackToSlot(EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.HAND, slot), getSlotFromLastKnown(EquipmentSlotType.Group.HAND, slot));
			
			return body;
		}
		return super.getBodyForRender();
	}
	
	public void respawnMob(LivingEntity body)
	{
		body.setPosition(getPosX(), getPosY(), getPosZ());
		getEntityWorld().addEntity(body);
		remove();
	}
	
	public boolean attackEntityFrom(DamageSource cause, float amount)
	{
		if(cause != DamageSource.OUT_OF_WORLD)
		{
			if(hasBody() && getSoul() != null)
			{
				if(isPlayer())
					getSoul().attackEntityFrom(cause, amount);
				else
				{
					LivingEntity body = getBody();
					body.attackEntityFrom(cause, amount);
					setBody(body);
				}
			}
			return false;
		}
		return super.attackEntityFrom(cause, amount);
	}
	
	public boolean addPotionEffect(EffectInstance effectInstanceIn)
	{
		if(hasBody() && getSoul() != null)
		{
			if(isPlayer())
				getSoul().addPotionEffect(effectInstanceIn);
			else
			{
				LivingEntity body = getBody();
				body.addPotionEffect(effectInstanceIn);
				setBody(body);
			}
		}
		return false;
	}
	
	public void onKillCommand()
	{
		super.onKillCommand();
		if(!isPlayer() && getBody() != null && !getEntityWorld().isRemote)
		{
			LivingEntity body = getBody();
			body.setPosition(getPosX(), getPosY(), getPosZ());
			
			LivingData data = LivingData.forEntity(body);
			data.setBludgeoning(0F);
			
			getEntityWorld().addEntity(body);
			body.onKillCommand();
		}
	}
	
	public void openContainer(PlayerEntity playerIn)
	{
		LivingEntity soul = getSoul();
		if(!isPlayer())
			super.openContainer(playerIn);
		else if(soul != null)
			playerIn.openContainer(new SimpleNamedContainerProvider((window, player, p1) -> new ContainerPlayerBody(window, player, ((PlayerEntity)soul).inventory, this), soul.getDisplayName()));
	}
}
