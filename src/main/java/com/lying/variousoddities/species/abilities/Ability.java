package com.lying.variousoddities.species.abilities;

import javax.annotation.Nonnull;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class Ability
{
	private ITextComponent displayName = null;
	private final ResourceLocation registryName;
	
	protected Ability(@Nonnull ResourceLocation registryNameIn)
	{
		this.registryName = registryNameIn;
	}
	
	public final ResourceLocation getRegistryName(){ return this.registryName; }
	
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
	
	public boolean hasCustomName(){ return this.displayName != null; }
	
	public void setDisplayName(ITextComponent nameIn)
	{
		if(nameIn != null)
			this.displayName = nameIn;
		else
			this.displayName = null;
	}
	
	public ITextComponent getDisplayName()
	{
		return hasCustomName() ? this.displayName : translatedName();
	}
	
	public ITextComponent translatedName()
	{
		return new TranslationTextComponent("ability."+getMapName());
	}
	
	public final CompoundNBT writeAtomically(CompoundNBT compound)
	{
		compound.putString("Name", getRegistryName().toString());
		
		if(hasCustomName())
			compound.putString("CustomName", ITextComponent.Serializer.toJson(this.displayName));
		
		CompoundNBT tag = writeToNBT(new CompoundNBT());
		if(!tag.isEmpty())
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
	
	public static abstract class Builder extends ForgeRegistryEntry<Ability.Builder>
	{
		public Builder(@Nonnull ResourceLocation registryName){ setRegistryName(registryName); }
		
		public abstract Ability create(CompoundNBT compound);
	}
	
	public static enum Type
	{
		WEAKNESS(3),
		UTILITY(2),
		DEFENSE(1),
		ATTACK(0);
		
		public final int texIndex;
		
		private Type(int index)
		{
			this.texIndex = index;
		}
	}
}
