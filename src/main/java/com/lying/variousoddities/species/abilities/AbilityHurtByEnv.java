package com.lying.variousoddities.species.abilities;

import com.google.common.base.Predicate;
import com.lying.variousoddities.init.VODamageSource;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityHurtByEnv extends Ability
{
	private EnvType type;
	
	public AbilityHurtByEnv(EnvType typeIn)
	{
		super();
		this.type = typeIn;
	}
	
	public Type getType(){ return Type.WEAKNESS; }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public EnvType getEnvType(){ return this.type; }
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::onLivingUpdate);
	}
	
	public Component translatedName()
	{
		return Component.translatable("ability.varodd.hurt_by_env", this.type.translated());
	}
	
	public void onLivingUpdate(LivingTickEvent event)
	{
		LivingEntity living = event.getEntity();
		if(!living.level.isClientSide)
			for(AbilityHurtByEnv env : AbilityRegistry.getAbilitiesOfClass(living, this.getClass()))
				if(env.type != EnvType.WATER)
					if(type.shouldDamage(living))
						type.damageEntity(living);
	}
	
	public static enum EnvType implements StringRepresentable
	{
		WATER(LivingEntity::isInWaterRainOrBubble, DamageSource.DROWN),
		HOT(new Predicate<LivingEntity>()
		{
			public boolean apply(LivingEntity input)
			{
				int i = Mth.floor(input.getX());
				int j = Mth.floor(input.getY());
				int k = Mth.floor(input.getZ());
				return input.level.getBiome(new BlockPos(i, 0, k)).get().shouldSnowGolemBurn(new BlockPos(i, j, k));
			}
		}, DamageSource.ON_FIRE),
		COLD(new Predicate<LivingEntity>()
		{
			public boolean apply(LivingEntity input)
			{
				int i = Mth.floor(input.getX());
				int j = Mth.floor(input.getY());
				int k = Mth.floor(input.getZ());
				return input.level.getBiome(new BlockPos(i, 0, k)).get().coldEnoughToSnow(new BlockPos(i, j, k));
			}
		}, VODamageSource.COLD);
		
		private final Predicate<LivingEntity> test;
		private final DamageSource source;
		
		private EnvType(Predicate<LivingEntity> testIn, DamageSource damageIn)
		{
			this.test = testIn;
			this.source = damageIn;
		}
		
		public Component translated(){ return Component.translatable("enum.varodd.env_type."+getSerializedName()); }
		
		public String getSerializedName(){ return name().toLowerCase(); }
		
		public static EnvType fromString(String nameIn)
		{
			for(EnvType type : values())
				if(type.getSerializedName().equalsIgnoreCase(nameIn))
					return type;
			return WATER;
		}
		
		public boolean shouldDamage(LivingEntity living){ return test.apply(living); }
		public void damageEntity(LivingEntity living)
		{
			living.hurt(source, 1.0F);
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			return new AbilityHurtByEnv(EnvType.fromString(compound.getString("Type")));
		}
	}
}
