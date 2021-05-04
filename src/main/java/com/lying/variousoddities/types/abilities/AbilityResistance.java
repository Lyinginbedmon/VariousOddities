package com.lying.variousoddities.types.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityResistance extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "resistance");
	
	private DamageType damage;
	private int amount;
	
	public AbilityResistance(int amountIn, DamageType typeIn)
	{
		this.amount = amountIn;
		this.damage = typeIn;
	}
	
	public ResourceLocation getRegistryName(){ return REGISTRY_NAME; }
	
	public ITextComponent translatedName()
	{
		return new TranslationTextComponent("ability.varodd.damage_resistance", damage.getTranslated(), amount);
	}
	
	public Type getType(){ return Ability.Type.DEFENSE; }
	
	public int getAmount(){ return Math.max(4, amount); }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		compound.putInt("Amount", this.amount);
		compound.putString("Type", damage.getString());
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		this.amount = compound.getInt("Amount");
		this.damage = DamageType.fromString(compound.getString("Type"));
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::applyResistance);
	}
	
	public void applyResistance(LivingHurtEvent event)
	{
		DamageSource source = event.getSource();
		for(Ability ability : AbilityRegistry.getAbilitiesOfType(event.getEntityLiving(), REGISTRY_NAME))
		{
			AbilityResistance reduction = (AbilityResistance)ability;
			if(reduction.applysTo(source))
				event.setAmount(Math.max(0F, event.getAmount() - reduction.getAmount()));
		}
		
		if(event.getAmount() == 0F)
			event.setCanceled(true);
	}
	
	public boolean applysTo(DamageSource source)
	{
		return DamageType.getDamageTypes(source).contains(damage);
	}
	
	public static class Builder extends Ability.Builder
	{
		public Ability create(CompoundNBT compound)
		{
			int amount = compound.getInt("Amount");
			DamageType damage = DamageType.fromString(compound.getString("Type"));
			if(damage == null) damage = DamageType.FIRE;
			return new AbilityResistance(amount, damage);
		}
	}
}
