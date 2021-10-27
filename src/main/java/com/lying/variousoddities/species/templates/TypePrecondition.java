package com.lying.variousoddities.species.templates;

import java.util.EnumSet;

import com.google.gson.JsonParseException;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.templates.TypeOperation.Condition.Style;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TypePrecondition extends TemplatePrecondition
{
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "type");
	
	private Style operation;
	private EnumSet<EnumCreatureType> types;
	
	private IFormattableTextComponent customText = null;
	
	public TypePrecondition(EnumCreatureType... types)
	{
		this(Style.OR, types);
	}
	
	public TypePrecondition(Style operation, EnumCreatureType... typesIn)
	{
		this.operation = operation;
		
		this.types = EnumSet.noneOf(EnumCreatureType.class);
		for(EnumCreatureType type : typesIn)
			if(!types.contains(type))
				types.add(type);
	}
	
	public ResourceLocation getRegistryName() { return REGISTRY_NAME; }
	
	public static TypePrecondition isAnyOf(EnumCreatureType... types){ return new TypePrecondition(Style.OR, types); }
	
	public static TypePrecondition isNoneOf(EnumCreatureType... types){ return new TypePrecondition(Style.NOR, types); }
	
	public static TypePrecondition isLiving(){ return new TypePrecondition(Style.NOR, EnumCreatureType.UNDEAD).setCustomDisplay(new TranslationTextComponent("precondition."+Reference.ModInfo.MOD_ID+".type.living")); }
	
	public static TypePrecondition isCorporeal(){ return new TypePrecondition(Style.NOR, EnumCreatureType.INCORPOREAL).setCustomDisplay(new TranslationTextComponent("precondition."+Reference.ModInfo.MOD_ID+".type.corporeal")); }
	
	protected TypePrecondition setCustomDisplay(IFormattableTextComponent textComponent){ this.customText = textComponent; return this; }
	
	public IFormattableTextComponent translate()
	{
		return this.customText != null ? this.customText : new TranslationTextComponent("precondition."+Reference.ModInfo.MOD_ID+".type."+this.operation.getString(), typesToString(this.types.toArray(new EnumCreatureType[0])));
	}
	
	protected static StringTextComponent typesToString(EnumCreatureType... types)
	{
		StringTextComponent text = new StringTextComponent("[");
		for(int i=0; i<types.length; i++)
		{
			text.append(types[i].getTranslated(true));
			if(i < types.length - 1)
				text.append(new StringTextComponent(", "));
		}
		text.append(new StringTextComponent("]"));
		return text;
	}
	
	protected boolean testTypes(EnumSet<EnumCreatureType> typesIn)
	{
		if(types == null || types.isEmpty())
			return true;
		
		switch(this.operation)
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
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		compound.putString("Style", operation.getString());
		
		if(this.customText != null)
			compound.putString("CustomName", ITextComponent.Serializer.toJson(this.customText));
		
		ListNBT typeList = new ListNBT();
		this.types.forEach((type) -> { typeList.add(StringNBT.valueOf(type.getString())); });
		compound.put("Types", typeList);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		this.operation = Style.fromString(compound.getString("Style"));
		if(compound.contains("CustomName", 8))
		{
			try
			{
				ITextComponent text = ITextComponent.Serializer.getComponentFromJson(compound.getString("CustomName"));
				if(text != null && text instanceof IFormattableTextComponent)
					this.customText = (IFormattableTextComponent)text;
			}
			catch(JsonParseException e){ }
		}
		else
			this.customText = null;
		
		this.types.clear();
		ListNBT typeList = compound.getList("Types", 8);
		for(int i=0; i<typeList.size(); i++)
		{
			EnumCreatureType type = EnumCreatureType.fromName(typeList.getString(i));
			if(type != null && !types.contains(type))
				types.add(type);
		}
	}
	
	public static class Builder extends TemplatePrecondition.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public TemplatePrecondition create()
		{
			return isLiving();
		}
	}
}
