package com.lying.variousoddities.data;

import javax.annotation.Nonnull;

import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;

public class VOEntityTags extends TagsProvider<EntityType<?>>
{
    public static final TagKey<EntityType<?>> CRABS = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(Reference.ModInfo.MOD_ID,"crabs"));
    public static final TagKey<EntityType<?>> RATS = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(Reference.ModInfo.MOD_ID,"rats"));
    public static final TagKey<EntityType<?>> SCORPIONS = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(Reference.ModInfo.MOD_ID,"scorpions"));
    
	@SuppressWarnings("deprecation")
	public VOEntityTags(DataGenerator p_i50784_1_, ExistingFileHelper existingFileHelper)
	{
		super(p_i50784_1_, Registry.ENTITY_TYPE, Reference.ModInfo.MOD_ID, existingFileHelper);
	}
	
	@Nonnull
	public String getName(){ return "Various Oddities entity tags"; }
	
	protected void addTags()
	{
		tag(CRABS)
			.add(VOEntities.CRAB, VOEntities.CRAB_GIANT);
		tag(RATS)
			.add(VOEntities.RAT, VOEntities.RAT_GIANT);
		tag(SCORPIONS)
			.add(VOEntities.SCORPION, VOEntities.SCORPION_GIANT);
	}
}
