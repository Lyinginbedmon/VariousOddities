package com.lying.variousoddities.client;

import javax.annotation.Nullable;

import com.lying.variousoddities.api.world.settlement.Settlement;
import com.lying.variousoddities.world.savedata.SettlementManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SettlementManagerClient extends SettlementManager
{
	public SettlementManagerClient(CompoundTag nbt) { super(nbt); }
	
	/** Notifies all players of all settlements */
	public void notifyObservers(){ }
	
	/** Notifies the given player of all settlements in this world */
	public void notifyObserver(Player par1Player){ }
	
	/** Notifies the given player of the given settlement */
	public void notifyObserver(Player par1Player, int index, @Nullable Settlement settlement){ }
	
	/**
	 * Transmits client-side data of the given settlement to all players, largely for displaying rooms client-side.<br>
	 * Also used to remove settlements from the client when passed a valid index and a null settlement.
	 */
	public void notifyObservers(int index, Settlement settlement){ }
}
