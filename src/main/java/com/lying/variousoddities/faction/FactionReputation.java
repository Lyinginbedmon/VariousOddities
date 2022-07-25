package com.lying.variousoddities.faction;

import java.util.EnumSet;

import javax.annotation.Nullable;

import com.lying.variousoddities.api.entity.IFactionMob;
import com.lying.variousoddities.api.event.ReputationEvent;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.condition.Conditions;
import com.lying.variousoddities.faction.FactionBus.ReputationChange;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.world.savedata.FactionManager;
import com.lying.variousoddities.world.savedata.FactionManager.Faction;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

public class FactionReputation
{
	public static String validateName(String factionName)
	{
		return factionName.toLowerCase().replace(" ", "_");
	}
	
	public static int getPlayerReputation(Player player, String factionName)
	{
		factionName = validateName(factionName);
		PlayerData data = PlayerData.forPlayer(player);
		if(data == null)
			return 0;
		
		int rep = data.reputation.getReputation(factionName);
		if(rep == Integer.MIN_VALUE)
		{
			// Set to starting reputation
			FactionManager manager = FactionManager.get(player.getLevel());
			Faction faction = manager.getFaction(factionName);
			if(faction != null)
				rep = faction.startingRep;
			else
				rep = 0;
		}
		
		return rep;
	}
	
	public static int getPlayerReputation(Player player, String factionName, @Nullable LivingEntity sourceMob)
	{
		factionName = validateName(factionName);
		int rep = getPlayerReputation(player, factionName);
		
		if(sourceMob != null)
		{
			LivingData data = LivingData.forEntity(sourceMob);
			if(data != null)
				if(data.hasCondition(Conditions.DOMINATED, player))
					rep = 100;
				else if(data.hasCondition(Conditions.CHARMED, player))
					rep = 50;
		}
		
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
	public static int setPlayerReputation(Player player, String factionName, int repIn)
	{
		factionName = validateName(factionName);
		repIn = Math.max(-100, Math.min(100, repIn));
		
		PlayerData data = PlayerData.forPlayer(player);
		if(data != null)
			data.reputation.setReputation(factionName, repIn);
		
		return repIn;
	}
	
	/**
	 * Returns the given player's reputation with the given faction to its configured starting value
	 * @param player
	 * @param factionName
	 * @return
	 */
	public static int resetPlayerReputation(Player player, String factionName)
	{
		factionName = validateName(factionName);
		FactionManager manager = FactionManager.get(player.getLevel());
		Faction faction = manager.getFaction(factionName);
		
		int rep = faction == null ? 0 : faction.startingRep;
		PlayerData data = PlayerData.forPlayer(player);
		if(data != null)
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
	public static int addPlayerReputation(Player player, String factionName, ReputationChange causeIn, int repIn, @Nullable LivingEntity sourceMob)
	{
		factionName = validateName(factionName);
		
		int currentReputation = getPlayerReputation(player, factionName);
		EnumAttitude initialState = EnumAttitude.fromRep(currentReputation);
		
		ReputationEvent.Change event = new ReputationEvent.Change(player, factionName, currentReputation, repIn, sourceMob, causeIn);
		if(MinecraftForge.EVENT_BUS.post(event)) return currentReputation;
		repIn = event.getChange();
		
		int rep = Math.max(-100, Math.min(100, currentReputation + repIn));
		PlayerData data = PlayerData.forPlayer(player);
		if(data == null)
			return 0;
		data.reputation.setReputation(factionName, rep);
		
		EnumAttitude nextState = EnumAttitude.fromRep(rep);
		if(initialState != nextState && !player.getLevel().isClientSide)
		{
            addPlayerReputation(player, factionName, causeIn, player.getRandom().nextInt(10) + ((int)Math.signum(repIn) * 5), sourceMob);
			if(!MinecraftForge.EVENT_BUS.post(new ReputationEvent.Attitude(player, factionName, initialState, nextState)))
                player.displayClientMessage(Component.translatable("gui.varodd.reputation", factionName, nextState.getTranslatedName()), true);
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
	public static int removePlayerReputation(Player player, String factionName, ReputationChange causeIn, int repIn, @Nullable LivingEntity sourceMob)
	{
		return addPlayerReputation(player, factionName, causeIn, -Math.abs(repIn), sourceMob);
	}
	
	public static void changePlayerReputation(Player player, LivingEntity sourceMob, ReputationChange causeIn, int change)
	{
		changePlayerReputation(player, getFaction(sourceMob), causeIn, change, sourceMob);
	}
	
	public static void changePlayerReputation(Player player, String faction, ReputationChange causeIn, int change, @Nullable LivingEntity sourceMob)
	{
		if(player == null || (faction == null || faction.length() == 0)) return;
		
		FactionManager manager = FactionManager.get(player.getLevel());
		if(manager == null || manager.getFaction(faction) == null) return;
		
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
		else if(sourceMob.getType() == EntityType.PLAYER && PlayerData.forPlayer((Player)sourceMob) != null)
			return PlayerData.forPlayer((Player)sourceMob).reputation.factionName();
		return null;
	}
	
	/**
	 * Returns the current attitude of the given faction towards the given player, based on their reputation with that faction
	 * @param player
	 * @param factionName
	 * @return
	 */
	public static EnumAttitude getPlayerAttitude(Player player, String factionName, @Nullable LivingEntity sourceMob)
	{
		if(factionName == null) return EnumAttitude.INDIFFERENT;
		return EnumAttitude.fromRep(getPlayerReputation(player, factionName, sourceMob));
	}
	
	public static EnumAttitude getPlayerAttitude(Player player, LivingEntity sourceMob)
	{
		if(sourceMob == null) return EnumAttitude.INDIFFERENT;
		return EnumAttitude.fromRep(getPlayerReputation(player, getFaction(sourceMob), sourceMob));
	}
	
	public static enum EnumAttitude
	{
		HOSTILE(-100, -61, EnumSet.of(EnumInteraction.ATTACK, EnumInteraction.FLEE)),
		UNFRIENDLY(-60, -21, EnumSet.of(EnumInteraction.AVOID, EnumInteraction.RETALIATE)),
		INDIFFERENT(-20, 20, EnumSet.of(EnumInteraction.RETALIATE, EnumInteraction.TRADE)),
		FRIENDLY(21, 60, EnumSet.of(EnumInteraction.TRADE, EnumInteraction.HEAL)),
		HELPFUL(61, 100, EnumSet.of(EnumInteraction.TRADE, EnumInteraction.HEAL, EnumInteraction.DEFEND, EnumInteraction.FOLLOW));
		
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
		private final EnumSet<EnumInteraction> interactions;
		
		private EnumAttitude(int min, int max, EnumSet<EnumInteraction> interactionsIn)
		{
			minScore = min;
			maxScore = max;
			interactions = interactionsIn;
		}
		
		public boolean allowsInteraction(EnumInteraction interaction)
		{
			return interactions.contains(interaction);
		}
		
		public Component getTranslatedName()
		{
			return Component.translatable("enum."+Reference.ModInfo.MOD_ID+".attitude."+toString().toLowerCase()).withStyle((style) -> { return style.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, Component.translatable("enum."+Reference.ModInfo.MOD_ID+".attitude."+toString().toLowerCase()+".definition"))); });
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
