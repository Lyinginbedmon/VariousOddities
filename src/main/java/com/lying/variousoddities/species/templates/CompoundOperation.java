package com.lying.variousoddities.species.templates;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/** A template operation containing a set of sub operations */
public class CompoundOperation extends TemplateOperation
{
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "compound");
	
	protected List<TemplateOperation> subOperations = Lists.newArrayList();
	
	public CompoundOperation()
	{
		super(Operation.SET);
	}
	
	public ResourceLocation getRegistryName(){ return REGISTRY_NAME; }
	
	public CompoundTag writeToNBT(CompoundTag compound){ return compound; }
	public void readFromNBT(CompoundTag compound){ }
	
	@Override
	public JsonObject writeToJson(JsonObject json)
	{
		json.addProperty("Name", getRegistryName().toString());
		
		JsonArray operations = new JsonArray();
		for(TemplateOperation operation : subOperations)
			operations.add(operation.writeToJson(new JsonObject()));
		
		json.add("Operations", operations);
		return json;
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		JsonArray operations = json.getAsJsonArray("Operations");
		for(int i=0; i<operations.size(); i++)
		{
			TemplateOperation operation = TemplateOperation.getFromJson(operations.get(i).getAsJsonObject());
			if(operation != null)
				this.subOperations.add(operation);
		}
	}
	
	public CompoundOperation addOperation(TemplateOperation operationIn){ this.subOperations.add(operationIn); return this; }
	
	public void applyToEntity(LivingEntity entity)
	{
		for(TemplateOperation operation : subOperations)
			operation.applyToEntity(entity);
	}
	
	public void applyToTypes(Collection<EnumCreatureType> typeSet)
	{
		for(TemplateOperation operation : subOperations)
			operation.applyToTypes(typeSet);
	}
	
	public void applyToAbilities(Map<ResourceLocation, Ability> abilityMap)
	{
		for(TemplateOperation operation : subOperations)
			operation.applyToAbilities(abilityMap);
	}
	
	public static class Builder extends TemplateOperation.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public CompoundOperation create()
		{
			return new CompoundOperation();
		}
	}
}
