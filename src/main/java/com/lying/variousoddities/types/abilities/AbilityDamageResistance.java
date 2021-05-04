package com.lying.variousoddities.types.abilities;

import com.lying.variousoddities.api.event.DamageResistanceEvent;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.types.TypeHandler.DamageResist;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityDamageResistance extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "damage_resistance");
	
	private final DamageType damageType;
	private final DamageResist resistType;
	
	private AbilityDamageResistance(CompoundNBT compound)
	{
		damageType = DamageType.fromString(compound.getString("Damage"));
		resistType = DamageResist.fromString(compound.getString("Type"));
	}
	
	public AbilityDamageResistance(DamageType damageIn, DamageResist typeIn)
	{
		this.damageType = damageIn;
		this.resistType = typeIn;
	}
	
	public ResourceLocation getRegistryName(){ return REGISTRY_NAME; }
	
	public ResourceLocation getMapName(){ return new ResourceLocation(Reference.ModInfo.MOD_ID, "damage_resistance_"+damageType.getString()); }
	
	public ITextComponent translatedName()
	{
		return resistType.getTranslated(damageType);
	}
	
	public Type getType(){ return resistType == DamageResist.VULNERABLE ? Ability.Type.WEAKNESS : Ability.Type.DEFENSE; }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		compound.putString("Damage", this.damageType.getString());
		compound.putString("Type", this.resistType.getString());
		return compound;
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::applyDamageResistance);
	}
	
	public void applyDamageResistance(DamageResistanceEvent event)
	{
		DamageSource source = event.getSource();
		for(Ability ability : AbilityRegistry.getAbilitiesOfType(event.getEntityLiving(), REGISTRY_NAME))
		{
			AbilityDamageResistance resistance = (AbilityDamageResistance)ability;
			if(resistance.damageType.isDamageType(source) && event.getResistance() != DamageResist.IMMUNE)
				if(resistance.resistType == DamageResist.IMMUNE)
					event.setResistance(DamageResist.IMMUNE);
				else
					event.setResistance(event.getResistance().add(resistance.resistType));
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Ability create(CompoundNBT compound)
		{
			return new AbilityDamageResistance(compound);
		}
	}
}
