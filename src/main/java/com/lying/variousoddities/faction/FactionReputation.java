package com.lying.variousoddities.faction;

import javax.annotation.Nullable;

import com.lying.variousoddities.api.entity.IFactionMob;
import com.lying.variousoddities.api.event.ReputationEvent;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.faction.FactionBus.ReputationChange;
import com.lying.variousoddities.faction.Factions.Faction;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;

public class FactionReputation
{
	public static String validateName(String factionName)
	{
		return factionName.toLowerCase().replace(" ", "_");
	}
	
	public static int getPlayerReputation(PlayerEntity player, String factionName)
	{
		factionName = validateName(factionName);
		PlayerData data = PlayerData.forPlayer(player);
		int rep = data.reputation.getReputation(factionName);
		if(rep == Integer.MIN_VALUE)
		{
			// Set to starting reputation
			Faction faction = Factions.get(factionName);
			if(faction != null)
				rep = faction.startingRep;
			else
				rep = 0;
		}
		
		return rep;
	}
	
	public static int getPlayerReputation(PlayerEntity player, String factionName, @Nullable LivingEntity sourceMob)
	{
		factionName = validateName(factionName);
		int rep = getPlayerReputation(player, factionName);
		
		ReputationEvent.Get event = new ReputationEvent.Get(player, factionName, rep, sourceMob);
		MinecraftForge.EVENT_BUS.post(event);
		
		return event.getCurrentRep();
	}
	
	/**
	 * Set the given player's reputation with the given faction to the given amount, clamped to between -100 to 100
	 * @param player
	 * @param factionName
	 * @param repIn
	 * @return
	 */
	public static int setPlayerReputation(PlayerEntity player, String factionName, int repIn)
	{
		factionName = validateName(factionName);
		repIn = Math.max(-100, Math.min(100, repIn));
		
		PlayerData data = PlayerData.forPlayer(player);
		data.reputation.setReputation(factionName, repIn);
		
		return repIn;
	}
	
	/**
	 * Returns the given player's reputation with the given faction to its configured starting value
	 * @param player
	 * @param factionName
	 * @return
	 */
	public static int resetPlayerReputation(PlayerEntity player, String factionName)
	{
		factionName = validateName(factionName);
		Faction faction = Factions.get(factionName);
		
		int rep = faction == null ? 0 : faction.startingRep;
		PlayerData data = PlayerData.forPlayer(player);
		data.reputation.setReputation(factionName, rep);
		
		return rep;
	}
	
	/**
	 * Adds the given amount to the given player's reputation with the given faction, clamped to between -100 to 100
	 * @param player
	 * @param factionName
	 * @param repIn
	 * @return
	 */
	public static int addPlayerReputation(PlayerEntity player, String factionName, ReputationChange causeIn, int repIn, @Nullable LivingEntity sourceMob)
	{
		factionName = validateName(factionName);
		
		int currentReputation = getPlayerReputation(player, factionName);
		EnumAttitude initialState = EnumAttitude.fromRep(currentReputation);
		
		ReputationEvent.Change event = new ReputationEvent.Change(player, factionName, currentReputation, repIn, sourceMob, causeIn);
		if(MinecraftForge.EVENT_BUS.post(event)) return currentReputation;
		repIn = event.getChange();
		
		int rep = Math.max(-100, Math.min(100, currentReputation + repIn));
		PlayerData.forPlayer(player).reputation.setReputation(factionName, rep);
		
		EnumAttitude nextState = EnumAttitude.fromRep(rep);
		if(initialState != nextState && !player.getEntityWorld().isRemote)
		{
            addPlayerReputation(player, factionName, causeIn, player.getRNG().nextInt(10) + ((int)Math.signum(repIn) * 5), sourceMob);
			if(!MinecraftForge.EVENT_BUS.post(new ReputationEvent.Attitude(player, factionName, initialState, nextState)))
                player.sendStatusMessage(new TranslationTextComponent("gui.varodd.reputation", factionName, nextState.getTranslatedName()), true);
		}
		
		return rep;
	}
	
