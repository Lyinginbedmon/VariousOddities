package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.init.VOMobEffects;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public abstract class AbilityStatusImmunity extends Ability
{
	protected AbilityStatusImmunity()
	{
		super();
	}
	
	public Type getType(){ return Type.DEFENSE; }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public abstract boolean appliesToStatus(MobEffectInstance effectIn);
	
	public static class Poison extends AbilityStatusImmunity
	{
		public Poison()
		{
			super();
		}
		
		public boolean appliesToStatus(MobEffectInstance effectIn){ return effectIn.getEffect() == MobEffects.POISON; }
		
		public static class Builder extends Ability.Builder
		{
			public Builder(){ super(); }
			public Ability create(CompoundTag compound){ return new Poison(); }
		}
	}
	
	public static class Paralysis extends AbilityStatusImmunity
	{
		public Paralysis()
		{
			super();
		}
		
		public boolean appliesToStatus(MobEffectInstance effectIn){ return VOMobEffects.isParalysisEffect(effectIn); }
		
		public static class Builder extends Ability.Builder
		{
			public Builder(){ super(); }
			public Ability create(CompoundTag compound){ return new Paralysis(); }
		}
	}
	
	public static class Configurable extends AbilityStatusImmunity
	{
		private int potionID = 0;
		
		public Configurable(int effectIn)
		{
			super();
			this.potionID = effectIn;
		}
		
		public ResourceLocation getMapName()
		{
			MobEffect potion = MobEffect.byId(potionID);
			if(potion == null)
				return super.getMapName();
			else
				return new ResourceLocation(Reference.ModInfo.MOD_ID, potion.getDisplayName().getString().toLowerCase()+"_immunity");
		}
		
		public Component translatedName()
		{
			MobEffect potion = MobEffect.byId(potionID);
			if(potion == null)
				return super.translatedName();
			else
				return Component.translatable("ability.varodd.status_immunity", potion.getDisplayName());
		}
		
		public boolean appliesToStatus(MobEffectInstance effectIn)
		{
			return effectIn.getEffect() == MobEffect.byId(potionID);
		}
		
		public CompoundTag writeToNBT(CompoundTag compound)
		{
			compound.putInt("Id", potionID);
			return compound;
		}
		
		public void readFromNBT(CompoundTag compound)
		{
			this.potionID = compound.getInt("Id");
		}
		
		public static class Builder extends Ability.Builder
		{
			public Builder(){ super(); }
			public Ability create(CompoundTag compound){ return new Configurable(compound.getInt("Id")); }
		}
	}
}
