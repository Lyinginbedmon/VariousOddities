package com.lying.variousoddities.species.templates;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;

public abstract class TemplateOperation
{
	protected UUID templateID;
	protected Operation action;
	
	public TemplateOperation(Operation actionIn)
	{
		this.action = actionIn;
	}
	
	public abstract ResourceLocation getRegistryName();
	
	public void setTemplateID(UUID uuidIn){ this.templateID = uuidIn;  }
	
	public ITextComponent translate(){ return new TranslationTextComponent("operation."+Reference.ModInfo.MOD_ID+"."+getRegistryName().getPath()); }
	
	public void applyToEntity(LivingEntity entity){ }
	
	public boolean ifTypes(Collection<EnumCreatureType> typesIn){ return true; }
	
	public void applyToTypes(Collection<EnumCreatureType> typeSet){ }
	
	public void applyToAbilities(Map<ResourceLocation, Ability> abilityMap){ }
	
	public abstract CompoundNBT writeToNBT(CompoundNBT compound);
	
	public abstract void readFromNBT(CompoundNBT compound);
	
	public JsonObject writeToJson(JsonObject json)
	{
		json.addProperty("Action", this.action.getString());
		json.addProperty("Name", getRegistryName().toString());
		json.addProperty("Tag", writeToNBT(new CompoundNBT()).toString());
		return json;
	}
	
	public void readFromJson(JsonObject json)
	{
		this.action = Operation.fromString(json.get("Action").getAsString());
		CompoundNBT tag = new CompoundNBT();
		try
		{
			tag = JsonToNBT.getTagFromJson(json.get("Tag").getAsString());
		}
		catch (CommandSyntaxException e){ }
		if(!tag.isEmpty())
			readFromNBT(tag);
	}
	
	public static abstract class Builder extends ForgeRegistryEntry<TemplateOperation.Builder>
	{
		public Builder(@Nonnull ResourceLocation registryName){ setRegistryName(registryName); }
		
		public abstract TemplateOperation create();
	}
	
	public static void onRegisterOperations(RegistryEvent.Register<Builder> event)
	{
		IForgeRegistry<Builder> registry = event.getRegistry();
		
		registry.register(new TypeOperation.Builder());
		registry.register(new AbilityOperation.Builder());
		registry.register(new CompoundOperation.Builder());
		registry.register(new OperationReplaceSupertypes.Builder());
		
		VariousOddities.log.info("Initialised "+registry.getEntries().size()+" template operations");
		if(ConfigVO.GENERAL.verboseLogs())
			for(ResourceLocation name : registry.getKeys())
				VariousOddities.log.info("#   "+name.toString());
	}
	
	public static TemplateOperation getFromJson(JsonObject json)
	{
		if(json.has("Name"))
		{
			ResourceLocation registryName = new ResourceLocation(json.get("Name").getAsString());
			if(VORegistries.OPERATIONS.containsKey(registryName))
			{
				TemplateOperation operation = VORegistries.OPERATIONS.getValue(registryName).create();
				operation.readFromJson(json);
				return operation;
			}
			else
				VariousOddities.log.error("Unrecognised template operation: "+registryName.toString());
		}
		return null;
	}
	
	public static enum Operation implements IStringSerializable
	{
		ADD,
		REMOVE,
		REMOVE_ALL,
		SET;
		
		public String getString(){ return name().toLowerCase(); }
		
		public static Operation fromString(String nameIn)
		{
			for(Operation operation : values())
				if(operation.getString().equalsIgnoreCase(nameIn))
					return operation;
			return null;
		}
	}
}
