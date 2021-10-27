package com.lying.variousoddities.species.abilities;

import com.google.common.base.Predicate;
import com.lying.variousoddities.init.VODamageSource;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityHurtByEnv extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "hurt_by_env");
	
	private EnvType type;
	
	public AbilityHurtByEnv(EnvType typeIn)
	{
		super(REGISTRY_NAME);
		this.type = typeIn;
	}
	
	public Type getType(){ return Type.WEAKNESS; }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public EnvType getEnvType(){ return this.type; }
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::onLivingUpdate);
	}
	
	public ITextComponent translatedName()
	{
		return new TranslationTextComponent("ability.varodd.hurt_by_env", this.type.translated());
	}
	
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		LivingEntity living = event.getEntityLiving();
		if(!living.world.isRemote)
			for(AbilityHurtByEnv env : AbilityRegistry.getAbilitiesOfType(living, this.getClass()))
				if(env.type != EnvType.WATER)
					if(type.shouldDamage(living))
						type.damageEntity(living);
	}
	
	public static enum EnvType implements IStringSerializable
	{
		WATER(LivingEntity::isInWaterRainOrBubbleColumn, DamageSource.DROWN),
		HOT(new Predicate<LivingEntity>()
		{
			public boolean apply(LivingEntity input)
			{
				int i = MathHelper.floor(input.getPosX());
				int j = MathHelper.floor(input.getPosY());
				int k = MathHelper.floor(input.getPosZ());
				return input.world.getBiome(new BlockPos(i, 0, k)).getTemperature(new BlockPos(i, j, k)) > 1.0F;
			}
		}, DamageSource.ON_FIRE),
		COLD(new Predicate<LivingEntity>()
		{
			public boolean apply(LivingEntity input)
			{
				int i = MathHelper.floor(input.getPosX());
				int j = MathHelper.floor(input.getPosY());
				int k = MathHelper.floor(input.getPosZ());
				return input.world.getBiome(new BlockPos(i, 0, k)).getTemperature(new BlockPos(i, j, k)) < 0.15F;
			}
		}, VODamageSource.COLD);
		
		private final Predicate<LivingEntity> test;
		private final DamageSource source;
		
		private EnvType(Predicate<LivingEntity> testIn, DamageSource damageIn)
		{
			this.test = testIn;
			this.source = damageIn;
		}
		
		public ITextComponent translated(){ return new TranslationTextComponent("enum.varodd.env_type."+getString()); }
		
		public String getString(){ return name().toLowerCase(); }
		
		public static EnvType fromString(String nameIn)
		{
			for(EnvType type : values())
				if(type.getString().equalsIgnoreCase(nameIn))
					return type;
			return WATER;
		}
		
		public boolean shouldDamage(LivingEntity living){ return test.apply(living); }
		public void damageEntity(LivingEntity living)
		{
			living.attackEntityFrom(source, 1.0F);
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			return new AbilityHurtByEnv(EnvType.fromString(compound.getString("Type")));
		}
	}
}
