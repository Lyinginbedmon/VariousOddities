package com.lying.variousoddities.species.templates;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.species.types.Types;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public class TypeOperation extends TemplateOperation
{
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "type");
	
	protected EnumCreatureType[] types = null;
	private boolean removalType = false;
	protected Condition condition = null;
	
	public TypeOperation(Operation actionIn, EnumCreatureType... typesIn)
	{
		super(actionIn);
		this.types = typesIn;
	}
	
	public TypeOperation(Operation actionIn, boolean removeSupertypesIn)
	{
		super(actionIn);
		this.removalType = removeSupertypesIn;
	}
	
	public ResourceLocation getRegistryName(){ return REGISTRY_NAME; }
	
	public TypeOperation setCondition(Condition conditionIn){ this.condition = conditionIn; return this; }
	
	public Component translate()
	{
		Component translation = null;
		String translationBase = "operation."+Reference.ModInfo.MOD_ID+".type.";
		switch(this.action)
		{
			case ADD:
				translation = Component.translatable(translationBase+"add", typesToString(this.types));
				break;
			case REMOVE:
				translation = Component.translatable(translationBase+"remove", typesToString(this.types));
				break;
			case REMOVE_ALL:
				translation = Component.translatable(translationBase+"remove_all."+(removalType ? "supertypes" : "subtypes"));
				break;
			case SET:
				translation = Component.translatable(translationBase+"set", (new Types(Arrays.asList(this.types)).toHeader()));
				break;
		}
		
		return condition == null ? translation : condition.translate().append(translation);
	}
	
	protected static MutableComponent typesToString(EnumCreatureType... types)
	{
		MutableComponent text = Component.literal("[");
		for(int i=0; i<types.length; i++)
		{
			text.append(types[i].getTranslated(true));
			if(i < types.length - 1)
				text.append(Component.literal(", "));
		}
		text.append(Component.literal("]"));
		return text;
	}
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		if(this.types != null)
		{
			ListTag typeList = new ListTag();
			for(EnumCreatureType type : types)
				if(type != null)
					typeList.add(StringTag.valueOf(type.getSerializedName()));
			
			compound.put("Types", typeList);
		}
		else
			compound.putBoolean("Supertypes", this.removalType);
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		if(compound.contains("Types", 9))
		{
			ListTag typeList = compound.getList("Types", 8);
			this.types = new EnumCreatureType[typeList.size()];
			for(int i=0; i<typeList.size(); i++)
			{
				EnumCreatureType type = EnumCreatureType.fromName(typeList.getString(i));
				if(type != null)
					this.types[i] = type;
			}
		}
		else
			this.removalType = compound.getBoolean("Supertypes");
	}
	
	public JsonObject writeToJson(JsonObject json)
	{
		if(this.condition != null)
			json.add("Condition", this.condition.writeToJson(new JsonObject()));
		super.writeToJson(json);
		return json;
	}
	
	public void readFromJson(JsonObject json)
	{
		if(json.has("Condition"))
			this.condition = Condition.readFromJson(json.getAsJsonObject("Condition"));
		super.readFromJson(json);
	}
	
	public boolean conditionsValid(Collection<EnumCreatureType> typeSet)
	{
		return this.condition == null || this.condition.isValid(typeSet);
	}
	
	public void applyToTypes(Collection<EnumCreatureType> typeSet)
	{
		if(!conditionsValid(typeSet))
			return;
		
		switch(this.action)
		{
			case ADD:			// Add all types of this operation
				if(this.types != null && this.types.length > 0)
					for(EnumCreatureType type : this.types)
						if(!typeSet.contains(type))
							typeSet.add(type);
				break;
			case REMOVE:		// Remove all types of this operation
				if(this.types != null && this.types.length > 0)
					for(EnumCreatureType type : this.types)
						if(typeSet.contains(type))
							typeSet.remove(type);
				break;
			case REMOVE_ALL:	// Remove all types of the given grade (super or sub)
				List<EnumCreatureType> removed = Lists.newArrayList();
				typeSet.forEach((type) -> { if(type.isSupertype() == removalType) removed.add(type); });
				typeSet.removeAll(removed);
				break;
			case SET:			// Replace the given set with the types of this operation
				if(this.types != null && this.types.length > 0)
				{
					typeSet.clear();
					for(EnumCreatureType type : this.types)
						if(!typeSet.contains(type))
							typeSet.add(type);
				}
				break;
			default:
				break;
		}
	}
	
	public static class Builder extends TemplateOperation.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public TemplateOperation create()
		{
			return new TypeOperation(Operation.ADD);
		}
	}
	
	public static class Condition
	{
		private Style style;
		private EnumSet<EnumCreatureType> types;
		
		public Condition(Style styleIn, EnumCreatureType... typesIn)
		{
			this.style = styleIn;
			
			this.types = EnumSet.noneOf(EnumCreatureType.class);
			for(EnumCreatureType type : typesIn)
				if(!types.contains(type))
					types.add(type);
		}
		
		public JsonObject writeToJson(JsonObject json)
		{
			json.addProperty("Style", this.style.getSerializedName());
			
			JsonArray typesList = new JsonArray();
			for(EnumCreatureType type : types)
				typesList.add(type.getSerializedName());
			json.add("Types", typesList);
			return json;
		}
		
		public static Condition readFromJson(JsonObject json)
		{
			Style style = Style.fromString(json.get("Style").getAsString());
			
			JsonArray typesList = json.getAsJsonArray("Types");
			EnumCreatureType[] types = new EnumCreatureType[typesList.size()];
			for(int i=0; i<typesList.size(); i++)
				types[i] = EnumCreatureType.fromName(typesList.get(i).getAsString());
			
			return new Condition(style, types);
		}
		
		public boolean isValid(Collection<EnumCreatureType> typesIn)
		{
			if(types == null || types.isEmpty())
				return true;
			
			switch(this.style)
			{
				case AND:
					for(EnumCreatureType type : this.types)
						if(!typesIn.contains(type))
							return false;
					return true;
				case OR:
					for(EnumCreatureType type : this.types)
						if(typesIn.contains(type))
							return true;
					return false;
				case XOR:
					boolean found = false;
					for(EnumCreatureType type : this.types)
						if(typesIn.contains(type))
						{
							if(found)
								return false;
							else
								found = true;
						}
					return found;
				case NOR:
					for(EnumCreatureType type : this.types)
						if(typesIn.contains(type))
							return false;
					return true;
			}
			return false;
		}
		
		public MutableComponent translate()
		{
			return Component.translatable("operation."+Reference.ModInfo.MOD_ID+".type.condition."+this.style.getSerializedName(), typesToString(this.types.toArray(new EnumCreatureType[0])));
		}
		
		public static enum Style implements StringRepresentable
		{
			AND,	// All of
			OR,		// Any of
			XOR,	// Only one of
			NOR;	// None of
			
			public String getSerializedName(){ return name().toLowerCase(); }
			
			public static Style fromString(String nameIn)
			{
				for(Style style : values())
					if(style.getSerializedName().equalsIgnoreCase(nameIn))
						return style;
				return AND;
			}
		}
	}
}