	/**
	 * Removes the given amount from the given player's reputation with the given faction, clamped to between -100 to 100
	 * @param player
	 * @param factionName
	 * @param repIn
	 * @return
	 */
	public static int removePlayerReputation(PlayerEntity player, String factionName, ReputationChange causeIn, int repIn, @Nullable LivingEntity sourceMob)
	{
		return addPlayerReputation(player, factionName, causeIn, -Math.abs(repIn), sourceMob);
	}
	
	public static void changePlayerReputation(PlayerEntity player, LivingEntity sourceMob, ReputationChange causeIn, int change)
	{
		changePlayerReputation(player, getFaction(sourceMob), causeIn, change, sourceMob);
	}
	
	public static void changePlayerReputation(PlayerEntity player, String faction, ReputationChange causeIn, int change, @Nullable LivingEntity sourceMob)
	{
		if(player == null || (faction == null || faction.length() == 0)) return;
		FactionReputation.addPlayerReputation(player, faction, causeIn, change, sourceMob);
	}
	
	/**
	 * Returns the faction of the given mob, if any.
	 * @param sourceMob
	 * @return A string identifying the faction of the mob, or null if it has none.
	 */
	public static String getFaction(LivingEntity sourceMob)
	{
		if(sourceMob instanceof IFactionMob)
			return ((IFactionMob)sourceMob).getFactionName();
		else if(sourceMob.getType() == EntityType.PLAYER)
			return PlayerData.forPlayer((PlayerEntity)sourceMob).reputation.factionName();
		return null;
	}
	
	/**
	 * Returns the current attitude of the given faction towards the given player, based on their reputation with that faction
	 * @param player
	 * @param factionName
	 * @return
	 */
	public static EnumAttitude getPlayerAttitude(PlayerEntity player, String factionName, @Nullable LivingEntity sourceMob)
	{
		if(factionName == null) return EnumAttitude.INDIFFERENT;
		return EnumAttitude.fromRep(getPlayerReputation(player, factionName, sourceMob));
	}
	
	public static EnumAttitude getPlayerAttitude(PlayerEntity player, LivingEntity sourceMob)
	{
		if(sourceMob == null) return EnumAttitude.INDIFFERENT;
		return EnumAttitude.fromRep(getPlayerReputation(player, getFaction(sourceMob), sourceMob));
	}
	
	public static enum EnumAttitude
	{
		HOSTILE(-100, -61, EnumInteraction.ATTACK, EnumInteraction.FLEE),
		UNFRIENDLY(-60, -21, EnumInteraction.AVOID, EnumInteraction.RETALIATE),
		INDIFFERENT(-20, 20, EnumInteraction.RETALIATE, EnumInteraction.TRADE),
		FRIENDLY(21, 60, EnumInteraction.TRADE, EnumInteraction.HEAL),
		HELPFUL(61, 100, EnumInteraction.TRADE, EnumInteraction.HEAL, EnumInteraction.DEFEND, EnumInteraction.FOLLOW);
		
		/**
		 * Implemented behaviours:
		 * 
		 * Attack
		 * Avoid
		 * Defend
		 * Flee
		 * Retaliate
		 * Trade
		 * 
		 * Missing behaviours:
		 * 
		 * Heal
		 * Follow
		 */
		
		private final int minScore, maxScore;
		private final EnumInteraction[] interactions;
		
		private EnumAttitude(int min, int max, EnumInteraction... interactionsIn)
		{
			minScore = min;
			maxScore = max;
			interactions = interactionsIn;
		}
		
		public boolean allowsInteraction(EnumInteraction interaction)
		{
			for(EnumInteraction action : interactions) if(action == interaction) return true;
			return false;
		}
		
		public String getTranslatedName()
		{
			return I18n.format("enum."+Reference.ModInfo.MOD_PREFIX+"attitude."+toString().toLowerCase()+".name");
		}
		
		public static EnumAttitude fromRep(int par1Int)
		{
			par1Int = Math.max(-100, Math.min(100, par1Int));
			for(EnumAttitude attitude : EnumAttitude.values())
				if(par1Int >= attitude.minScore && par1Int <= attitude.maxScore) return attitude;
			return INDIFFERENT;
		}
	}
	
	public static enum EnumInteraction
	{
		ATTACK,
		FLEE,
		AVOID,
		RETALIATE,
		TRADE,
		DEFEND,
		HEAL,
		FOLLOW;
	}
}
