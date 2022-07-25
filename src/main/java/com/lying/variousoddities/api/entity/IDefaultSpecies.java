package com.lying.variousoddities.api.entity;

import java.util.EnumSet;
import java.util.List;

import com.google.common.collect.Lists;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;

public interface IDefaultSpecies
{
	public default ResourceLocation defaultSpecies(){ return null; }
	
	public default NonNullList<ResourceLocation> defaultTemplates(){ return NonNullList.<ResourceLocation>create(); }
	
	public default EnumSet<EnumCreatureType> defaultCreatureTypes(){ return EnumSet.noneOf(EnumCreatureType.class); }
	
	public default ResourceLocation defaultHomeDimension(){ return null; }
	
	public default List<Ability> defaultAbilities(){ return Lists.newArrayList(); }
}
