package com.lying.variousoddities.species.templates;

import java.util.Map;

import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.templates.TypeOperation.Condition.Style;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

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
	
	public MutableComponent translate()
	{
		return Component.translatable("precondition."+Reference.ModInfo.MOD_ID+".ability."+style.getSerializedName(), ability == null ? "NULL" : ability.getDisplayName());
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
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		compound.putString("Style", style.getSerializedName());
		if(this.ability != null)
			compound.put("Ability", this.ability.writeAtomically(new CompoundTag()));
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
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
