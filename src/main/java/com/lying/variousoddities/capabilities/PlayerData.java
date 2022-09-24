package com.lying.variousoddities.capabilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.api.event.PlayerChangeConditionEvent;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.entity.AbstractBody;
import com.lying.variousoddities.init.VOCapabilities;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSyncPlayerData;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PlayerData implements ICapabilitySerializable<CompoundTag>
{
	public static final ResourceLocation IDENTIFIER = new ResourceLocation(Reference.ModInfo.MOD_ID, "player_data");
	public static final int DEAD_TIME = Reference.Values.TICKS_PER_SECOND * 15;
	
	private final LazyOptional<PlayerData> handler;
	
	private Player player = null;
	
	public Reputation reputation = new Reputation();
	
	protected BodyCondition conditionBody = BodyCondition.ALIVE;
	protected SoulCondition conditionSoul = SoulCondition.ALIVE;
	
	protected UUID bodyUUID = null;
	protected int deadTimer = DEAD_TIME;
	
	private boolean isDirty = true;
	
	public PlayerData()
	{
		this.handler = LazyOptional.of(() -> this);
	}
	
	public LazyOptional<PlayerData> handler(){ return this.handler; }
	
	public static PlayerData forPlayer(Player player)
	{
		if(player == null)
			return null;
		
		PlayerData data = player.getCapability(VOCapabilities.PLAYER_DATA).orElse(null);
		if(data != null)
			data.player = player;
		return data;
	}
	
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		return VOCapabilities.PLAYER_DATA.orEmpty(cap, this.handler);
	}
	
	public CompoundTag serializeNBT()
	{
		CompoundTag compound = new CompoundTag();
		
		compound.put("Reputation", this.reputation.serializeNBT(new CompoundTag()));
		compound.putString("Body", conditionBody.getSerializedName());
		compound.putString("Soul", conditionSoul.getSerializedName());
		if(this.bodyUUID != null)
			compound.putUUID("BodyUUID", this.bodyUUID);
		if(isBodyDead())
			compound.putInt("DeadTicks", this.deadTimer);
		return compound;
	}
	
	public void deserializeNBT(CompoundTag nbt)
	{
		if(nbt.contains("Reputation"))
			this.reputation.deserializeNBT(nbt.getCompound("Reputation"));
		this.conditionBody = BodyCondition.fromString(nbt.getString("Body"));
		this.conditionSoul = SoulCondition.fromString(nbt.getString("Soul"));
		if(nbt.contains("BodyUUID", 11))
			this.bodyUUID = nbt.getUUID("BodyUUID");
		else
			this.bodyUUID = null;
		if(isBodyDead())
			this.deadTimer = nbt.getInt("DeadTicks");
		else
			this.deadTimer = DEAD_TIME;
	}
	
	/** Returns a float between 1 and 0 representing how much death/respawn delay the player has left. */
	public float timeToRespawnable(){ return (float)Math.max(0, this.deadTimer) / (float)DEAD_TIME; }
	
	public int setDeadTicks(int par1Int)
	{
		int prevTimer = this.deadTimer;
		this.deadTimer = Math.max(0, par1Int);
		if(prevTimer != this.deadTimer)
			markDirty();
		return this.deadTimer;
	}
	
	public boolean setBodyCondition(BodyCondition conditionIn)
	{
		if(conditionIn == this.conditionBody)
			return false;
		
		PlayerChangeConditionEvent event = new PlayerChangeConditionEvent(this.player, this.conditionBody, conditionIn);
		if(MinecraftForge.EVENT_BUS.post(event))
			return false;
		
		this.conditionBody = event.getNewBody();
		setBodyUUID(null);
		
		if(event.getNewBody() != BodyCondition.DEAD)
			setDeadTicks(DEAD_TIME);
		
		markDirty();
		return true;
	}
	
	public boolean setConditionIsDead(UUID bodyID)
	{
		if(setBodyCondition(BodyCondition.DEAD))
		{
			setSoulCondition(SoulCondition.BOUND);
			setBodyUUID(bodyID);
			return true;
		}
		return false;
	}
	
	public boolean setSoulCondition(SoulCondition conditionIn)
	{
		if(conditionIn == this.conditionSoul)
			return false;
		
		PlayerChangeConditionEvent event = new PlayerChangeConditionEvent(this.player, this.conditionSoul, conditionIn);
		if(MinecraftForge.EVENT_BUS.post(event))
			return false;
		
		this.conditionSoul = event.getNewSoul();
		markDirty();
		return true;
	}
	
	/** Returns true if the given player is operating normally */
	public static boolean isPlayerNormalFunction(@Nullable LivingEntity player){ return checkCondition(player, BodyCondition.ALIVE, SoulCondition.ALIVE); }
	
	/** Returns true if the given player's body is dead */
	public static boolean isPlayerBodyDead(@Nullable LivingEntity player){ return checkCondition(player, BodyCondition.DEAD, null); }
	
	/** Returns true if the given player's body is unconscious */
	public static boolean isPlayerBodyAsleep(@Nullable LivingEntity player){ return checkCondition(player, BodyCondition.UNCONSCIOUS, null); }
	
	/** Returns true if the given player's soul is not present in their body for any reason */
	public static boolean isPlayerSoulDetached(@Nullable LivingEntity player){ return !checkCondition(player, null, SoulCondition.ALIVE); }
	
	/** Returns true if the given player's soul is bound to their body, whether inhabiting it or not */
	public static boolean isPlayerSoulBound(@Nullable LivingEntity player){ return !checkCondition(player, null, SoulCondition.ROAMING); }
	
	public static boolean isPlayerBody(@Nullable Player player, Entity body)
	{
		return player != null && PlayerData.forPlayer(player) != null && PlayerData.forPlayer(player).isBody(body);
	}
	
	private static boolean checkCondition(@Nullable LivingEntity playerIn, @Nullable BodyCondition bodyCondition, @Nullable SoulCondition soulCondition)
	{
		if(playerIn != null && playerIn.getType() == EntityType.PLAYER)
		{
			Player player = (Player)playerIn;
			PlayerData data = PlayerData.forPlayer(player);
			return data != null && (bodyCondition == null || data.getBodyCondition() == bodyCondition) && (soulCondition == null || data.getSoulCondition() == soulCondition);
		}
		return false;
	}
	
	public boolean isBodyDead(){ return this.conditionBody == BodyCondition.DEAD; }
	
	public BodyCondition getBodyCondition(){ return this.conditionBody; }
	public SoulCondition getSoulCondition(){ return this.conditionSoul; }
	
	public void setBodyUUID(@Nullable UUID idIn)
	{
		this.bodyUUID = idIn;
		markDirty();
	}
	
	public UUID getBodyUUID(){ return this.bodyUUID; }
	
	public boolean isBody(@Nullable Entity entityIn)
	{
		return getBodyUUID() != null && entityIn != null && entityIn.getUUID().equals(getBodyUUID());
	}
	
	@Nullable
	public Entity getBody(@Nonnull Level world)
	{
		if(world == null || getBodyCondition() == BodyCondition.ALIVE)
			return null;
		
		AABB bounds = player.getBoundingBox().inflate(256D);
		if(getBodyUUID() != null)
		{
			List<Entity> candidates = world.getEntitiesOfClass(Entity.class, bounds, this::isBody);
			if(!candidates.isEmpty())
				return candidates.get(0);
		}
		
		List<AbstractBody> candidates = world.getEntitiesOfClass(AbstractBody.class, bounds, AbstractBody::isPlayer);
		for(AbstractBody body : candidates)
		{
			if(body.isPlayer() && body.getGameProfile().getId().equals(player.getGameProfile().getId()))
			{
				setBodyUUID(body.getUUID());
				return body;
			}
		}
		
		return null;
	}
	
	public void tick(Player player)
	{
		if(isBodyDead())
			setDeadTicks(this.deadTimer-1);
		
		if(this.isDirty)
		{
			this.isDirty = false;
			
			if(!player.getLevel().isClientSide)
				PacketHandler.sendTo((ServerPlayer)player, new PacketSyncPlayerData(player.getUUID(), this));
		}
	}
	
	public void markDirty(){ this.isDirty = true; }
	
	/**
	 * Faction reputation holder class
	 * @author Lying
	 */
	public static class Reputation
	{
		private final Map<String, Integer> reputation = new HashMap<>();
		private String faction = "";
		
		public CompoundTag serializeNBT(CompoundTag compound)
		{
			if(!reputation.isEmpty())
			{
				ListTag list = new ListTag();
				for(String faction : reputation.keySet())
				{
					CompoundTag data = new CompoundTag();
					data.putString("Faction", faction);
					data.putInt("Rep", reputation.get(faction));
					list.add(data);
				}
				compound.put("Reputation", list);
			}
			return compound;
		}
		
		public void deserializeNBT(CompoundTag nbt)
		{
			ListTag list = nbt.getList("Reputation", 10);
			for(int i=0; i<list.size(); i++)
			{
				CompoundTag data = list.getCompound(i);
				reputation.put(data.getString("Faction"), data.getInt("Rep"));
			}
		}
		
		public String factionName(){ return (this.faction == null || this.faction.length() == 0) ? null : this.faction; }
		
		public void setFaction(String par1String){ this.faction = par1String; }
		
		/**
		 * Returns the players reputation with the given faction, or integer min value if they have none recorded.
		 * @param faction
		 * @return
		 */
		public int getReputation(String faction)
		{
			return reputation.containsKey(faction) ? reputation.get(faction) : Integer.MIN_VALUE;
		}
		
		public void setReputation(String faction, int rep)
		{
			if(ConfigVO.GENERAL.verboseLogs())
				VariousOddities.log.info("Set reputation with "+faction+" to "+rep);
			reputation.put(faction, Mth.clamp(rep, -100, 100));
		}
		
		public void addReputation(String faction, int rep)
		{
			int currentRep = getReputation(faction);
			setReputation(faction, currentRep == Integer.MIN_VALUE ? rep : currentRep + rep);
		}
	}
	
	public static enum BodyCondition implements StringRepresentable
	{
		ALIVE,			// Body is functioning normally
		UNCONSCIOUS,	// Body is temporarily inert
		DEAD;			// Body is inert until respawning
		
		public String getSerializedName(){ return name().toLowerCase(); }
		
		public static BodyCondition fromString(String nameIn)
		{
			for(BodyCondition condition : values())
				if(condition.getSerializedName().equalsIgnoreCase(nameIn))
					return condition;
			return ALIVE;
		}
	}
	
	public static enum SoulCondition implements StringRepresentable
	{
		ALIVE(0D),		// Soul is functioning normally
		BOUND(6D),		// Soul is restricted to the vicinity of the body
		ROAMING(-1D);	// Soul is able to roam away from the body
		
		private final double wanderRange;
		
		private SoulCondition(double range)
		{
			this.wanderRange = range;
		}
		
		public String getSerializedName(){ return name().toLowerCase(); }
		
		public static SoulCondition fromString(String nameIn)
		{
			for(SoulCondition condition : values())
				if(condition.getSerializedName().equalsIgnoreCase(nameIn))
					return condition;
			return ALIVE;
		}
		
		/** Returns how far a soul in this condition can be from its associated body */
		public double getWanderRange(){ return this.wanderRange; }
	}
}
