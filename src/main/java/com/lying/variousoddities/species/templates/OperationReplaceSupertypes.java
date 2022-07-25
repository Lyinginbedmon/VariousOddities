package com.lying.variousoddities.species.templates;

import java.util.Collection;

import com.google.gson.JsonObject;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class OperationReplaceSupertypes extends TypeOperation
{
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "replace_supertypes");
	
	public OperationReplaceSupertypes(EnumCreatureType... typesIn)
	{
		super(Operation.ADD, typesIn);
	}
	
	public ResourceLocation getRegistryName(){ return REGISTRY_NAME; }
	
	public Component translate()
	{
		Component translation = Component.translatable("operation."+Reference.ModInfo.MOD_ID+".replace_supertypes", typesToString(this.types));
		return condition == null ? translation : condition.translate().append(translation);
	}
	
	public JsonObject writeToJson(JsonObject json)
	{
		if(this.condition != null)
			json.add("Condition", this.condition.writeToJson(new JsonObject()));
		json.addProperty("Name", getRegistryName().toString());
		json.addProperty("Tag", writeToNBT(new CompoundTag()).toString());
		return json;
	}
	
	public void readFromJson(JsonObject json)
	{
		CompoundTag tag = new CompoundTag();
		try
		{
			tag = TagParser.parseTag(json.get("Tag").getAsString());
		}
		catch (CommandSyntaxException e){ }
		if(!tag.isEmpty())
			readFromNBT(tag);
		
		if(json.has("Condition"))
			this.condition = Condition.readFromJson(json.getAsJsonObject("Condition"));
	}
	
	public void applyToTypes(Collection<EnumCreatureType> typeSet)
	{
		if(!conditionsValid(typeSet))
			return;
		
		if(this.types != null && this.types.length > 0)
		{
			TypeOperation clearSupertypes = new TypeOperation(Operation.REMOVE_ALL, true);
			clearSupertypes.applyToTypes(typeSet);
			
			TypeOperation addTypes = new TypeOperation(Operation.ADD, this.types);
			addTypes.applyToTypes(typeSet);
		}
	}
	
	public static class Builder extends TemplateOperation.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public TemplateOperation create()
		{
			return new OperationReplaceSupertypes();
		}
	}
}
