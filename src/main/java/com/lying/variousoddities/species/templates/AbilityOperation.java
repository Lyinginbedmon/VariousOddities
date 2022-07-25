package com.lying.variousoddities.species.templates;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.Ability.Nature;
import com.lying.variousoddities.species.abilities.AbilityModifierCon;
import com.lying.variousoddities.species.abilities.AbilityRegistry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class AbilityOperation extends TemplateOperation
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "ability");
	
	private Ability ability = null;
	private boolean unlessBetter = true;
	private Nature[] natures = null;
	
	public AbilityOperation(Operation actionIn, Nature... natureIn)
	{
		super(actionIn);
		this.natures = natureIn;
	}
	
	public AbilityOperation(Operation actionIn, Ability abilityIn)
	{
		this(actionIn, true, abilityIn);
	}
	
	public AbilityOperation(Operation actionIn, boolean unlessBetterIn, Ability abilityIn)
	{
		super(actionIn);
		this.ability = abilityIn;
		this.unlessBetter = unlessBetterIn;
	}
	
	public static AbilityOperation loseCon(){ return (AbilityOperation)new AbilityOperation(Operation.REMOVE, new AbilityModifierCon(1D)).setCustomDisplay(Component.translatable("operation."+Reference.ModInfo.MOD_ID+".ability.remove_constitution")); }
	public static AbilityOperation add(Ability abilityIn){ return add(false, abilityIn); }
	public static AbilityOperation add(boolean unlessBetter, Ability abilityIn){ return new AbilityOperation(Operation.ADD, unlessBetter, abilityIn); }
	
	public ResourceLocation getRegistryName(){ return REGISTRY_NAME; }
	
	public Component translate()
	{
		if(hasCustomDisplay())
			return getCustomDisplay();
		
		String translationBase = "operation."+Reference.ModInfo.MOD_ID+".ability.";
		switch(this.action)
		{
			case SET:
			case ADD:
				return Component.translatable(translationBase+"add", ability == null ? "??" : ability.getDisplayName());
			case REMOVE:
				return Component.translatable(translationBase+"remove", ability == null ? "??" : ability.getDisplayName());
			case REMOVE_ALL:
				return Component.translatable(translationBase+"remove_all", naturesToString());
			default:
				return super.translate();
		}
	}
	
	public boolean canStackWith(TemplateOperation operationB){ return operationB.getRegistryName().equals(getRegistryName()) && this.action == operationB.action(); }
	
	public List<Component> stackAsList(List<TemplateOperation> operations)
	{
		List<Component> list = Lists.newArrayList();
		String stackName = "operation."+Reference.ModInfo.MOD_ID+".ability.";
		switch(this.action)
		{
			case SET:
			case ADD:		stackName += "add"; break;
			case REMOVE:	stackName += "remove"; break;
			case REMOVE_ALL:
			default:
				list.add(translate());
				return list;
		}
		stackName += ".stack";
		list.add(Component.translatable(stackName));
		
		List<Ability> subList = Lists.newArrayList();
		for(TemplateOperation operation : operations)
			if(operation.getRegistryName().equals(getRegistryName()))
				subList.add(((AbilityOperation)operation).ability);
		Collections.sort(subList, Ability.SORT_ABILITY);
		subList.forEach((ability) -> { list.add(Component.literal(" * ").append(ability.getDisplayName())); });
		return list;
	}
	
	private MutableComponent naturesToString()
	{
		MutableComponent text = Component.literal("[");
		for(int i=0; i<natures.length; i++)
		{
			text.append(natures[i].translated());
			if(i < natures.length - 1)
				text.append(Component.literal(", "));
		}
		text.append(Component.literal("]"));
		return text;
	}
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		if(hasCustomDisplay())
			compound.putString("CustomDisplay", Component.Serializer.toJson(this.getCustomDisplay()));
		if(this.ability != null)
		{
			compound.put("Ability", this.ability.writeAtomically(new CompoundTag()));
			compound.putBoolean("UnlessBetter", this.unlessBetter);
		}
		else if(this.natures != null)
		{
			ListTag natureList = new ListTag();
			for(Nature nature : natures)
				natureList.add(StringTag.valueOf(nature.getSerializedName()));
			compound.put("Nature", natureList);
		}
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		if(compound.contains("CustomDisplay"))
		{
			String s = compound.getString("CustomDisplay");
			try
			{
				setCustomDisplay(Component.Serializer.fromJson(s));
			}
			catch (Exception exception)
			{
				VariousOddities.log.warn("Failed to parse species display name {}", s, exception);
			}
		}
		if(compound.contains("Ability", 10))
		{
			try
			{
				this.ability = AbilityRegistry.getAbility(compound.getCompound("Ability"));
			}
			catch(Exception e){ }
			this.unlessBetter = compound.getBoolean("UnlessBetter");
		}
		else if(compound.contains("Nature", 9))
		{
			ListTag typeList = compound.getList("Nature", 8);
			this.natures = new Nature[typeList.size()];
			for(int i=0; i<typeList.size(); i++)
			{
				Nature type = Nature.fromString(typeList.getString(i));
				if(type != null)
					this.natures[i] = type;
			}
		}
	}
	
	public void applyToAbilities(Map<ResourceLocation, Ability> abilityMap)
	{
		ResourceLocation abilityName = ability == null ? null : ability.getMapName();
		switch(this.action)
		{
			case SET:			// Overwrite the existing ability
			case ADD:			// Add the given ability
				if(ability == null) return;
				if(abilityMap.containsKey(abilityName) && unlessBetter && abilityMap.get(abilityName).compare(ability) > 0)
					return;
				abilityMap.put(abilityName, ability.setSourceId(templateID));
				break;
			case REMOVE:		// Remove the ability, if it is present
				if(ability != null && abilityMap.containsKey(abilityName))
					abilityMap.remove(abilityName);
				break;
			case REMOVE_ALL:	// Remove all abilities of the selected nature
				if(this.natures != null)
				{
					List<ResourceLocation> removed = Lists.newArrayList();
					abilityMap.forEach((mapName, ability) -> {
							for(Nature nature : this.natures)
								if(ability.getNature() == nature)
									removed.add(mapName);
						});
					removed.forEach((mapName) -> { abilityMap.remove(mapName); });
				}
				break;
		}
	}
	
	public Ability getAbility() { return this.ability.clone(); }
	
	public static class Builder extends TemplateOperation.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public TemplateOperation create()
		{
			return new AbilityOperation(Operation.ADD, Nature.EXTRAORDINARY);
		}
	}
}
