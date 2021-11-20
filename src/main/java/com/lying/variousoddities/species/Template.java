package com.lying.variousoddities.species;

import java.util.Collection;
import java.util.EnumSet;
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
import com.lying.variousoddities.species.templates.TemplatePrecondition;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class Template
{
	private int power = 0;
	
	private final List<TemplatePrecondition> preconditions = Lists.newArrayList();
	private final List<TemplateOperation> operations = Lists.newArrayList();
	
	private ResourceLocation registryName;
	private UUID templateID;
	
	private ITextComponent customName = null;
	
	public Template(){ }
	public Template(ResourceLocation name, UUID uuid)
	{
		setRegistryName(name);
		this.templateID = uuid;
	}
	
	public void setRegistryName(ResourceLocation name){ this.registryName = name; }
	public ResourceLocation getRegistryName(){ return this.registryName; }
	
	public UUID uuid(){ return this.templateID; }
	
	public ITextComponent getDisplayName()
	{
		if(this.customName != null)
			return this.customName;
		String path = this.registryName.getPath();
		path = (path.substring(0, 1).toUpperCase() + path.substring(1)).replace('_', ' ');
		return new StringTextComponent(path);
	}
	
	public Template setDisplayName(ITextComponent nameIn)
	{
		this.customName = nameIn;
		return this;
	}
	
	public Template addPrecondition(TemplatePrecondition preconditionIn)
	{
		this.preconditions.add(preconditionIn);
		return this;
	}
	
	public List<TemplatePrecondition> getPreconditions(){ return this.preconditions; }
	
	public boolean isApplicableTo(LivingEntity entity, EnumSet<EnumCreatureType> types, Map<ResourceLocation, Ability> abilities)
	{
		for(TemplatePrecondition precondition : preconditions)
			if(!precondition.isValidFor(entity, types, abilities))
				return false;
		return true;
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
		
		json.addProperty("Power", getPower());
		
		if(this.customName != null)
			json.addProperty("CustomName", ITextComponent.Serializer.toJson(this.customName));
		
		JsonArray operations = new JsonArray();
			for(TemplateOperation operation : this.operations)
				operations.add(operation.writeToJson(new JsonObject()));
		json.add("Operations", operations);
		
		JsonArray preconditions = new JsonArray();
			for(TemplatePrecondition precondition : this.preconditions)
				preconditions.add(precondition.writeToJson(new JsonObject()));
		json.add("Preconditions", preconditions);
		
		return json;
	}
	
	public void applyTypeOperations(Collection<EnumCreatureType> typesIn)
	{
		operations.forEach((operation) -> { operation.applyToTypes(typesIn); });
	}
	
	public void applyAbilityOperations(Map<ResourceLocation, Ability> abilityMap)
	{
		operations.forEach((operation) -> { operation.applyToAbilities(abilityMap); });
	}
	
	public static Template fromJson(@Nullable JsonElement json)
	{
		if(json == null)
			return null;
		JsonObject object = json.getAsJsonObject();
		
		Template template = new Template();
		
		if(object.has("UUID"))
			template.templateID = UUID.fromString(object.get("UUID").getAsString());
		
		if(object.has("Power"))
			template.setPower(object.get("Power").getAsInt());
		
		if(object.has("CustomName"))
		{
			String s = object.get("CustomName").getAsString();
			try
			{
				template.setDisplayName(ITextComponent.Serializer.getComponentFromJson(s));
			}
			catch (Exception exception)
			{
				VariousOddities.log.warn("Failed to parse template display name {}", s, exception);
			}
		}
		
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
		
		if(object.has("Preconditions"))
		{
			JsonArray preconditionList = object.get("Preconditions").getAsJsonArray();
			for(int i=0; i<preconditionList.size(); i++)
			{
				TemplatePrecondition precondition = TemplatePrecondition.getFromJson(preconditionList.get(i).getAsJsonObject());
				if(precondition != null)
					template.addPrecondition(precondition);
				else
					VariousOddities.log.error("!! Error loading template precondition");
			}
		}
		
		return template;
	}
}