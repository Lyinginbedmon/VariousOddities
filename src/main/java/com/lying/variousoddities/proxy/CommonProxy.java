package com.lying.variousoddities.proxy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent;

public abstract class CommonProxy implements IProxy
{
	public PlayerEntity getPlayerEntity(NetworkEvent.Context ctx)
	{
		return ctx.getSender();
	}
	
	public void clearSettlements(){ }
	
	public void openSpeciesSelectScreen(PlayerEntity entity, int power, boolean randomise){ }
}
