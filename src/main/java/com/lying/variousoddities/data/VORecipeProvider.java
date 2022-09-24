package com.lying.variousoddities.data;

import java.util.function.Consumer;

import com.lying.variousoddities.item.ItemHeldFlag;
import com.lying.variousoddities.item.ItemHeldFlag.EnumPrideType;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;

public class VORecipeProvider extends RecipeProvider
{
	public VORecipeProvider(DataGenerator generatorIn)
	{
		super(generatorIn);
	}
	
	protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer)
	{
		Item rainbowFlag = ItemHeldFlag.getItem(EnumPrideType.GAY);
		for(EnumPrideType pride : ItemHeldFlag.EnumPrideType.values())
		{
			EnumPrideType next = EnumPrideType.values()[(pride.ordinal() + 1) % EnumPrideType.values().length];
			ShapelessRecipeBuilder.shapeless(ItemHeldFlag.getItem(next))
				.requires(ItemHeldFlag.getItem(pride)).group("pride_flag")
				.unlockedBy("has_flag", has(rainbowFlag)).save(consumer);
		}
	}
	
	public String getName()
	{
		return "Various Oddities crafting recipes";
	}
}
