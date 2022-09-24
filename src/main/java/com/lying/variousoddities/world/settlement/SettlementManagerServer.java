package com.lying.variousoddities.world.settlement;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.lying.variousoddities.api.world.settlement.Settlement;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSettlementData;
import com.lying.variousoddities.world.savedata.SettlementManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class SettlementManagerServer extends SettlementManager
{
	public SettlementManagerServer(CompoundTag nbt) { super(nbt); }
	
	/**
	 * Updates the settlement manager, including all settlement updates
	 * @param event
	 */
	@SubscribeEvent
	public static void onWorldUpdateEvent(TickEvent.LevelTickEvent event)
	{
		if(event.isCanceled() || event.side == LogicalSide.CLIENT || event.phase != Phase.START) return;
		ServerLevel world = (ServerLevel)event.level;
		SettlementManager manager = get(world);
		if(manager != null && !manager.isEmpty())
		{
			List<Integer> ruins = new ArrayList<>();
			for(int index : manager.settlements.keySet())
			{
				Settlement settlement = manager.getSettlementByIndex(index);
				
				boolean valid = true;
				if(settlement.updateRooms(world, world.getRandom()) || !settlement.hasRooms())
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
					manager.setDirty();
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
	public static void onPlayerLogInEvent(EntityJoinLevelEvent event)
	{
		if(event.getEntity().getType() == EntityType.PLAYER)
		{
			Level world = event.getLevel();
			if(world != null && !world.isClientSide)
				SettlementManager.get((ServerLevel)world).notifyObserver((Player)event.getEntity());
		}
	}
	
	/**
	 * Transmits client-side data of the given settlement to all players, largely for displaying rooms client-side.<br>
	 * Also used to remove settlements from the client when passed a valid index and a null settlement.
	 */
	public void notifyObservers(int index, @Nullable Settlement settlement)
	{
		if(world == null || world.isClientSide) return;
		for(Player player : world.players())
			notifyObserver((Player)player, index, settlement);
	}
	
	/** Notifies all players of all settlements */
	public void notifyObservers()
	{
		if(world == null || world.isClientSide)
			return;
		else
			for(int index : settlements.keySet())
				PacketHandler.sendToAll((ServerLevel)world, new PacketSettlementData(settlementToClientNBT(index, settlements.get(index), new CompoundTag())));
	}
	
	/** Notifies the given player of all settlements in this world */
	public void notifyObserver(Player par1Player)
	{
		if(par1Player == null) return;
		if(world.isClientSide)
			/*PacketHandler.sendToServer(new PacketSettlementData())*/;
		else
			for(int index : settlements.keySet())
				notifyObserver(par1Player, index, settlements.get(index));
	}
	
	/** Notifies the given player of the given settlement */
	public void notifyObserver(Player par1Player, int index, @Nullable Settlement settlement)
	{
		if(par1Player == null) return;
			PacketHandler.sendTo((ServerPlayer)par1Player, new PacketSettlementData(settlementToClientNBT(index, settlement, new CompoundTag())));
	}
}
