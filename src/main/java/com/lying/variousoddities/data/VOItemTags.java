package com.lying.variousoddities.data;

import javax.annotation.Nullable;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags.IOptionalNamedTag;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class VOItemTags extends ItemTagsProvider
{
    public static final IOptionalNamedTag<Item> WORG_FOOD = ItemTags.createOptional(new ResourceLocation(Reference.ModInfo.MOD_ID, "worg_food"));
    
	public VOItemTags(DataGenerator dataGenerator, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(dataGenerator, new VOBlockTags(dataGenerator, existingFileHelper), Reference.ModInfo.MOD_ID, existingFileHelper);
	}
	
	@Override
	public String getName()
	{
		return "Various Oddities item tags";
	}
	
	protected void registerTags()
	{
		getOrCreateBuilder(WORG_FOOD).add(
				Items.ROTTEN_FLESH,
				Items.CHICKEN,
				Items.MUTTON,
				Items.BEEF,
				Items.PORKCHOP,
				Items.SALMON,
				Items.TROPICAL_FISH
				);
	}
}
