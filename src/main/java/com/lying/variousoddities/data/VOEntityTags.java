package com.lying.variousoddities.data;

import javax.annotation.Nonnull;

import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.tags.ITag;

public class VOEntityTags extends EntityTypeTagsProvider
{
    public static final ITag.INamedTag<EntityType<?>> CRABS = EntityTypeTags.getTagById(Reference.ModInfo.MOD_ID+".crabs");
    public static final ITag.INamedTag<EntityType<?>> RATS = EntityTypeTags.getTagById(Reference.ModInfo.MOD_ID+".rats");
    public static final ITag.INamedTag<EntityType<?>> SCORPIONS = EntityTypeTags.getTagById(Reference.ModInfo.MOD_ID+".scorpions");
    
	public VOEntityTags(DataGenerator p_i50784_1_, ExistingFileHelper existingFileHelper)
	{
		super(p_i50784_1_, Reference.ModInfo.MOD_ID, existingFileHelper);
	}
	
	@Nonnull
	public String getName(){ return "Various Oddities entity tags"; }
	
	protected void registerTags()
	{
		getOrCreateBuilder(CRABS)
			.add(VOEntities.CRAB, VOEntities.CRAB_GIANT);
		getOrCreateBuilder(RATS)
			.add(VOEntities.RAT, VOEntities.RAT_GIANT);
		getOrCreateBuilder(SCORPIONS)
			.add(VOEntities.SCORPION, VOEntities.SCORPION_GIANT);
	}
}
