package com.lying.variousoddities.species.abilities;

import java.util.EnumSet;
import java.util.List;

import com.google.common.collect.Lists;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityDamageReduction extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "damage_reduction");
	
	private DamageType[] exceptions;
	private int amount;
	
	public AbilityDamageReduction(int amountIn, DamageType... exceptionsIn)
	{
		super(REGISTRY_NAME);
		this.amount = amountIn;
		this.exceptions = exceptionsIn;
	}
	
	public ITextComponent translatedName()
	{
		StringTextComponent exceptionTranslated;
		if(exceptions.length == 0)
			exceptionTranslated = new StringTextComponent("-");
		else
		{
			exceptionTranslated = new StringTextComponent("");
			for(int i=0; i<exceptions.length; i++)
			{
				DamageType type = exceptions[i];
				exceptionTranslated.append(type.getTranslated());
				if(i < exceptions.length - 1)
					exceptionTranslated.append(new StringTextComponent(", "));
			}
		}
		
		return new TranslationTextComponent("ability.varodd.damage_reduction", amount, exceptionTranslated);
	}
	
	public Type getType(){ return Ability.Type.DEFENSE; }
	
	public int getAmount(){ return Math.max(4, amount); }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		compound.putInt("Amount", this.amount);
		if(exceptions.length > 0)
		{
			ListNBT exceptionList = new ListNBT();
			for(DamageType type : exceptions)
				exceptionList.add(StringNBT.valueOf(type.getString()));
			compound.put("Exceptions", exceptionList);
		}
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		this.amount = compound.getInt("Amount");
		if(compound.contains("Exceptions", 9))
		{
			ListNBT exceptionList = compound.getList("Exceptions", 8);
			List<DamageType> list = Lists.newArrayList();
			for(int i=0; i<exceptionList.size(); i++)
				list.add(DamageType.fromString(exceptionList.getString(i)));
			this.exceptions = list.toArray(new DamageType[0]);
		}
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::applyDamageReduction);
	}
	
	public void applyDamageReduction(LivingHurtEvent event)
	{
		DamageSource source = event.getSource();
		for(Ability ability : AbilityRegistry.getAbilitiesOfType(event.getEntityLiving(), REGISTRY_NAME))
		{
			AbilityDamageReduction reduction = (AbilityDamageReduction)ability;
			if(reduction.applysTo(source))
				event.setAmount(Math.max(0F, event.getAmount() - reduction.getAmount()));
		}
		
		if(event.getAmount() == 0F)
			event.setCanceled(true);
	}
	
	public boolean applysTo(DamageSource source)
	{
		if(!source.isUnblockable())
		{
			EnumSet<DamageType> damageTypes = DamageType.getDamageTypes(source);
			for(DamageType type : exceptions)
				if(!damageTypes.contains(type))
					return true;
		}
		return false;
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			int amount = compound.getInt("Amount");
			
			DamageType[] exceptions;
			if(compound.contains("Exceptions", 9))
			{
				ListNBT exceptionList = compound.getList("Exceptions", 8);
				exceptions = new DamageType[exceptionList.size()];
				for(int i=0; i<exceptionList.size(); i++)
					exceptions[i] = DamageType.fromString(exceptionList.getString(i));
			}
			else
				exceptions = new DamageType[0];
			return new AbilityDamageReduction(amount, exceptions);
		}
	}
}
