package com.lying.variousoddities.entity;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class EntityDummyBiped extends MobEntity
{
	private static final DataParameter<CompoundNBT> PROFILE = EntityDataManager.createKey(EntityDummyBiped.class, DataSerializers.COMPOUND_NBT);
	
	public EntityDummyBiped(EntityType<? extends EntityDummyBiped> type, World worldIn)
	{
		super(type, worldIn);
	}
	
	public void registerData()
	{
		super.registerData();
		getDataManager().register(PROFILE, new CompoundNBT());
	}
	
	public static AttributeModifierMap.MutableAttribute getAttributes()
	{
		return PlayerEntity.func_234570_el_().createMutableAttribute(Attributes.FOLLOW_RANGE, 16.0D).createMutableAttribute(Attributes.ATTACK_KNOCKBACK);
	}
	
	public void setGameProfile(GameProfile profile){ getDataManager().set(PROFILE, NBTUtil.writeGameProfile(new CompoundNBT(), profile)); }
	public boolean hasGameProfile(){ return !getDataManager().get(PROFILE).isEmpty(); }
	public GameProfile getGameProfile(){ return NBTUtil.readGameProfile(getDataManager().get(PROFILE)); }
	
	public ITextComponent getDisplayName(){ return hasGameProfile() ? new StringTextComponent(getGameProfile().getName()) : super.getDisplayName(); }
}
