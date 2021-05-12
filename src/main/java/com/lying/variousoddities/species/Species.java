package com.lying.variousoddities.species;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class Species extends ForgeRegistryEntry<Species>
{
	private boolean canPlayerSelect = true;
	private int power = 0;
	private final List<Ability> abilities = Lists.newArrayList();
	private final List<EnumCreatureType> types = Lists.newArrayList();
	
	public Species(ResourceLocation name)
	{
		setRegistryName(name);
	}
	
	public boolean isPlayerSelectable(){ return this.canPlayerSelect; }
	
	public Species setPlayerSelect(boolean bool)
	{
		this.canPlayerSelect = bool;
		return this;
	}
	
	public int getPower(){ return this.power; }
	
	public Species setPower(@Nonnull int par1Int)
	{
		this.power = MathHelper.clamp(par1Int, 0, 10);
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
		json.addProperty("Power", getPower());
		
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
	
	public SpeciesInstance create()
	{
		return new SpeciesInstance(getRegistryName()).addTypes(this.types).addAbilities(this.abilities);
	}
	
	public class SpeciesInstance
	{
		private final ResourceLocation registryName;
		private final List<Ability> abilities = Lists.newArrayList();
		private final List<EnumCreatureType> types = Lists.newArrayList();
		
		public SpeciesInstance(@Nonnull ResourceLocation registryNameIn)
		{
			this.registryName = registryNameIn;
		}
		
		public ResourceLocation getRegistryName(){ return this.registryName; }
		
		private SpeciesInstance addTypes(Collection<EnumCreatureType> typesIn){ this.types.addAll(typesIn); return this; }
		private SpeciesInstance addAbilities(Collection<Ability> abilitiesIn){ this.abilities.addAll(abilitiesIn); return this; }
		
		public List<EnumCreatureType> getTypes(){ return this.types; }
		public Map<ResourceLocation, Ability> addToMap(Map<ResourceLocation, Ability> mapIn)
		{
			this.abilities.forEach((ability) -> { mapIn.put(ability.getMapName(), ability); });
			return mapIn;
		}
		
		public CompoundNBT writeToNBT(CompoundNBT compound)
		{
			compound.putString("Name", getRegistryName().toString());
			
			ListNBT types = new ListNBT();
				for(EnumCreatureType type : this.types)
					types.add(StringNBT.valueOf(type.getString()));
			compound.put("Types", types);
			
			ListNBT abilities = new ListNBT();
				for(Ability ability : this.abilities)
					abilities.add(ability.writeAtomically(new CompoundNBT()));
			compound.put("Abilities", abilities);
			return compound;
		}
		
		public void readFromNBT(CompoundNBT compound)
		{
			this.types.clear();
			ListNBT typeList = compound.getList("Types", 8);
			for(int i=0; i<typeList.size(); i++)
				this.types.add(EnumCreatureType.fromName(typeList.getString(i)));
			
			this.abilities.clear();
			ListNBT abilityList = compound.getList("Abilities", 10);
			for(int i=0; i<abilityList.size(); i++)
				this.abilities.add(AbilityRegistry.getAbility(abilityList.getCompound(i)));
		}
	}
}