package com.lying.variousoddities.client.renderer;

import com.lying.variousoddities.init.VOItems;
import com.lying.variousoddities.item.ItemMossBottle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;

public class ColorHandler
{
	@SuppressWarnings("deprecation")
	public static void registerColorHandlers()
	{
		ItemColors registry = Minecraft.getInstance().getItemColors();
		registry.register((stack, layer) -> { return layer == 1 ? ItemMossBottle.getColor(stack).getMaterialColor().col : -1; }, VOItems.MOSS_BOTTLE.get());
	}
}
