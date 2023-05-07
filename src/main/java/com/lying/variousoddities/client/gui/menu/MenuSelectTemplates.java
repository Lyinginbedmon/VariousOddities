package com.lying.variousoddities.client.gui.menu;

import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.phys.HitResult;

public abstract class MenuSelectTemplates extends MenuCharacterCreation implements MenuProvider
{
	public MenuSelectTemplates(MenuType<MenuCharacterCreation> menuType, int containerIdIn, HitResult hitResult)
	{
		super(menuType, containerIdIn, hitResult);
	}
}
