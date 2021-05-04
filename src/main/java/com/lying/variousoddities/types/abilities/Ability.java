package com.lying.variousoddities.types.abilities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.IEventBus;

public abstract class Ability
{
	/**
	 * Returns the name of this ability in AbilityRegistry.<br>
	 * This should NEVER change!
	 */
	public abstract ResourceLocation getRegistryName();
	
	/** 
	 * Returns the registry name for this ability in a creature's ability map.<br>
	 * For most abilities this is identical to getRegistryName.<br>
	 * Map name should otherwise reflect some unique property of an ability instance.
	 */
	public ResourceLocation getMapName(){ return getRegistryName(); }
	
	/** Returns true if this ability is active without needing to be triggered. */
	public final boolean passive(){ return !(this instanceof ActivatedAbility); }
	
	/** Registers any necessary listeners needed for this ability to function */
	public void addListeners(IEventBus bus){ }
	
	public ITextComponent translatedName()
	{
		return new TranslationTextComponent("ability."+getMapName());
	}
	
	public final CompoundNBT writeAtomically(CompoundNBT compound)
	{
		compound.putString("Name", getRegistryName().toString());
		compound.put("Tag", writeToNBT(new CompoundNBT()));
		return compound;
	}
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		
	}
	
	public abstract Type getType();
	
	public static abstract class Builder
	{
		public abstract Ability create(CompoundNBT compound);
	}
	
	public static enum Type
	{
		WEAKNESS,
		UTILITY,
		DEFENSE,
		ATTACK;
	}
}
