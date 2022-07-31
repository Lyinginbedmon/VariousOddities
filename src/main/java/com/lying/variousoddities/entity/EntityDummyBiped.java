package com.lying.variousoddities.entity;

import com.mojang.authlib.GameProfile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EntityDummyBiped extends Mob
{
	private static final DataParameter<CompoundTag> PROFILE = EntityDataManager.createKey(EntityDummyBiped.class, DataSerializers.COMPOUND_NBT);
	
	public EntityDummyBiped(EntityType<? extends EntityDummyBiped> type, Level worldIn)
	{
		super(type, worldIn);
	}
	
	public void registerData()
	{
		super.registerData();
		getDataManager().register(PROFILE, new CompoundTag());
	}
	
    public static AttributeSupplier.Builder createAttributes()
	{
		return Player.createAttributes().add(Attributes.FOLLOW_RANGE, 16.0D).add(Attributes.ATTACK_KNOCKBACK);
	}
	
	public void setGameProfile(GameProfile profile){ getDataManager().set(PROFILE, NbtUtils.writeGameProfile(new CompoundTag(), profile)); }
	public boolean hasGameProfile(){ return !getDataManager().get(PROFILE).isEmpty(); }
	public GameProfile getGameProfile(){ return NbtUtils.readGameProfile(getDataManager().get(PROFILE)); }
	
	public Component getDisplayName(){ return hasGameProfile() ? Component.literal(getGameProfile().getName()) : super.getDisplayName(); }
}
