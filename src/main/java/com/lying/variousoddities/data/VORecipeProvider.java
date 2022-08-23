package com.lying.variousoddities.data;

import java.util.function.Consumer;

import com.lying.variousoddities.item.ItemHeldFlag;
import com.lying.variousoddities.item.ItemHeldFlag.EnumPrideType;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.util.IItemProvider;
import net.minecraft.world.item.Item;

public class VORecipeProvider extends RecipeProvider
{
	public VORecipeProvider(DataGenerator generatorIn)
	{
		super(generatorIn);
	}
	
	@Override
	protected void registerRecipes(Consumer<IFinishedRecipe> consumer)
	{
		Item rainbowFlag = ItemHeldFlag.getItem(EnumPrideType.GAY);
		for(EnumPrideType pride : ItemHeldFlag.EnumPrideType.values())
		{
			EnumPrideType next = EnumPrideType.values()[(pride.ordinal() + 1) % EnumPrideType.values().length];
			ShapelessRecipeBuilder.shapelessRecipe(ItemHeldFlag.getItem(next))
				.addIngredient(ItemHeldFlag.getItem(pride)).setGroup("pride_flag")
				.addCriterion("has_flag", hasItem(rainbowFlag)).build(consumer);
		}
	}
	
	protected static InventoryChangeTrigger.Instance hasItems(IItemProvider... items)
	{
		ItemPredicate[] predicates = new ItemPredicate[items.length];
		for(int i=0; i<items.length; i++)
			predicates[i] = ItemPredicate.Builder.create().item(items[i]).build();
		return hasItem(predicates);
	}
	
	@Override
	public String getName()
	{
		return "Various Oddities crafting recipes";
	}
}
