package com.lying.variousoddities.species.templates;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.species.types.Types;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TypeOperation extends TemplateOperation
{
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "type");
	
	private EnumCreatureType[] types = null;
	private boolean removalType = false;
	
	public TypeOperation(Operation actionIn, EnumCreatureType typeIn)
	{
		this(actionIn, new EnumCreatureType[]{typeIn});
	}
	
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
	
	public ITextComponent translate()
	{
		String translationBase = "operation."+Reference.ModInfo.MOD_ID+".type.";
		switch(this.action)
		{
			case ADD:
				return new TranslationTextComponent(translationBase+"add", typesToString());
			case REMOVE:
				return new TranslationTextComponent(translationBase+"remove", typesToString());
			case REMOVE_ALL:
				return new TranslationTextComponent(translationBase+"remove_all."+(removalType ? "supertypes" : "subtypes"));
			case SET:
				return new TranslationTextComponent(translationBase+"set", (new Types(Arrays.asList(this.types)).toHeader()));
			default:
				return super.translate();
		}
	}
	
	private StringTextComponent typesToString()
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
	
	public void applyToTypes(Collection<EnumCreatureType> typeSet)
	{
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
}
