package com.lying.variousoddities.species.templates;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.species.types.Types;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

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
	
	public ITextComponent translate()
	{
		ITextComponent translation = null;
		String translationBase = "operation."+Reference.ModInfo.MOD_ID+".type.";
		switch(this.action)
		{
			case ADD:
				translation = new TranslationTextComponent(translationBase+"add", typesToString(this.types));
				break;
			case REMOVE:
				translation = new TranslationTextComponent(translationBase+"remove", typesToString(this.types));
				break;
			case REMOVE_ALL:
				translation = new TranslationTextComponent(translationBase+"remove_all."+(removalType ? "supertypes" : "subtypes"));
				break;
			case SET:
				translation = new TranslationTextComponent(translationBase+"set", (new Types(Arrays.asList(this.types)).toHeader()));
				break;
		}
		
		return condition == null ? translation : condition.translate().append(translation);
	}
	
	protected static StringTextComponent typesToString(EnumCreatureType... types)
	{
		StringTextComponent text = new StringTextComponent("[");
		for(int i=0; i<types.length; i++)
		{
			text.append(types[i].getTranslated());
			if(i < types.length - 1)
				text.append(new StringTextComponent(", "));
		}
		text.append(new StringTextComponent("]"));
		return text;
	}
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		if(this.types != null)
		{
			ListNBT typeList = new ListNBT();
			for(EnumCreatureType type : types)
				if(type != null)
					typeList.add(StringNBT.valueOf(type.getString()));
			
			compound.put("Types", typeList);
		}
		else
			compound.putBoolean("Supertypes", this.removalType);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		if(compound.contains("Types", 9))
		{
			ListNBT typeList = compound.getList("Types", 8);
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
		private EnumCreatureType[] types;
		
		public Condition(Style styleIn, EnumCreatureType... typesIn)
		{
			this.style = styleIn;
			this.types = typesIn;
		}
		
		public JsonObject writeToJson(JsonObject json)
		{
			json.addProperty("Style", this.style.getString());
			
			JsonArray typesList = new JsonArray();
			for(EnumCreatureType type : types)
				typesList.add(type.getString());
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
			if(types == null || types.length == 0)
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
			}
			return false;
		}
		
		public IFormattableTextComponent translate()
		{
			return new TranslationTextComponent("operation."+Reference.ModInfo.MOD_ID+".type.condition."+this.style.getString(), typesToString(this.types));
		}
		
		public static enum Style implements IStringSerializable
		{
			AND,
			OR,
			XOR;
			
			public String getString(){ return name().toLowerCase(); }
			
			public static Style fromString(String nameIn)
			{
				for(Style style : values())
					if(style.getString().equalsIgnoreCase(nameIn))
						return style;
				return AND;
			}
		}
	}
}
