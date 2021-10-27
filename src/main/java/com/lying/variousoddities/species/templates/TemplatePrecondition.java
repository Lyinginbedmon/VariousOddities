package com.lying.variousoddities.species.templates;

import java.util.EnumSet;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;

public abstract class TemplatePrecondition
{
	protected UUID templateID;
	
	public abstract ResourceLocation getRegistryName();
	
	public void setTemplateID(UUID uuidIn){ this.templateID = uuidIn;  }
	
	public ITextComponent translate(){ return new TranslationTextComponent("precondition."+Reference.ModInfo.MOD_ID+"."+getRegistryName().getPath()); }
	
	/**
	 * Tests the given entity for meeting this precondition
	 * @param entity The entity to test
	 * @param types	The creature types of the entity, typically from their species
	 * @param abilities	The ability map of the entity
	 * @return True if this precondition is met
	 */
	public final boolean isValidFor(LivingEntity entity, EnumSet<EnumCreatureType> types, Map<ResourceLocation, Ability> abilities)
	{
		return testEntity(entity) && testTypes(types) && testAbilities(abilities);
	}
	
	protected boolean testEntity(LivingEntity entity){ return true; }
	
	protected boolean testTypes(EnumSet<EnumCreatureType> types){ return true; }
	
	protected boolean testAbilities(Map<ResourceLocation, Ability> abilities){ return true; }
	
	public abstract CompoundNBT writeToNBT(CompoundNBT compound);
	
	public abstract void readFromNBT(CompoundNBT compound);
	
	public static TemplatePrecondition getFromJson(JsonObject json)
	{
		if(json.has("Name"))
		{
			ResourceLocation registryName = new ResourceLocation(json.get("Name").getAsString());
			if(VORegistries.PRECONDITIONS.containsKey(registryName))
			{
				TemplatePrecondition operation = VORegistries.PRECONDITIONS.getValue(registryName).create();
				operation.readFromJson(json);
				return operation;
			}
			else
				VariousOddities.log.error("Unrecognised template precondition: "+registryName.toString());
		}
		return null;
	}
	
	public JsonObject writeToJson(JsonObject json)
	{
		json.addProperty("Name", getRegistryName().toString());
		json.addProperty("Tag", writeToNBT(new CompoundNBT()).toString());
		return json;
	}
	
	public void readFromJson(JsonObject json)
	{
		CompoundNBT tag = new CompoundNBT();
		try
		{
			tag = JsonToNBT.getTagFromJson(json.get("Tag").getAsString());
		}
		catch (CommandSyntaxException e){ }
		if(!tag.isEmpty())
			readFromNBT(tag);
	}
	
	public static void onRegisterPreconditions(RegistryEvent.Register<Builder> event)
	{
		IForgeRegistry<Builder> registry = event.getRegistry();
		
		registry.register(new AbilityPrecondition.Builder());
		registry.register(new TypePrecondition.Builder());
		
		VariousOddities.log.info("Initialised "+registry.getEntries().size()+" template preconditions");
		if(ConfigVO.GENERAL.verboseLogs())
			for(ResourceLocation name : registry.getKeys())
				VariousOddities.log.info("#   "+name.toString());
	}
	
	public static abstract class Builder extends ForgeRegistryEntry<TemplatePrecondition.Builder>
	{
		public Builder(@Nonnull ResourceLocation registryName){ setRegistryName(registryName); }
		
		public abstract TemplatePrecondition create();
	}
}
