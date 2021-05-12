package com.lying.variousoddities.species;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class Template extends ForgeRegistryEntry<Template>
{
	private final List<Ability> abilities = Lists.newArrayList();
	private final List<EnumCreatureType> types = Lists.newArrayList();
	
	public Template(){ }
	public Template(ResourceLocation name)
	{
		setRegistryName(name);
	}
	
	public Template addAbility(@Nonnull Ability abilityIn)
	{
		this.abilities.add(abilityIn);
		return this;
	}
	
	public Template addType(@Nonnull EnumCreatureType typeIn)
	{
		if(!this.types.contains(typeIn))
			this.types.add(typeIn);
		return this;
	}
	
	public Template addType(@Nonnull EnumCreatureType... typeIn)
	{
		for(EnumCreatureType type : typeIn)
			addType(type);
		return this;
	}
	
	public JsonObject toJson()
	{
		JsonObject json = new JsonObject();
		
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