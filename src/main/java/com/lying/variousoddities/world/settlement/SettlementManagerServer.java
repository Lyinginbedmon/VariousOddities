package com.lying.variousoddities.world.settlement;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.lying.variousoddities.api.world.settlement.Settlement;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSettlementData;
import com.lying.variousoddities.world.savedata.SettlementManager;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class SettlementManagerServer extends SettlementManager
{
	/**
	 * Updates the settlement manager, including all settlement updates
	 * @param event
	 */
	@SubscribeEvent
	public static void onWorldUpdateEvent(TickEvent.WorldTickEvent event)
	{
		if(event.isCanceled() || event.side == LogicalSide.CLIENT || event.phase != Phase.START) return;
		ServerWorld world = (ServerWorld)event.world;
		SettlementManager manager = get(world);
		if(manager != null && !manager.isEmpty())
		{
			List<Integer> ruins = new ArrayList<>();
			for(int index : manager.settlements.keySet())
			{
				Settlement settlement = manager.getSettlementByIndex(index);
				
				boolean valid = true;
				if(settlement.updateRooms(world, world.rand) || !settlement.hasRooms())
				{
					if(!settlement.hasNoAI())
						settlement.update();
					
					if(!settlement.isInvulnerable())
						if(settlement.hasMarker() && !settlement.validateMarker() || settlement.isAbandoned())
						{
							valid = false;
							settlement.invalidate();
							ruins.add(index);
						}
					manager.markDirty();
				}
				manager.notifyObservers(index, valid ? settlement : null);
			}
			
			manager.removeAll(ruins);
		}
	}
	
	/**
	 * Notifies players of all settlements in the world upon login
	 * @param event
	 */
	@SubscribeEvent
	public static void onPlayerLogInEvent(EntityJoinWorldEvent event)
	{
		if(event.getEntity().getType() == EntityType.PLAYER)
		{
			World world = event.getWorld();
			if(world != null && !world.isRemote)
				SettlementManager.get((ServerWorld)world).notifyObserver((PlayerEntity)event.getEntity());
		}
	}
	
	/**
	 * Transmits client-side data of the given settlement to all players, largely for displaying rooms client-side.<br>
	 * Also used to remove settlements from the client when passed a valid index and a null settlement.
	 */
	public void notifyObservers(int index, @Nullable Settlement settlement)
	{
		if(world == null || world.isRemote) return;
		for(PlayerEntity player : world.getPlayers())
			notifyObserver((PlayerEntity)player, index, settlement);
	}
	
	/** Notifies all players of all settlements */
	public void notifyObservers()
	{
		if(world == null || world.isRemote)
			return;
		else
			for(int index : settlements.keySet())
				PacketHandler.sendToAll((ServerWorld)world, new PacketSettlementData(settlementToClientNBT(index, settlements.get(index), new CompoundNBT())));
	}
	
	/** Notifies the given player of all settlements in this world */
	public void notifyObserver(PlayerEntity par1Player)
	{
		if(par1Player == null) return;
		if(world.isRemote)
			/*PacketHandler.sendToServer(new PacketSettlementData())*/;
		else
			for(int index : settlements.keySet())
				notifyObserver(par1Player, index, settlements.get(index));
	}
	
	/** Notifies the given player of the given settlement */
	public void notifyObserver(PlayerEntity par1Player, int index, @Nullable Settlement settlement)
	{
		if(par1Player == null) return;
			PacketHandler.sendTo((ServerPlayerEntity)par1Player, new PacketSettlementData(settlementToClientNBT(index, settlement, new CompoundNBT())));
	}
}
