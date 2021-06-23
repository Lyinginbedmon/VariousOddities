package com.lying.variousoddities.species;

import java.util.Collection;
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
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.templates.TemplateOperation;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class Template
{
	/** True if players can select this template after choosing their species */
	private boolean chooseAtStart = false;
	private int power = 0;
	
	private final List<TemplateOperation> operations = Lists.newArrayList();
	
	private ResourceLocation registryName;
	private UUID templateID;
	
	public Template(){ }
	public Template(ResourceLocation name, UUID uuid)
	{
		setRegistryName(name);
		this.templateID = uuid;
	}
	
	public void setRegistryName(ResourceLocation name){ this.registryName = name; }
	public ResourceLocation getRegistryName(){ return this.registryName; }
	
	public UUID uuid(){ return this.templateID; }
	
	/** True if players can select this template after choosing their species */
	public boolean isPlayerSelectable(){ return this.chooseAtStart; }
	
	public Template setPlayerSelect(boolean bool)
	{
		this.chooseAtStart = bool;
		return this;
	}
	
	/**
	 * A rating of how powerful this template is in comparison to others.<br>
	 * Such as high health & powerful abilities vs low health and utility abilities.<br>
	 * Purely cosmetic, meant as a way of comparing player-selectable species.
	 */
	public int getPower(){ return this.power; }
	
	public Template setPower(@Nonnull int par1Int)
	{
		this.power = MathHelper.clamp(par1Int, 0, 10);
		return this;
	}
	
	public Template addOperation(TemplateOperation operationIn)
	{
		operationIn.setTemplateID(this.templateID);
		this.operations.add(operationIn);
		return this;
	}
	
	public List<TemplateOperation> getOperations(){ return this.operations; }
	
	public JsonObject toJson()
	{
		JsonObject json = new JsonObject();
		
		json.addProperty("UUID", this.templateID.toString());
		
		json.addProperty("CanPlayerSelect", this.isPlayerSelectable());
		json.addProperty("Power", getPower());
		
		JsonArray operations = new JsonArray();
			for(TemplateOperation operation : this.operations)
				operations.add(operation.writeToJson(new JsonObject()));
		json.add("Operations", operations);
		
		return json;
	}
	
	public void applyTypeOperations(Collection<EnumCreatureType> typesIn)
	{
		for(TemplateOperation operation : operations)
			operation.applyToTypes(typesIn);
	}
	
	public void applyAbilityOperations(Map<ResourceLocation, Ability> abilityMap)
	{
		for(TemplateOperation operation : operations)
			operation.applyToAbilities(abilityMap);
	}
	
	public static Template fromJson(@Nullable JsonElement json)
	{
		if(json == null)
			return null;
		JsonObject object = json.getAsJsonObject();
		
		Template template = new Template();
		
		if(object.has("UUID"))
			template.templateID = UUID.fromString(object.get("UUID").getAsString());
		
		if(object.has("CanPlayerSelect"))
			template.setPlayerSelect(object.get("CanPlayerSelect").getAsBoolean());
		
		if(object.has("Power"))
			template.setPower(object.get("Power").getAsInt());
		
		if(object.has("Operations"))
		{
			JsonArray operationList = object.get("Operations").getAsJsonArray();
			for(int i=0; i<operationList.size(); i++)
			{
				TemplateOperation operation = TemplateOperation.getFromJson(operationList.get(i).getAsJsonObject());
				if(operation != null)
					template.addOperation(operation);
				else
					VariousOddities.log.error("!! Error loading template operation");
			}
		}
		if(template.getOperations().isEmpty())
			VariousOddities.log.warn("Template has no operations, was this intentional?");
		
		return template;
	}
}