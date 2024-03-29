package com.lying.variousoddities.species.abilities;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.lying.variousoddities.api.event.AbilityEvent.AbilityAffectEntityEvent;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

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
	
	private Component displayName = null;
	private Component description = null;
	private Nature customNature = null;
	private UUID sourceId = null;
	
	protected Ability() { }
	
	public Ability clone(){ return AbilityRegistry.getAbility(writeAtomically(new CompoundTag())); }
	
	public final ResourceLocation getRegistryName(){ return AbilityRegistry.getAbilityRegistryKey(this).location(); }
	
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
	@Nullable
	public UUID getSourceId(){ return this.sourceId; }
	public Ability setSourceId(@Nullable UUID idIn){ this.sourceId = idIn; return this; }
	public boolean isTemporary(){ return getSourceId() == null; }
	public Ability setTemporary(){ setSourceId(null); return this; }
	
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
	
	public Ability setDisplayName(Component nameIn)
	{
		if(nameIn != null)
			this.displayName = nameIn;
		else
			this.displayName = null;
		return this;
	}
	
	public boolean hasCustomDesc() { return this.description != null; }
	
	public Ability setCustomDesc(Component descIn)
	{
		if(descIn != null)
			this.description = descIn;
		else
			this.description = null;
		return this;
	}
	
	public void setCustomNature(Nature natureIn){ this.customNature = natureIn; }
	
	public Component getDisplayName()
	{
		MutableComponent name = (MutableComponent)(hasCustomName() ? this.displayName : translatedName());
		if(isTemporary())
			name.withStyle((style) -> { return style.applyFormat(ChatFormatting.ITALIC); });
		return name;
	}
	
	public Component translatedName()
	{
		return Component.translatable("ability."+getMapName());
	}
	
	public Component getDescription()
	{
		return hasCustomDesc() ? this.description : description();
	}
	
	public Component description()
	{
		return Component.translatable("ability."+getRegistryName()+".desc");
	}
	
	/** Writes all data needed to reinstantiate this ability to NBT */
	public final CompoundTag writeAtomically(CompoundTag compound)
	{
		compound.putString("Name", getRegistryName().toString());
		if(this.sourceId != null)
			compound.putString("UUID", this.sourceId.toString());
		
		if(hasCustomName())
			compound.putString("CustomName", Component.Serializer.toJson(this.displayName));
		
		if(hasCustomDesc())
			compound.putString("CustomDesc", Component.Serializer.toJson(this.description));
		
		if(this.customNature != null)
			compound.putString("CustomNature", this.customNature.getSerializedName());
		
		CompoundTag tag = writeToNBT(new CompoundTag());
		if(!tag.isEmpty())
			compound.put("Tag", writeToNBT(new CompoundTag()));
		return compound;
	}
	
	/** Writes instance-specific data for this ability to NBT */
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		
	}
	
	public void onAbilityAdded(LivingEntity entity){ }
	
	public void onAbilityRemoved(LivingEntity entity){ }
	
	public boolean canAbilityAffectEntity(Entity target, LivingEntity owner)
	{
		return !MinecraftForge.EVENT_BUS.post(new AbilityAffectEntityEvent(target, this, owner));
	}
	
	public abstract Type getType();
	
	public Nature getNature(){ return this.customNature != null ? this.customNature : getDefaultNature(); }
	
	protected abstract Nature getDefaultNature();
	
	public static abstract class Builder
	{
		public static final ResourceKey<Registry<Builder>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(Reference.ModInfo.MOD_ID, "abilities"));
		
		public Builder() { }
		
		public abstract Ability create(CompoundTag compound);
		
		public final ResourceLocation getRegistryName() { return AbilityRegistry.getBuilderRegistryKey(getClass()).location(); }
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
		
		public Component translated(){ return Component.translatable("enum."+Reference.ModInfo.MOD_ID+".ability_type."+name().toLowerCase()); }
	}
	
	public static enum Nature implements StringRepresentable
	{
		SUPERNATURAL(false),
		EXTRAORDINARY(true),
		SPELL_LIKE(false);
		
		private final boolean useInAntiMagic;
		
		private Nature(boolean useInAntiMagic)
		{
			this.useInAntiMagic = useInAntiMagic;
		}
		
		public String getSerializedName(){ return this.name().toLowerCase(); }
		
		public static Nature fromString(String nameIn)
		{
			for(Nature nature : values())
				if(nature.getSerializedName().equalsIgnoreCase(nameIn))
					return nature;
			return EXTRAORDINARY;
		}
		
		public boolean canBeUsedInAntiMagic(){ return this.useInAntiMagic; }
		
		public Component translated(){ return Component.translatable("enum."+Reference.ModInfo.MOD_ID+".ability_nature."+getSerializedName()); }
	}
}
