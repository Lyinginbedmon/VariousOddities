package com.lying.variousoddities.species.abilities;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class Ability
{
	/** Sorts abilities alphabetically by their display name */
	public static final Comparator<Ability> SORT_ABILITY = new Comparator<Ability>()
	{
		public int compare(Ability o1, Ability o2)
		{
			String name1 = o1.getDisplayName().getString();
			String name2 = o2.getDisplayName().getString();
			
			List<String> names = Arrays.asList(name1, name2);
			Collections.sort(names);
			
			int index1 = names.indexOf(name1);
			int index2 = names.indexOf(name2);
			return (index1 > index2 ? 1 : index1 < index2 ? -1 : 0);
		}
	};
	
	private ITextComponent displayName = null;
	private Nature customNature = null;
	private final ResourceLocation registryName;
	private UUID sourceId = null;
	
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
	
	/**
	 * Returns the source UUID for this ability.<br>
	 * The source ID determines if two instances of the same ability originate from separate places.<br>
	 * Used in Abilities when refreshing the cache.
	 */
	public UUID getSourceId(){ return this.sourceId; }
	public Ability setSourceId(UUID idIn){ this.sourceId = idIn; return this; }
	
	/** Compares this ability to the given one and returns an assessment of relative strength */
	public int compare(Ability abilityIn){ return 0; }
	
	/** Returns true if this ability should be displayed in the select-species screen */
	public boolean displayInSpecies(){ return true; }
	
	/** Returns true if this ability is active without needing to be triggered. */
	public final boolean passive(){ return !(this instanceof ActivatedAbility); }
	
	public boolean isActive(){ return true; }
	
	/** Registers any necessary listeners needed for this ability to function */
	public void addListeners(IEventBus bus){ }
	
	public boolean hasCustomName(){ return this.displayName != null; }
	
	public Ability setDisplayName(ITextComponent nameIn)
	{
		if(nameIn != null)
			this.displayName = nameIn;
		else
			this.displayName = null;
		return this;
	}
	
	public void setCustomNature(Nature natureIn){ this.customNature = natureIn; }
	
	public ITextComponent getDisplayName()
	{
		return hasCustomName() ? this.displayName : translatedName();
	}
	
	public ITextComponent translatedName()
	{
		return new TranslationTextComponent("ability."+getMapName());
	}
	
	/** Writes all data needed to reinstantiate this ability to NBT */
	public final CompoundNBT writeAtomically(CompoundNBT compound)
	{
		compound.putString("Name", getRegistryName().toString());
		if(this.sourceId != null)
			compound.putString("UUID", this.sourceId.toString());
		
		if(hasCustomName())
			compound.putString("CustomName", ITextComponent.Serializer.toJson(this.displayName));
		
		if(this.customNature != null)
			compound.putString("CustomNature", this.customNature.getString());
		
		CompoundNBT tag = writeToNBT(new CompoundNBT());
		if(!tag.isEmpty())
			compound.put("Tag", writeToNBT(new CompoundNBT()));
		return compound;
	}
	
	/** Writes instance-specific data for this ability to NBT */
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		
	}
	
	public void onAbilityAdded(LivingEntity entity){ }
	
	public void onAbilityRemoved(LivingEntity entity){ }
	
	public abstract Type getType();
	
	public Nature getNature(){ return this.customNature != null ? this.customNature : getDefaultNature(); }
	
	protected abstract Nature getDefaultNature();
	
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
	
	public static enum Nature implements IStringSerializable
	{
		SUPERNATURAL(false),
		EXTRAORDINARY(true),
		SPELL_LIKE(false);
		
		private final boolean useInAntiMagic;
		
		private Nature(boolean useInAntiMagic)
		{
			this.useInAntiMagic = useInAntiMagic;
		}
		
		public String getString(){ return this.name().toLowerCase(); }
		
		public static Nature fromString(String nameIn)
		{
			for(Nature nature : values())
				if(nature.getString().equalsIgnoreCase(nameIn))
					return nature;
			return EXTRAORDINARY;
		}
		
		public boolean canBeUsedInAntiMagic(){ return this.useInAntiMagic; }
	}
}
