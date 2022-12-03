package com.lying.variousoddities.species.templates;

import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public abstract class TemplatePrecondition
{
	protected UUID templateID;
	
	public abstract ResourceLocation getRegistryName();
	
	public void setTemplateID(UUID uuidIn){ this.templateID = uuidIn;  }
	
	public MutableComponent translate(){ return Component.translatable("precondition."+Reference.ModInfo.MOD_ID+"."+getRegistryName().getPath()); }
	
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
	
	public abstract CompoundTag writeToNBT(CompoundTag compound);
	
	public abstract void readFromNBT(CompoundTag compound);
	
	public static TemplatePrecondition getFromJson(JsonObject json)
	{
		if(json.has("Name"))
		{
			ResourceLocation registryName = new ResourceLocation(json.get("Name").getAsString());
			for(Entry<ResourceKey<Builder>, Builder> entry : VORegistries.PRECONDITIONS_REGISTRY.get().getEntries())
				if(entry.getKey().equals(registryName))
				{
					TemplatePrecondition operation = entry.getValue().create();
					operation.readFromJson(json);
					return operation;
				}
			VariousOddities.log.error("Unrecognised template precondition: "+registryName.toString());
		}
		return null;
	}
	
	public JsonObject writeToJson(JsonObject json)
	{
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
	}
	
	public static abstract class Builder
	{
		public static final ResourceKey<Registry<Builder>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(Reference.ModInfo.MOD_ID, "template_preconditions"));
		private final ResourceLocation registryName;
		
		public Builder(@Nonnull ResourceLocation registryNameIn){ registryName = registryNameIn; }
		
		public abstract TemplatePrecondition create();
		
		public ResourceLocation getRegistryName() { return registryName; }
	}
}
