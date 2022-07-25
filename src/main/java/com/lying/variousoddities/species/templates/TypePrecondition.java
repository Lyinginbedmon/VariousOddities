package com.lying.variousoddities.species.templates;

import java.util.EnumSet;

import com.google.gson.JsonParseException;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.templates.TypeOperation.Condition.Style;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class TypePrecondition extends TemplatePrecondition
{
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "type");
	
	private Style operation;
	private EnumSet<EnumCreatureType> types;
	
	private MutableComponent customText = null;
	
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
	
	public static TypePrecondition isLiving(){ return new TypePrecondition(Style.NOR, EnumCreatureType.UNDEAD).setCustomDisplay(Component.translatable("precondition."+Reference.ModInfo.MOD_ID+".type.living")); }
	
	public static TypePrecondition isHumanShaped(){ return new TypePrecondition(Style.OR, EnumCreatureType.HUMANOID, EnumCreatureType.MONSTROUS_HUMANOID).setCustomDisplay(Component.translatable("precondition."+Reference.ModInfo.MOD_ID+".type.humanoid")); }
	
	public static TypePrecondition isCorporeal(){ return new TypePrecondition(Style.NOR, EnumCreatureType.INCORPOREAL).setCustomDisplay(Component.translatable("precondition."+Reference.ModInfo.MOD_ID+".type.corporeal")); }
	
	protected TypePrecondition setCustomDisplay(MutableComponent textComponent){ this.customText = textComponent; return this; }
	
	public MutableComponent translate()
	{
		return this.customText != null ? this.customText : Component.translatable("precondition."+Reference.ModInfo.MOD_ID+".type."+this.operation.getSerializedName(), typesToString(this.types.toArray(new EnumCreatureType[0])));
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
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		compound.putString("Style", operation.getSerializedName());
		
		if(this.customText != null)
			compound.putString("CustomName", Component.Serializer.toJson(this.customText));
		
		ListTag typeList = new ListTag();
		this.types.forEach((type) -> { typeList.add(StringTag.valueOf(type.getSerializedName())); });
		compound.put("Types", typeList);
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		this.operation = Style.fromString(compound.getString("Style"));
		if(compound.contains("CustomName", 8))
		{
			try
			{
				Component text = Component.Serializer.fromJson(compound.getString("CustomName"));
				if(text != null && text instanceof MutableComponent)
					this.customText = (MutableComponent)text;
			}
			catch(JsonParseException e){ }
		}
		else
			this.customText = null;
		
		this.types.clear();
		ListTag typeList = compound.getList("Types", 8);
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
