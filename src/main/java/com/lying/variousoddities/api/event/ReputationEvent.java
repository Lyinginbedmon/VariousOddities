package com.lying.variousoddities.api.event;

import javax.annotation.Nullable;

import com.lying.variousoddities.faction.FactionBus.ReputationChange;
import com.lying.variousoddities.faction.FactionReputation.EnumAttitude;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

public class ReputationEvent extends PlayerEvent
{
	private final String faction;
	private final LivingEntity sourceMob;
	
	public ReputationEvent(PlayerEntity playerIn, String factionIn, @Nullable LivingEntity sourceIn)
	{
		super(playerIn);
		faction = factionIn;
		sourceMob = sourceIn;
	}
	
	public String getFaction(){ return faction; }
	public LivingEntity getSource(){ return sourceMob; }
	
	/**
	 * Fired whenever a player's reputation is retrieved, but will not be stored.<br>
	 * Manipulate the reputation value to alter  perception of the player, without affecting their actual reputation.
     * This event is not {@link Cancelable}.<br>
     * <br>
     * This event does not have a result. {@link HasResult}<br>
	 * @author Lying<br>
	 *
	 */
	public static class Get extends ReputationEvent
	{
		private int currentRep;
		
		public Get(PlayerEntity playerIn, String factionIn, int repIn, LivingEntity sourceIn)
		{
			super(playerIn, factionIn, sourceIn);
			currentRep = repIn;
		}
		
		public int getCurrentRep(){ return currentRep; }
		public void setCurrentRep(int par1Int){ currentRep = par1Int; }
	}

	/**
	 * Fired whenever a player's stored reputation value exceeds the threshold to alter the corresponding attitude.<br>
     * This event is {@link Cancelable}.<br>
     * If the event is canceled, the change is not reported to the player.<br>
     * <br>
     * This event does not have a result. {@link HasResult}<br>
	 * @author Lying<br>
	 */
	public static class Attitude extends ReputationEvent
	{
		private final EnumAttitude oldAttitude;
		private final EnumAttitude newAttitude;
		
		public Attitude(PlayerEntity playerIn, String factionIn, EnumAttitude oldAtt, EnumAttitude newAtt)
		{
			super(playerIn, factionIn, null);
			oldAttitude = oldAtt;
			newAttitude = newAtt;
		}
		
		public EnumAttitude getFormerAttitude(){ return oldAttitude; }
		public EnumAttitude getNewAttitude(){ return newAttitude; }
		
		public boolean isCancelable(){ return true; }
		
	}
	
	/**
	 * Fired whenever a player's reputation would be changed and the result stored.<br>
     * This event is {@link Cancelable}.<br>
     * If this event is canceled, the change does not occur.<br>
     * <br>
     * This event does not have a result. {@link HasResult}<br>
	 * @author Lying<br>
	 *
	 */
	@Cancelable
	public static class Change extends ReputationEvent
	{
		private final int currentRep;
		private int repChange;
		private final ReputationChange cause;
		
		public Change(PlayerEntity playerIn, String factionIn, int repIn, int changeIn, @Nullable LivingEntity sourceIn, ReputationChange causeIn)
		{
			super(playerIn, factionIn, sourceIn);
			currentRep = repIn;
			repChange = changeIn;
			cause = causeIn;
		}
		
		public int getCurrentRep(){ return currentRep; }
		public ReputationChange getCause(){ return cause; }
		public int getChange(){ return repChange; }
		public void setChange(int par1Int){ repChange = par1Int; }
		
		public boolean isCancelable(){ return true; }
	}
}
