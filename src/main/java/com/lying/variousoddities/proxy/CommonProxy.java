package com.lying.variousoddities.proxy;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public abstract class CommonProxy implements IProxy
{
	public Player getPlayerEntity(NetworkEvent.Context ctx)
	{
		return ctx.getSender();
	}
	
	public void clearSettlements(){ }
	
	public void openSpeciesSelectScreen(Player entity, int power, boolean randomise){ }
}
