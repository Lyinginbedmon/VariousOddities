package com.lying.variousoddities.species;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.abilities.AbilitySize;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.species.types.Types;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class Species
{
	public static final Species HUMAN = new Species(new ResourceLocation("human"))
			.setDisplayName(Component.translatable("species."+Reference.ModInfo.MOD_ID+".human"))
			.addType(EnumCreatureType.HUMANOID);
	private static final UUID UUID_SPECIES = UUID.fromString("d5da3b78-e6ca-4d2e-878b-0e7c3c57a668");
	private ResourceLocation registryName;
	
	private int power = 0;
	
	private ResourceLocation origin = null;
	private final List<Ability> abilities = Lists.newArrayList();
	private final List<EnumCreatureType> types = Lists.newArrayList();
	private boolean playerSelectable = true;
	
	private Component customName = null;
	
	private Species(){ }
	public Species(ResourceLocation name)
	{
		this.registryName = name;
		addAbility(AbilitySize.MEDIUM.clone());
	}
	
	public final void setRegistryName(ResourceLocation name){ this.registryName = name; }
	
	public final ResourceLocation getRegistryName(){ return this.registryName; }
	
	public Component getDisplayName()
	{
		if(this.customName != null)
			return this.customName;
		String path = this.registryName.getPath();
		path = (path.substring(0, 1).toUpperCase() + path.substring(1)).replace('_', ' ');
		return Component.literal(path);
	}
	
	public Species setDisplayName(Component nameIn)
	{
		this.customName = nameIn;
		return this;
	}
	
	/**
	 * A rating of how powerful this species is in comparison to others.<br>
	 * Such as high health & powerful abilities vs low health and utility abilities.<br>
	 * Purely cosmetic, meant as a way of comparing player-selectable species.
	 */
	public int getPower(){ return this.power; }
	
	public Species setPower(@Nonnull int par1Int)
	{
		this.power = Mth.clamp(par1Int, 0, 10);
		return this;
	}
	
	public boolean isPlayerSelectable() { return this.playerSelectable; }
	
	public Species notPlayerSelectable() { this.playerSelectable = false; return this; }
	
	public Species setOriginDimension(@Nullable ResourceLocation dimension)
	{
		this.origin = dimension;
		return this;
	}
	
	public Species addAbility(@Nullable Ability abilityIn)
	{
		if(abilityIn != null)
			this.abilities.add(abilityIn.setSourceId(UUID_SPECIES));
		return this;
	}
	
	/** Returns a list of all abilities in this species, including from creature types */
	public List<Ability> getFullAbilities()
	{
		Map<ResourceLocation, Ability> abilityMap = new HashMap<>();
		
		this.types.forEach((type) -> { if(type.isSupertype()) type.getHandler().addAbilitiesToMap(abilityMap); });
		this.types.forEach((type) -> { if(!type.isSupertype()) type.getHandler().addAbilitiesToMap(abilityMap); });
		
		this.abilities.forEach((ability) -> { abilityMap.put(ability.getMapName(), ability); });
		
		List<Ability> abilities = Lists.newArrayList();
		abilities.addAll(abilityMap.values());
		return abilities;
	}
	
	public List<Ability> getAbilities(){ return this.abilities; }
	
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
	
	public boolean hasTypes(){ return !this.types.isEmpty(); }
	
	public List<EnumCreatureType> getCreatureTypes(){ return this.types; }
	
	public Types getTypes(){ return new Types(this.types); }
	
	public CompoundTag storeInNBT(CompoundTag nbt)
	{
		nbt.putString("Name", this.getRegistryName().toString());
		nbt.putInt("Power", getPower());
		nbt.putBoolean("PlayerSelectable", isPlayerSelectable());
		
		if(customName != null)
			nbt.putString("CustomName", Component.Serializer.toJson(this.customName));
		
		if(origin != null)
			nbt.putString("Dimension", origin.toString());
		
		ListTag types = new ListTag();
			for(EnumCreatureType type : this.types)
				types.add(StringTag.valueOf(type.getSerializedName()));
			nbt.put("Types", types);
		
		ListTag abilities = new ListTag();
			for(Ability ability : this.abilities)
			{
				CompoundTag abilityData = ability.writeAtomically(new CompoundTag());
				if(abilityData.contains("UUID"))
					abilityData.remove("UUID");
				abilities.add(abilityData);
			}
		nbt.put("Abilities", abilities);
		return nbt;
	}
	
	public static Species createFromNBT(CompoundTag nbt)
	{
		ResourceLocation registryName = new ResourceLocation(nbt.getString("Name"));
		Species species = new Species(registryName);
		
		species.setPower(nbt.getInt("Power"));
		species.playerSelectable = nbt.getBoolean("PlayerSelectable");
		
		if(nbt.contains("CustomName", 8))
		{
			String s = nbt.getString("CustomName");
			try
			{
				species.setDisplayName(Component.Serializer.fromJson(s));
			}
			catch (Exception exception)
			{
				VariousOddities.log.warn("Failed to parse species display name {}", s, exception);
			}
		}
		
		if(nbt.contains("Dimension", 8))
			species.setOriginDimension(new ResourceLocation(nbt.getString("Dimension")));
		
		if(nbt.contains("Types", 9))
		{
			ListTag typeList = nbt.getList("Types", 8);
			for(int i=0; i<typeList.size(); i++)
			{
				EnumCreatureType type = EnumCreatureType.fromName(typeList.getString(i));
				if(type != null)
					species.addType(type);
			}
		}
		
		if(nbt.contains("Abilities", 9))
		{
			ListTag abilityList = nbt.getList("Abilities", 10);
			for(int i=0; i<abilityList.size(); i++)
			{
				try
				{
					CompoundTag data = abilityList.getCompound(i);
					Ability ability = AbilityRegistry.getAbility(data);
					if(ability != null)
						species.addAbility(ability);
				}
				catch(Exception e){ }
			}
		}
		
		return species;
	}
	
	public JsonObject toJson()
	{
		JsonObject json = new JsonObject();
		
		json.addProperty("Power", getPower());
		json.addProperty("PlayerSelectable", isPlayerSelectable());
		
		if(this.customName != null)
			json.addProperty("CustomName", Component.Serializer.toJson(this.customName));
		
		if(origin != null)
			json.addProperty("Dimension", origin.toString());
		
		JsonArray types = new JsonArray();
			for(EnumCreatureType type : this.types)
				types.add(type.getSerializedName());
		json.add("Types", types);
		
		JsonArray abilities = new JsonArray();
			for(Ability ability : this.abilities)
			{
				CompoundTag abilityData = ability.writeAtomically(new CompoundTag());
				if(abilityData.contains("UUID"))
					abilityData.remove("UUID");
				abilities.add(abilityData.toString());
			}
		json.add("Abilities", abilities);
		
		return json;
	}
	
	public static Species fromJson(@Nullable JsonElement json)
	{
		if(json == null)
			return null;
		JsonObject object = json.getAsJsonObject();
		
		Species species = new Species();
		
		if(object.has("Power"))
			species.setPower(object.get("Power").getAsInt());
		
		if(object.has("PlayerSelectable"))
			species.playerSelectable = object.get("PlayerSelectable").getAsBoolean();
		
		if(object.has("CustomName"))
		{
			String s = object.get("CustomName").getAsString();
			try
			{
				species.setDisplayName(Component.Serializer.fromJson(s));
			}
			catch (Exception exception)
			{
				VariousOddities.log.warn("Failed to parse species display name {}", s, exception);
			}
		}
		
		if(object.has("Dimension"))
			species.setOriginDimension(new ResourceLocation(object.get("Dimension").getAsString()));
		
		if(object.has("Types"))
		{
			JsonArray typeList = object.get("Types").getAsJsonArray();
			for(int i=0; i<typeList.size(); i++)
			{
				EnumCreatureType type = EnumCreatureType.fromName(typeList.get(i).getAsString());
				if(type != null)
					species.addType(type);
			}
		}
		
		if(object.has("Abilities"))
		{
			JsonArray abilityList = object.get("Abilities").getAsJsonArray();
			for(int i=0; i<abilityList.size(); i++)
			{
				try
				{
					CompoundTag data = TagParser.parseTag(abilityList.get(i).getAsString());
					Ability ability = AbilityRegistry.getAbility(data);
					if(ability != null)
						species.addAbility(ability);
				}
				catch(Exception e){ }
			}
		}
		
		return species;
	}
	
	public SpeciesInstance createInstance()
	{
		return new SpeciesInstance(getRegistryName()).addOriginDimension(this.origin).addTypes(this.types).addAbilities(this.abilities);
	}
	
	public class SpeciesInstance
	{
		private final ResourceLocation registryName;
		private ResourceLocation originDimension = null;
		private final List<Ability> abilities = Lists.newArrayList();
		private final List<EnumCreatureType> types = Lists.newArrayList();
		
		public SpeciesInstance(@Nonnull ResourceLocation registryNameIn)
		{
			this.registryName = registryNameIn;
		}
		
		public ResourceLocation getRegistryName(){ return this.registryName; }
		public Component getDisplayName(){ return SpeciesRegistry.getSpecies(getRegistryName()).getDisplayName(); }
		
		private SpeciesInstance addOriginDimension(@Nullable ResourceLocation dimension){ this.originDimension = dimension; return this; }
		private SpeciesInstance addTypes(Collection<EnumCreatureType> typesIn){ this.types.addAll(typesIn); return this; }
		private SpeciesInstance addAbilities(Collection<Ability> abilitiesIn){ this.abilities.addAll(abilitiesIn); return this; }
		
		public List<EnumCreatureType> getTypes(){ return this.types; }
		public Map<ResourceLocation, Ability> addToMap(Map<ResourceLocation, Ability> mapIn)
		{
			this.abilities.forEach((ability) -> { mapIn.put(ability.getMapName(), ability); });
			return mapIn;
		}
		
		public CompoundTag writeToNBT(CompoundTag compound)
		{
			compound.putString("Name", getRegistryName().toString());
			
			if(originDimension != null)
				compound.putString("Dimension", this.originDimension.toString());
			
			ListTag types = new ListTag();
				for(EnumCreatureType type : this.types)
					types.add(StringTag.valueOf(type.getSerializedName()));
			compound.put("Types", types);
			
			ListTag abilities = new ListTag();
				for(Ability ability : this.abilities)
					abilities.add(ability.writeAtomically(new CompoundTag()));
			compound.put("Abilities", abilities);
			return compound;
		}
		
		public void readFromNBT(CompoundTag compound)
		{
			if(compound.contains("Dimension", 8))
				this.originDimension = new ResourceLocation(compound.getString("Dimension"));
			
			this.types.clear();
			ListTag typeList = compound.getList("Types", 8);
			for(int i=0; i<typeList.size(); i++)
				this.types.add(EnumCreatureType.fromName(typeList.getString(i)));
			
			this.abilities.clear();
			ListTag abilityList = compound.getList("Abilities", 10);
			for(int i=0; i<abilityList.size(); i++)
				this.abilities.add(AbilityRegistry.getAbility(abilityList.getCompound(i)).setSourceId(UUID_SPECIES));
		}
	}
}