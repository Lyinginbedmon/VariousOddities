package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class AbilityStatusImmunity extends Ability
{
	protected AbilityStatusImmunity(ResourceLocation registryNameIn)
	{
		super(registryNameIn);
	}
	
	public Type getType(){ return Type.DEFENSE; }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public abstract boolean appliesToStatus(EffectInstance effectIn);
	
	public static class Poison extends AbilityStatusImmunity
	{
		public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "poison_immunity");
		
		public Poison()
		{
			super(REGISTRY_NAME);
		}
		
		public boolean appliesToStatus(EffectInstance effectIn){ return effectIn.getPotion() == Effects.POISON; }
		
		public static class Builder extends Ability.Builder
		{
			public Builder(){ super(REGISTRY_NAME); }
			public Ability create(CompoundNBT compound){ return new Poison(); }
		}
	}
	
	public static class Paralysis extends AbilityStatusImmunity
	{
		public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "paralysis_immunity");
		
		public Paralysis()
		{
			super(REGISTRY_NAME);
		}
		
		public boolean appliesToStatus(EffectInstance effectIn){ return VOPotions.isParalysisEffect(effectIn); }
		
		public static class Builder extends Ability.Builder
		{
			public Builder(){ super(REGISTRY_NAME); }
			public Ability create(CompoundNBT compound){ return new Paralysis(); }
		}
	}
	
	public static class Configurable extends AbilityStatusImmunity
	{
		public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "status_immunity");
		
		private int potionID = 0;
		
		public Configurable(int effectIn)
		{
			super(REGISTRY_NAME);
			this.potionID = effectIn;
		}
		
		public ResourceLocation getMapName()
		{
			Effect potion = Effect.get(potionID);
			if(potion == null)
				return super.getMapName();
			else
				return new ResourceLocation(Reference.ModInfo.MOD_ID, potion.getName().toLowerCase()+"_immunity");
		}
		
		public ITextComponent translatedName()
		{
			Effect potion = Effect.get(potionID);
			if(potion == null)
				return super.translatedName();
			else
				return new TranslationTextComponent("ability.varodd.status_immunity", potion.getDisplayName());
		}
		
		public boolean appliesToStatus(EffectInstance effectIn)
		{
			return effectIn.getPotion() == Effect.get(potionID);
		}
		
		public CompoundNBT writeToNBT(CompoundNBT compound)
		{
			compound.putInt("Id", potionID);
			return compound;
		}
		
		public void readFromNBT(CompoundNBT compound)
		{
			this.potionID = compound.getInt("Id");
		}
		
		public static class Builder extends Ability.Builder
		{
			public Builder(){ super(REGISTRY_NAME); }
			public Ability create(CompoundNBT compound){ return new Configurable(compound.getInt("Id")); }
		}
	}
}
