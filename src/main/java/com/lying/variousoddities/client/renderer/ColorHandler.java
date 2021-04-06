package com.lying.variousoddities.client.renderer;

import com.lying.variousoddities.init.VOItems;
import com.lying.variousoddities.item.ItemMossBottle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.ItemColors;

public class ColorHandler
{
	public static void registerColorHandlers()
	{
		ItemColors registry = Minecraft.getInstance().getItemColors();
		registry.register((stack, layer) -> { return layer == 1 ? ItemMossBottle.getColor(stack).getColorValue() : -1; }, VOItems.MOSS_BOTTLE);
	}
}
