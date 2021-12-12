package com.lying.variousoddities.capabilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.api.event.PlayerChangeConditionEvent;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.entity.AbstractBody;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSyncPlayerData;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PlayerData implements ICapabilitySerializable<CompoundNBT>
{
	@CapabilityInject(PlayerData.class)
	public static final Capability<PlayerData> CAPABILITY = null;
	public static final ResourceLocation IDENTIFIER = new ResourceLocation(Reference.ModInfo.MOD_ID, "player_data");
	public static final int DEAD_TIME = Reference.Values.TICKS_PER_SECOND * 15;
	
	private final LazyOptional<PlayerData> handler;
	
	private PlayerEntity player = null;
	
	public Reputation reputation = new Reputation();
	
	protected BodyCondition conditionBody = BodyCondition.ALIVE;
	protected SoulCondition conditionSoul = SoulCondition.ALIVE;
	protected boolean canPossess = false;
	protected UUID possessingUUID = null;
	
	protected UUID bodyUUID = null;
	protected int deadTimer = DEAD_TIME;
	
	private boolean isDirty = true;
	
	public PlayerData()
	{
		this.handler = LazyOptional.of(() -> this);
	}
	
	public static void register()
	{
		CapabilityManager.INSTANCE.register(PlayerData.class, new PlayerData.Storage(), () -> null);
		if(ConfigVO.GENERAL.verboseLogs())
			VariousOddities.log.info("Registered player data capability");
	}
	
	public LazyOptional<PlayerData> handler(){ return this.handler; }
	
	public static PlayerData forPlayer(PlayerEntity player)
	{
		if(player == null)
			return null;
		
		PlayerData data = null;
		try
		{
			data = player.getCapability(CAPABILITY).orElse(null);
		}
		catch(Exception e){ }
		if(data != null)
			data.player = player;
		return data;
	}
	
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		return CAPABILITY.orEmpty(cap, this.handler);
	}
	
	public CompoundNBT serializeNBT()
	{
		CompoundNBT compound = new CompoundNBT();
		
		compound.put("Reputation", this.reputation.serializeNBT(new CompoundNBT()));
		compound.putString("Body", conditionBody.getString());
		compound.putString("Soul", conditionSoul.getString());
		if(this.bodyUUID != null)
			compound.putUniqueId("BodyUUID", this.bodyUUID);
		if(isBodyDead())
			compound.putInt("DeadTicks", this.deadTimer);
		compound.putBoolean("CanPossess", this.canPossess);
		if(isPossessing())
			compound.putUniqueId("Possessing", this.possessingUUID);
		return compound;
	}
	
	public void deserializeNBT(CompoundNBT nbt)
	{
		if(nbt.contains("Reputation"))
			this.reputation.deserializeNBT(nbt.getCompound("Reputation"));
		this.conditionBody = BodyCondition.fromString(nbt.getString("Body"));
		this.conditionSoul = SoulCondition.fromString(nbt.getString("Soul"));
		if(nbt.contains("BodyUUID", 11))
			this.bodyUUID = nbt.getUniqueId("BodyUUID");
		else
			this.bodyUUID = null;
		if(isBodyDead())
			this.deadTimer = nbt.getInt("DeadTicks");
		else
			this.deadTimer = DEAD_TIME;
		setPossession(nbt.getBoolean("CanPossess"));
		if(nbt.contains("Possessing", 11))
			setPossessing(nbt.getUniqueId("Possessing"));
		else
			setPossessing(null);
	}
	
	public void stopPossessing()
	{
		LivingEntity target = getPossessed();
		if(target != null)
		{
			LivingData data = LivingData.forEntity(target);
			data.setPossessedBy(null);
		}
		
		this.possessingUUID = null;
		this.canPossess = false;
		markDirty();
	}
	public boolean isPossessing(){ return this.possessingUUID != null; }
	public void setPossessing(@Nullable UUID idIn)
	{
		this.possessingUUID = idIn;
		markDirty();
	}
	public UUID getPossessing(){ return this.possessingUUID; }
	public LivingEntity getPossessed()
	{
		List<MobEntity> candidates = player.getEntityWorld().getEntitiesWithinAABB(MobEntity.class, player.getBoundingBox().grow(128D), new Predicate<MobEntity>()
		{
			public boolean apply(MobEntity input){ return input.getUniqueID().equals(getPossessing()); }
		});
		return candidates.isEmpty() ? null : candidates.get(0);
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
	
	public boolean possessionEnabled(){ return this.canPossess; }
	public void setPossession(boolean par1Bool)
	{
		if(par1Bool != this.canPossess)
		{
			this.canPossess = par1Bool;
			markDirty();
		}
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
	
	public static boolean isPlayerBody(@Nullable PlayerEntity player, Entity body)
	{
		return player != null && PlayerData.forPlayer(player) != null && PlayerData.forPlayer(player).isBody(body);
	}
	
	public static boolean isPlayerPossessing(@Nullable PlayerEntity player, @Nullable Entity body)
	{
		return player != null && body != null && PlayerData.forPlayer(player) != null && PlayerData.forPlayer(player).isPossessing() && PlayerData.forPlayer(player).getPossessed() == body;
	}
	
	private static boolean checkCondition(@Nullable LivingEntity playerIn, @Nullable BodyCondition bodyCondition, @Nullable SoulCondition soulCondition)
	{
		if(playerIn != null && playerIn.getType() == EntityType.PLAYER)
		{
			PlayerEntity player = (PlayerEntity)playerIn;
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
		return getBodyUUID() != null && entityIn != null && entityIn.getUniqueID().equals(getBodyUUID());
	}
	
	@Nullable
	public Entity getBody(@Nonnull World world)
	{
		if(world == null || getBodyCondition() == BodyCondition.ALIVE || getBodyUUID() == null)
			return null;
		
		List<Entity> candidates = world.getEntitiesWithinAABB(Entity.class, AbstractBody.ENTIRE_WORLD, this::isBody);
		return candidates.isEmpty() ? null : candidates.get(0);
	}
	
	public void tick(PlayerEntity player)
	{
		if(isBodyDead())
			setDeadTicks(this.deadTimer-1);
		
		if(this.isDirty)
		{
			this.isDirty = false;
			
			if(!player.getEntityWorld().isRemote)
				PacketHandler.sendTo((ServerPlayerEntity)player, new PacketSyncPlayerData(player.getUniqueID(), this));
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
		
		public CompoundNBT serializeNBT(CompoundNBT compound)
		{
			if(!reputation.isEmpty())
			{
				ListNBT list = new ListNBT();
				for(String faction : reputation.keySet())
				{
					CompoundNBT data = new CompoundNBT();
					data.putString("Faction", faction);
					data.putInt("Rep", reputation.get(faction));
					list.add(data);
				}
				compound.put("Reputation", list);
			}
			return compound;
		}
		
		public void deserializeNBT(CompoundNBT nbt)
		{
			ListNBT list = nbt.getList("Reputation", 10);
			for(int i=0; i<list.size(); i++)
			{
				CompoundNBT data = list.getCompound(i);
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
			reputation.put(faction, MathHelper.clamp(rep, -100, 100));
		}
		
		public void addReputation(String faction, int rep)
		{
			int currentRep = getReputation(faction);
			setReputation(faction, currentRep == Integer.MIN_VALUE ? rep : currentRep + rep);
		}
	}
	
	public static enum BodyCondition implements IStringSerializable
	{
		ALIVE,			// Body is functioning normally
		UNCONSCIOUS,	// Body is temporarily inert
		DEAD;			// Body is inert until respawning
		
		public String getString(){ return name().toLowerCase(); }
		
		public static BodyCondition fromString(String nameIn)
		{
			for(BodyCondition condition : values())
				if(condition.getString().equalsIgnoreCase(nameIn))
					return condition;
			return ALIVE;
		}
	}
	
	public static enum SoulCondition implements IStringSerializable
	{
		ALIVE(0D),		// Soul is functioning normally
		BOUND(6D),		// Soul is restricted to the vicinity of the body
		ROAMING(-1D);	// Soul is able to roam away from the body
		
		private final double wanderRange;
		
		private SoulCondition(double range)
		{
			this.wanderRange = range;
		}
		
		public String getString(){ return name().toLowerCase(); }
		
		public static SoulCondition fromString(String nameIn)
		{
			for(SoulCondition condition : values())
				if(condition.getString().equalsIgnoreCase(nameIn))
					return condition;
			return ALIVE;
		}
		
		/** Returns how far a soul in this condition can be from its associated body */
		public double getWanderRange(){ return this.wanderRange; }
	}
	
	public static class Storage implements Capability.IStorage<PlayerData>
	{
		public INBT writeNBT(Capability<PlayerData> capability, PlayerData instance, Direction side)
		{
			return instance.serializeNBT();
		}
		
		public void readNBT(Capability<PlayerData> capability, PlayerData instance, Direction side, INBT nbt)
		{
			if(nbt.getId() == 10)
				instance.deserializeNBT((CompoundNBT)nbt);
		}
	}
}
