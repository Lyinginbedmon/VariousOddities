package com.lying.variousoddities.species.templates;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.RegistryObject;

public abstract class TemplateOperation
{
	protected UUID templateID;
	protected Operation action;
	
	private MutableComponent customText = null;
	
	public TemplateOperation(Operation actionIn)
	{
		this.action = actionIn;
	}
	
	public abstract ResourceLocation getRegistryName();
	
	public void setTemplateID(UUID uuidIn){ this.templateID = uuidIn;  }
	
	public Component translate(){ return hasCustomDisplay() ? getCustomDisplay() : Component.translatable("operation."+Reference.ModInfo.MOD_ID+"."+getRegistryName().getPath()); }
	
	public boolean hasCustomDisplay(){ return this.customText != null; }
	public MutableComponent getCustomDisplay(){ return this.customText; }
	protected TemplateOperation setCustomDisplay(MutableComponent textComponent){ this.customText = textComponent; return this; }
	
	public Operation action(){ return this.action; }
	
	public boolean canStackWith(TemplateOperation operationB){ return false; }
	
	public List<Component> stackAsList(List<TemplateOperation> operations){ return Lists.newArrayList(translate()); }
	
	public void applyToEntity(LivingEntity entity){ }
	
	public boolean ifTypes(Collection<EnumCreatureType> typesIn){ return true; }
	
	public void applyToTypes(Collection<EnumCreatureType> typeSet){ }
	
	public void applyToAbilities(Map<ResourceLocation, Ability> abilityMap){ }
	
	public abstract CompoundTag writeToNBT(CompoundTag compound);
	
	public abstract void readFromNBT(CompoundTag compound);
	
	public JsonObject writeToJson(JsonObject json)
	{
		json.addProperty("Action", this.action.getSerializedName());
		json.addProperty("Name", getRegistryName().toString());
		json.addProperty("Tag", writeToNBT(new CompoundTag()).toString());
		return json;
	}
	
	public void readFromJson(JsonObject json)
	{
		this.action = Operation.fromString(json.get("Action").getAsString());
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
		private final ResourceLocation registryName;
		
		public Builder(@Nonnull ResourceLocation registryNameIn){ registryName = registryNameIn; }
		
		public abstract TemplateOperation create();
		
		public ResourceLocation getRegistryName() { return registryName; }
	}
	
	public static TemplateOperation getFromJson(JsonObject json)
	{
		if(json.has("Name"))
		{
			ResourceLocation registryName = new ResourceLocation(json.get("Name").getAsString());
			for(RegistryObject<TemplateOperation.Builder> entry : VORegistries.OPERATIONS.getEntries())
				if(entry.isPresent() && entry.getId().equals(registryName))
				{
					TemplateOperation operation = entry.get().create();
					operation.readFromJson(json);
					return operation;
				}
			
			VariousOddities.log.error("Unrecognised template operation: "+registryName.toString());
		}
		return null;
	}
	
	public static enum Operation implements StringRepresentable
	{
		ADD,
		REMOVE,
		REMOVE_ALL,
		SET;
		
		public String getSerializedName(){ return name().toLowerCase(); }
		
		public static Operation fromString(String nameIn)
		{
			for(Operation operation : values())
				if(operation.getSerializedName().equalsIgnoreCase(nameIn))
					return operation;
			return null;
		}
	}
}
