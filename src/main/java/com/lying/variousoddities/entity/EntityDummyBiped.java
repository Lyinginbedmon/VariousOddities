package com.lying.variousoddities.entity;

import com.mojang.authlib.GameProfile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EntityDummyBiped extends Mob
{
	private static final EntityDataAccessor<CompoundTag> PROFILE = SynchedEntityData.defineId(EntityDummyBiped.class, EntityDataSerializers.COMPOUND_TAG);
	
	public EntityDummyBiped(EntityType<? extends EntityDummyBiped> type, Level worldIn)
	{
		super(type, worldIn);
	}
	
	public void defineSynchedData()
	{
		super.defineSynchedData();
		getEntityData().define(PROFILE, new CompoundTag());
	}
	
    public static AttributeSupplier.Builder createAttributes()
	{
		return Player.createAttributes().add(Attributes.FOLLOW_RANGE, 16.0D).add(Attributes.ATTACK_KNOCKBACK);
	}
	
	public void setGameProfile(GameProfile profile){ getEntityData().set(PROFILE, NbtUtils.writeGameProfile(new CompoundTag(), profile)); }
	public boolean hasGameProfile(){ return !getEntityData().get(PROFILE).isEmpty(); }
	public GameProfile getGameProfile(){ return NbtUtils.readGameProfile(getEntityData().get(PROFILE)); }
	
	public Component getDisplayName(){ return hasGameProfile() ? Component.literal(getGameProfile().getName()) : super.getDisplayName(); }
}
