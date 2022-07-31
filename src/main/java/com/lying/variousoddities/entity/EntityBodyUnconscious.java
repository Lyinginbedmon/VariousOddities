package com.lying.variousoddities.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.capabilities.PlayerData.BodyCondition;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.inventory.ContainerPlayerBody;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EntityBodyUnconscious extends AbstractBody
{
    protected static final DataParameter<CompoundTag> LAST_KNOWN_EQUIPMENT	= EntityDataManager.<CompoundTag>createKey(EntityBodyUnconscious.class, DataSerializers.COMPOUND_NBT);
	private final NonNullList<ItemStack> lastKnownArmour = NonNullList.<ItemStack>withSize(4, ItemStack.EMPTY);
	private final NonNullList<ItemStack> lastKnownEquip = NonNullList.<ItemStack>withSize(2, ItemStack.EMPTY);
	
	public EntityBodyUnconscious(EntityType<? extends EntityBodyUnconscious> type, Level worldIn)
	{
		super(type, worldIn);
	}
	
	public void registerData()
	{
		super.registerData();
		getDataManager().register(LAST_KNOWN_EQUIPMENT, new CompoundTag());
	}
	
	@Nullable
	public static EntityBodyUnconscious createBodyFrom(@Nonnull LivingEntity living)
	{
		if(living == null) return null;
		EntityBodyUnconscious body = new EntityBodyUnconscious(VOEntities.BODY, living.getLevel());
		body.copyFrom(living, false);
		body.setSoulUUID(living.getUUID());
		
		if(living.getType() == EntityType.PLAYER)
		{
			Player player = (Player)living;
			PlayerData.forPlayer(player).setBodyUUID(body.getUUID());
		}
		
		return body;
	}
	
	public static EntityBodyUnconscious getBodyFromEntity(@Nonnull LivingEntity living)
	{
		Level level = living.getLevel();
		for(EntityBodyUnconscious body : level.getEntitiesOfClass(EntityBodyUnconscious.class, living.getBoundingBox().inflate(256D)))
			if(body.getSoulUUID().equals(living.getUUID()))
				return body;
		return null;
	}
	
	public boolean stealsGear(){ return false; }
	
	public boolean shouldBindIfPersistent(){ return true; }
	
	public void setBody(@Nullable LivingEntity living)
	{
		CompoundTag data = new CompoundTag();
		
		// Store entity equipment in body inventory
		if(living != null)
		{
			setSoulUUID(living.getUUID());
			
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
	
	public LivingEntity getBody()
	{
		for(LivingEntity living : getLevel().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(256D)))
			if(living.getUUID() == this.getSoulUUID())
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
				Player soul = (Player)getSoul();
				if(soul != null && !this.level.isClientSide)
				{
					boolean needsUpdate = false;
					for(int slot=0; slot<4; slot++)
					{
						ItemStack equipped = soul.getItemBySlot(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, slot));
						if(!(ItemStack.areItemsEqual(equipped, lastKnownArmour.get(slot)) && ItemStack.areItemStackTagsEqual(equipped, lastKnownArmour.get(slot))))
						{
							needsUpdate = true;
							lastKnownArmour.set(slot, equipped.copy());
						}
					}
					
					for(int slot=0; slot<2; slot++)
					{
						ItemStack equipped = soul.getItemBySlot(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.HAND, slot));
						if(!(ItemStack.areItemsEqual(equipped, lastKnownEquip.get(slot)) && ItemStack.areItemStackTagsEqual(equipped, lastKnownEquip.get(slot))))
						{
							needsUpdate = true;
							lastKnownEquip.set(slot, equipped.copy());
						}
					}
					
					if(needsUpdate)
						getDataManager().set(LAST_KNOWN_EQUIPMENT, AbstractBody.writeInventoryToNBT(new CompoundTag(), new Inventory(lastKnownArmour.toArray(new ItemStack[4])), new Inventory(lastKnownEquip.toArray(new ItemStack[2])), null));
					
					// If the player is online and not unconscious, remove body
					PlayerData data = PlayerData.forPlayer(soul);
					if(data.getBodyCondition() != BodyCondition.UNCONSCIOUS)
						this.kill();
				}
				
				return;
			}
			else if(!this.level.isClientSide)
			{
				LivingEntity body = getBody();
				LivingData bodyData = LivingData.forEntity(body);
				if(!bodyData.isUnconscious())
				{
					respawnMob(body);
					return;
				}
				
				body.tick();
				setBody(body);
			}
		}
	}
	
	private void readLastKnownFromNBT(CompoundTag compound)
	{
		ListTag armourList = compound.getList("ArmorItems", 10);
		for(int i=0; i<this.lastKnownArmour.size(); i++)
			this.lastKnownArmour.set(i, ItemStack.of(armourList.getCompound(i)));
		
		ListTag handList = compound.getList("HandItems", 10);
		for(int i=0; i<this.lastKnownEquip.size(); i++)
			this.lastKnownEquip.set(i, ItemStack.of(handList.getCompound(i)));
	}
	
	private ItemStack getSlotFromLastKnown(EquipmentSlot.Type group, int index)
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
				body.setItemSlot(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, slot), getSlotFromLastKnown(EquipmentSlot.Type.ARMOR, slot));
			
			for(int slot=0; slot<2; slot++)
				body.setItemSlot(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.HAND, slot), getSlotFromLastKnown(EquipmentSlot.Type.HAND, slot));
			
			return body;
		}
		return super.getBodyForRender();
	}
	
	public void respawnMob(LivingEntity body)
	{
		body.setPos(getX(), getY(), getZ());
		getLevel().addFreshEntity(body);
		setRemoved(Entity.RemovalReason.DISCARDED);
	}
	
	public boolean hurt(DamageSource cause, float amount)
	{
		if(cause != DamageSource.OUT_OF_WORLD)
		{
			if(hasBody() && getSoul() != null)
			{
				if(isPlayer())
					getSoul().hurt(cause, amount);
				else
				{
					LivingEntity body = getBody();
					body.hurt(cause, amount);
					setBody(body);
				}
			}
			return false;
		}
		return super.hurt(cause, amount);
	}
	
	public boolean addPotionEffect(MobEffectInstance effectInstanceIn)
	{
		if(hasBody() && getSoul() != null)
		{
			if(isPlayer())
				getSoul().addEffect(effectInstanceIn);
			else
			{
				LivingEntity body = getBody();
				body.addEffect(effectInstanceIn);
				setBody(body);
			}
		}
		return false;
	}
	
	public void kill()
	{
		super.kill();
		if(!isPlayer() && getBody() != null && !getLevel().isClientSide)
		{
			LivingEntity body = getBody();
			body.setPos(getX(), getY(), getZ());
			
			LivingData data = LivingData.forEntity(body);
			data.setBludgeoning(0F);
			
			getLevel().addFreshEntity(body);
			body.kill();
		}
	}
	
	public void openContainer(Player playerIn)
	{
		LivingEntity soul = getSoul();
		if(!isPlayer())
			super.openContainer(playerIn);
		else if(soul != null)
			playerIn.openContainer(new SimpleNamedContainerProvider((window, player, p1) -> new ContainerPlayerBody(window, player, ((Player)soul).inventory, this), soul.getDisplayName()));
	}
}
