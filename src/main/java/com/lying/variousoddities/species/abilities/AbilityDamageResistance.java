package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.api.event.DamageResistanceEvent;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.types.TypeHandler.DamageResist;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityDamageResistance extends Ability
{
	private final DamageType damageType;
	private final DamageResist resistType;
	
	public AbilityDamageResistance(DamageType damageIn, DamageResist typeIn)
	{
		super();
		this.damageType = damageIn;
		this.resistType = typeIn;
	}
	
	public ResourceLocation getMapName(){ return new ResourceLocation(Reference.ModInfo.MOD_ID, "damage_resistance_"+damageType.getSerializedName()); }
	
	public int compare(Ability abilityIn)
	{
		AbilityDamageResistance resistance = (AbilityDamageResistance)abilityIn;
		if(resistance.damageType == damageType)
			return resistance.resistType.val() < resistType.val() ? -1 : resistance.resistType.val() > resistType.val() ? 1 : 0;
		return 0;
	}
	
	public Component translatedName()
	{
		return resistType.getTranslated(damageType);
	}
	
	public Component description()
	{
		return Component.translatable("ability.varodd:damage_resistance."+resistType.getSerializedName(), damageType.getTranslated());
	}
	
	public Type getType(){ return resistType == DamageResist.VULNERABLE ? Ability.Type.WEAKNESS : Ability.Type.DEFENSE; }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		compound.putString("Damage", this.damageType.getSerializedName());
		compound.putString("Type", this.resistType.getSerializedName());
		return compound;
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::applyDamageResistance);
	}
	
	public void applyDamageResistance(DamageResistanceEvent event)
	{
		DamageSource source = event.getSource();
		for(Ability ability : AbilityRegistry.getAbilitiesOfType(event.getEntity(), getRegistryName()))
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
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			DamageType damageType = DamageType.fromString(compound.getString("Damage"));
			DamageResist resistType = DamageResist.fromString(compound.getString("Type"));
			return new AbilityDamageResistance(damageType, resistType);
		}
	}
}
