package com.lying.variousoddities.species;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lying.variousoddities.types.EnumCreatureType;
import com.lying.variousoddities.types.abilities.Ability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class Species extends ForgeRegistryEntry<Species>
{
	private boolean canPlayerSelect = true;
	private final List<Ability> abilities = Lists.newArrayList();
	private final List<EnumCreatureType> types = Lists.newArrayList();
	
	public Species(){ }
	
	public boolean isPlayerSelectable(){ return this.canPlayerSelect; }
	
	public Species setPlayerSelect(boolean bool)
	{
		this.canPlayerSelect = bool;
		return this;
	}
	
	public Species addAbility(@Nonnull Ability abilityIn)
	{
		this.abilities.add(abilityIn);
		return this;
	}
	
	public Species addType(@Nonnull EnumCreatureType typeIn)
	{
		if(!this.types.contains(typeIn))
			this.types.add(typeIn);
		return this;
	}
	
	public Species addType(@Nonnull EnumCreatureType... typeIn)
	{
		for(EnumCreatureType type : typeIn)
			addType(type);
		return this;
	}
	
	public JsonObject toJson()
	{
		JsonObject json = new JsonObject();
		json.addProperty("CanPlayerSelect", this.isPlayerSelectable());
		
		JsonArray types = new JsonArray();
			for(EnumCreatureType type : this.types)
				types.add(type.getString());
		json.add("Types", types);
		
		JsonArray abilities = new JsonArray();
			for(Ability ability : this.abilities)
				abilities.add(ability.writeAtomically(new CompoundNBT()).toString());
		json.add("Abilities", abilities);
		
		return json;
	}
}