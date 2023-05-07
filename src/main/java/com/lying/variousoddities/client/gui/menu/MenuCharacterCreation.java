package com.lying.variousoddities.client.gui.menu;

import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

public abstract class MenuCharacterCreation extends AbstractContainerMenu implements MenuProvider
{
	
	
	protected MenuCharacterCreation(MenuType<MenuCharacterCreation> menuType, int containerIdIn, HitResult hitResult)
	{
		super(menuType, containerIdIn);
	}
	
	public ItemStack quickMoveStack(Player p_38941_, int p_38942_) { return ItemStack.EMPTY; }
	
	public boolean stillValid(Player p_38874_) { return true; }
}
