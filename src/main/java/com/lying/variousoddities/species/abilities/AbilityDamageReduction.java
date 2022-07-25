package com.lying.variousoddities.species.abilities;

import java.util.EnumSet;
import java.util.List;

import com.google.common.collect.Lists;
import com.lying.variousoddities.api.event.DamageTypesEvent;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityDamageReduction extends AbilityMeleeDamage
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
	
	public Component translatedName()
	{
		MutableComponent exceptionTranslated;
		if(exceptions.length == 0)
			exceptionTranslated = Component.literal("-");
		else
		{
			exceptionTranslated = Component.literal("");
			for(int i=0; i<exceptions.length; i++)
			{
				DamageType type = exceptions[i];
				exceptionTranslated.append(type.getTranslated());
				if(i < exceptions.length - 1)
					exceptionTranslated.append(Component.literal(", "));
			}
		}
		
		return Component.translatable("ability.varodd.damage_reduction", amount, exceptions());
	}
	
	public Component description()
	{
		if(exceptions.length == 0)
			return Component.translatable("ability.varodd:damage_reduction.desc", amount);
		else
			return Component.translatable("ability.varodd:damage_reduction.desc.exceptions", exceptions(), amount);
	}
	
	private Component exceptions()
	{
		MutableComponent exceptionTranslated;
		if(exceptions.length == 0)
			exceptionTranslated = Component.literal("-");
		else
		{
			exceptionTranslated = Component.literal("");
			for(int i=0; i<exceptions.length; i++)
			{
				DamageType type = exceptions[i];
				exceptionTranslated.append(type.getTranslated());
				if(i < exceptions.length - 1)
					exceptionTranslated.append(Component.literal(", "));
			}
		}
		return exceptionTranslated;
	}
	
	public int compare(Ability abilityIn)
	{
		AbilityDamageReduction reduction = (AbilityDamageReduction)abilityIn;
		if(reduction.getAmount() != getAmount())
			return reduction.getAmount() < getAmount() ? 1 : -1;
		
		return reduction.exceptions.length < exceptions.length ? -1 : reduction.exceptions.length > exceptions.length ? 1 : 0;
	}
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Type getType(){ return Ability.Type.DEFENSE; }
	
	public int getAmount(){ return Math.max(4, amount); }
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		compound.putInt("Amount", this.amount);
		if(exceptions.length > 0)
		{
			ListTag exceptionList = new ListTag();
			for(DamageType type : exceptions)
				exceptionList.add(StringTag.valueOf(type.getSerializedName()));
			compound.put("Exceptions", exceptionList);
		}
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		this.amount = compound.getInt("Amount");
		if(compound.contains("Exceptions", 9))
		{
			ListTag exceptionList = compound.getList("Exceptions", 8);
			List<DamageType> list = Lists.newArrayList();
			for(int i=0; i<exceptionList.size(); i++)
				list.add(DamageType.fromString(exceptionList.getString(i)));
			this.exceptions = list.toArray(new DamageType[0]);
		}
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::applyDamageReduction);
		bus.addListener(this::addDamageTypes);
	}
	
	public void applyDamageReduction(LivingHurtEvent event)
	{
		DamageSource source = event.getSource();
		for(Ability ability : AbilityRegistry.getAbilitiesOfType(event.getEntity(), REGISTRY_NAME))
		{
			AbilityDamageReduction reduction = (AbilityDamageReduction)ability;
			if(reduction.applysTo(source))
				event.setAmount(Math.max(0F, event.getAmount() - reduction.getAmount()));
		}
		
		if(event.getAmount() == 0F)
			event.setCanceled(true);
	}
	
	
	public void addDamageTypes(DamageTypesEvent event)
	{
		DamageSource source = event.getSource();
		if(isValidDamageSource(event.getSource()))
		{
			LivingEntity attacker = (LivingEntity)source.getDirectEntity();
			if(attacker != null && AbilityRegistry.hasAbility(attacker, getMapName()))
			{
				AbilityDamageReduction reduction = (AbilityDamageReduction)AbilityRegistry.getAbilityByName(attacker, REGISTRY_NAME);
				if(reduction != null && reduction.exceptions.length > 0)
					for(DamageType type : reduction.exceptions)
						event.addType(type);
			}
		}
	}
	
	public boolean applysTo(DamageSource source)
	{
		if(source.isBypassArmor())
			return false;
		else
		{
			// Check if source has every damage type exception
			EnumSet<DamageType> damageTypes = DamageType.getDamageTypes(source);
			for(DamageType type : exceptions)
				if(!damageTypes.contains(type))
					return true;
			
			return false;
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundTag compound)
		{
			int amount = compound.getInt("Amount");
			
			DamageType[] exceptions;
			if(compound.contains("Exceptions", 9))
			{
				ListTag exceptionList = compound.getList("Exceptions", 8);
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
