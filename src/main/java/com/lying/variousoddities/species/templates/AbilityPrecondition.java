package com.lying.variousoddities.species.templates;

import java.util.Map;

import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.templates.TypeOperation.Condition.Style;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AbilityPrecondition extends TemplatePrecondition
{
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "ability");
	
	private Ability ability = null;
	private Style style = Style.AND;
	
	protected AbilityPrecondition(Style styleIn, Ability abilityIn)
	{
		this.style = styleIn;
		this.ability = abilityIn;
	}
	
	public ResourceLocation getRegistryName() { return REGISTRY_NAME; }
	
	public static AbilityPrecondition has(Ability abilityIn){ return new AbilityPrecondition(Style.AND, abilityIn); }
	
	public static AbilityPrecondition hasNo(Ability abilityIn){ return new AbilityPrecondition(Style.NOR, abilityIn); }
	
	public IFormattableTextComponent translate()
	{
		return new TranslationTextComponent("precondition."+Reference.ModInfo.MOD_ID+".ability."+style.getString(), ability == null ? "NULL" : ability.getDisplayName());
	}
	
	protected boolean testAbilities(Map<ResourceLocation, Ability> abilities)
	{
		if(ability != null)
			switch(style)
			{
				case AND:
					return abilities.containsKey(ability.getMapName());
				case NOR:
					return !abilities.containsKey(ability.getMapName());
				default:
					return false;
			}
		else
			return true;
	}
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		compound.putString("Style", style.getString());
		if(this.ability != null)
			compound.put("Ability", this.ability.writeAtomically(new CompoundNBT()));
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		style = Style.fromString(compound.getString("Style"));
		if(compound.contains("Ability", 10))
			try
			{
				this.ability = AbilityRegistry.getAbility(compound.getCompound("Ability"));
			}
			catch(Exception e){ }
	}
	
	public static class Builder extends TemplatePrecondition.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public TemplatePrecondition create()
		{
			return has(null);
		}
	}
}
