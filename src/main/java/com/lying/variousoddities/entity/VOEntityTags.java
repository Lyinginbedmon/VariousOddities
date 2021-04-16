package com.lying.variousoddities.entity;

import javax.annotation.Nonnull;

import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.EntityTypeTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class VOEntityTags extends EntityTypeTagsProvider
{
	public VOEntityTags(DataGenerator p_i50784_1_, ExistingFileHelper existingFileHelper)
	{
		super(p_i50784_1_, Reference.ModInfo.MOD_ID, existingFileHelper);
	}
	
	@Nonnull
	public String getName(){ return "Various Oddities entity tags"; }
	
	protected void registerTags()
	{
		getOrCreateBuilder(VOEntities.CRABS)
			.add(VOEntities.CRAB, VOEntities.CRAB_GIANT);
		getOrCreateBuilder(VOEntities.RATS)
			.add(VOEntities.RAT, VOEntities.RAT_GIANT);
		getOrCreateBuilder(VOEntities.SCORPIONS)
			.add(VOEntities.SCORPION, VOEntities.SCORPION_GIANT);
	}
}
