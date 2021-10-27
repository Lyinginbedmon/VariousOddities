package com.lying.variousoddities.species.templates;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.Ability.Nature;
import com.lying.variousoddities.species.abilities.AbilityRegistry;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AbilityOperation extends TemplateOperation
{
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "ability");
	
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
	
	public static AbilityOperation add(Ability abilityIn){ return add(false, abilityIn); }
	public static AbilityOperation add(boolean unlessBetter, Ability abilityIn){ return new AbilityOperation(Operation.ADD, unlessBetter, abilityIn); }
	
	public ResourceLocation getRegistryName(){ return REGISTRY_NAME; }
	
	public ITextComponent translate()
	{
		String translationBase = "operation."+Reference.ModInfo.MOD_ID+".ability.";
		switch(this.action)
		{
			case SET:
			case ADD:
				return new TranslationTextComponent(translationBase+"add", ability == null ? "??" : ability.getDisplayName());
			case REMOVE:
				return new TranslationTextComponent(translationBase+"remove", ability == null ? "??" : ability.getDisplayName());
			case REMOVE_ALL:
				return new TranslationTextComponent(translationBase+"remove_all", naturesToString());
			default:
				return super.translate();
		}
	}
	
	private StringTextComponent naturesToString()
	{
		StringTextComponent text = new StringTextComponent("[");
		for(int i=0; i<natures.length; i++)
		{
			text.append(new StringTextComponent(natures[i].getString()));
			if(i < natures.length - 1)
				text.append(new StringTextComponent(", "));
		}
		text.append(new StringTextComponent("]"));
		return text;
	}
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		if(this.ability != null)
		{
			compound.put("Ability", this.ability.writeAtomically(new CompoundNBT()));
			compound.putBoolean("UnlessBetter", this.unlessBetter);
		}
		else if(this.natures != null)
		{
			ListNBT natureList = new ListNBT();
			for(Nature nature : natures)
				natureList.add(StringNBT.valueOf(nature.getString()));
			compound.put("Nature", natureList);
		}
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
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
			ListNBT typeList = compound.getList("Nature", 8);
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
	
	public static class Builder extends TemplateOperation.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public TemplateOperation create()
		{
			return new AbilityOperation(Operation.ADD, Nature.EXTRAORDINARY);
		}
	}
}
