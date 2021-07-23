package com.lying.variousoddities.inventory;

import com.lying.variousoddities.entity.mount.EntityWarg;
import com.lying.variousoddities.init.VOItems;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;

public class ContainerWarg extends Container
{
	public static ContainerWarg fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf)
	{
		Entity mount = inv.player.getRidingEntity();
		if(mount instanceof EntityWarg)
			return new ContainerWarg(windowId, inv, ((EntityWarg)mount));
		return null;
	}
	
	private final EntityWarg theWarg;
	
	public ContainerWarg(int windowId, PlayerInventory inv, EntityWarg wargIn)
	{
		super(VOItems.CONTAINER_WARG, windowId);
		theWarg = wargIn;
	}
	
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return theWarg.isAlive() && playerIn.isAlive() && playerIn.getRidingEntity() == theWarg;
	}
}
