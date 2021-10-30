package com.lying.variousoddities.world.savedata;

import java.util.Comparator;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.network.PacketAddScent;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSyncScents;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class ScentsManager extends WorldSavedData
{
	public static final String DATA_NAME = Reference.ModInfo.MOD_ID+"_scents";
	
	/** Maximum number of scent markers to track at any given time per world */
	private static final int MAX_SCENTS = 256;
	/** How often scent markers are placed */
	private static final int REFRESH_RATE = Reference.Values.TICKS_PER_SECOND * 10;

	private static final Comparator<ScentMarker> MARKER_SORT_DURA = new Comparator<ScentMarker>()
			{
				public int compare(ScentMarker o1, ScentMarker o2)
				{
					int dur1 = o1.duration();
					int dur2 = o2.duration();
					return dur1 > dur2 ? 1 : dur1 < dur2 ? -1 : 0;
				}
			};
	private static final Comparator<ScentMarker> MARKER_SORT_DIST = new Comparator<ScentMarker>()
			{
				public int compare(ScentMarker o1, ScentMarker o2)
				{
					Vector3d pos1 = o1.position;
					Vector3d pos2 = o2.position;
					
					double dist1 = o1.world.getClosestPlayer(pos1.x, pos1.y, pos1.z, -1D, EntityPredicates.NOT_SPECTATING).getDistanceSq(pos1);
					double dist2 = o1.world.getClosestPlayer(pos2.x, pos2.y, pos2.z, -1D, EntityPredicates.NOT_SPECTATING).getDistanceSq(pos2);
					return dist1 < dist2 ? 1 : dist1 > dist2 ? -1 : MARKER_SORT_DURA.compare(o1, o2);
				}
			};
	
	protected List<ScentMarker> scents = Lists.newArrayList();
	private World world;
	private int scentTimer = 0;
	
	public ScentsManager()
	{
		super(DATA_NAME);
	}
	
	public ScentsManager(@Nonnull World worldIn)
	{
		this();
		this.world = worldIn;
	}
	
	public void setWorld(@Nonnull World worldIn){ this.world = worldIn; }
	
	public static ScentsManager get(World worldIn)
	{
		if(worldIn.isRemote)
			return VariousOddities.proxy.getScentsManager(worldIn);
		else
		{
			ScentsManager instance = ((ServerWorld)worldIn).getSavedData().getOrCreate(ScentsManager::new, ScentsManager.DATA_NAME);
			instance.setWorld(worldIn);
			return instance;
		}
	}
	
	public void tick()
	{
		if(world == null) return;
		
		scents.forEach((marker) -> { marker.decay(world); });
		
		if(world.isRemote)
			return;
		else if(--scentTimer <= 0)
		{
			scentTimer = REFRESH_RATE;
			if(scents.removeIf(ScentMarker::isDead))
				PacketHandler.sendToAll((ServerWorld)world, new PacketSyncScents(this));
		}
	}
	
	public List<ScentMarker> getScents(){ return this.scents; }
	
	public void addScentMarker(Vector3d position, Vector3d connection, EnumCreatureType type)
	{
		addScentMarker(new ScentMarker(this.world, position, type).addConnection(connection));
	}
	
	public void addScentMarker(ScentMarker newMarker)
	{
		ScentMarker oldMarker = getNearestMarkerOfType(newMarker.position, 4D, newMarker.type);
		if(oldMarker == null)
			scents.add(newMarker);
		else
			scents.add(ScentMarker.merge(oldMarker, newMarker));
		
		if(scents.size() > MAX_SCENTS)
			cleanScents();
		
		if(!world.isRemote)
			PacketHandler.sendToAll((ServerWorld)world, new PacketAddScent(newMarker));
	}
	
	public CompoundNBT write(CompoundNBT compound)
	{
		compound.putInt("Timer", scentTimer);
		ListNBT scentList = new ListNBT();
		scents.forEach((marker) -> { scentList.add(marker.writeToNBT(new CompoundNBT())); });
		compound.put("Scents", scentList);
		return compound;
	}
	
	public void read(CompoundNBT compound)
	{
		this.scentTimer = compound.getInt("Timer");
		scents.clear();
		ListNBT scentList = compound.getList("Scents", 10);
		for(int i=0; i<scentList.size(); i++)
			scents.add(new ScentMarker(this.world, scentList.getCompound(i)));
	}
	
	public void cleanScents()
	{
		scents.removeIf(ScentMarker::isDead);
		scents.sort(MARKER_SORT_DIST);
		while(scents.size() > MAX_SCENTS)
			scents.remove(scents.size() - 1);
	}
	
	/** Returns the nearest marker of the given type within the given maximum range */
	public ScentMarker getNearestMarkerOfType(Vector3d position, double maxDist, EnumCreatureType type)
	{
		ScentMarker nearest = null;
		double minDist = Double.MAX_VALUE;
		for(ScentMarker marker : getScentsOfType(type))
		{
			double dist = marker.position.distanceTo(position);
			if(dist < maxDist && dist < minDist)
			{
				nearest = marker;
				minDist = dist;
			}
		};
		
		return nearest;
	}
	
	/** Returns a list of all scents of the given type in this world */
	public List<ScentMarker> getScentsOfType(EnumCreatureType type)
	{
		List<ScentMarker> typeScents = Lists.newArrayList();
		this.scents.forEach((marker) -> { if(marker.type == type) typeScents.add(marker); });
		return typeScents;
	}
	
	public void syncScentsToPlayer(PlayerEntity player)
	{
		if(world.isRemote) return;
		PacketHandler.sendTo((ServerPlayerEntity)player, new PacketSyncScents(this));
	}

	/**
	 * Updates the scent manager on the server side
	 * @param event
	 */
	@SubscribeEvent
	public static void onWorldUpdateEvent(TickEvent.WorldTickEvent event)
	{
		if(event.isCanceled() || event.side == LogicalSide.CLIENT || event.phase != Phase.START) return;
		ScentsManager manager = get(event.world);
		if(manager != null)
			manager.tick();
	}
	
	/**
	 * Updates the scent manager on the client side
	 * @param event
	 */
	@SubscribeEvent
	public static void onClientUpdateEvent(TickEvent.PlayerTickEvent event)
	{
		if(event.isCanceled() || event.side != LogicalSide.CLIENT || event.phase != Phase.START) return;
		PlayerEntity player = event.player;
		ScentsManager manager = get(player.world);
		if(manager != null)
			manager.tick();
	}
	
	/**
	 * Notifies players of all scents in the world upon login
	 * @param event
	 */
	@SubscribeEvent
	public static void onPlayerLogInEvent(PlayerLoggedInEvent event)
	{
		if(event.isCanceled() || event.getPlayer().getEntityWorld() == null || event.getPlayer().getEntityWorld().isRemote) return;
		PlayerEntity player = event.getPlayer();
		ScentsManager manager = get(player.getEntityWorld());
		if(manager != null)
			manager.syncScentsToPlayer((PlayerEntity)event.getEntity());
	}
	
	/**
	 * Notifies players of all scents in the world upon changing dimension
	 * @param event
	 */
	@SubscribeEvent
	public static void onPlayerChangedWorldEvent(PlayerChangedDimensionEvent event)
	{
		if(event.isCanceled()) return;
		PlayerEntity player = event.getPlayer();
		if(player.getEntityWorld() == null || player.getEntityWorld().isRemote) return;
		
		MinecraftServer server = ((ServerWorld)player.getEntityWorld()).getServer();
		ServerWorld destination = server.getWorld(event.getTo());
		ScentsManager manager = get(destination);
		if(manager != null)
			manager.syncScentsToPlayer((PlayerEntity)event.getEntity());
	}
	
	public static class ScentMarker
	{
		public static final int DEFAULT_DURATION = Reference.Values.TICKS_PER_MINUTE;
		
		private final Vector3d position;
		private final EnumCreatureType type;
		
		private List<Connection> connections = Lists.newArrayList();
		
		private final World world;
		
		public ScentMarker(World worldIn, Vector3d positionIn, EnumCreatureType typeIn)
		{
			this.world = worldIn;
			this.position = positionIn;
			this.type = typeIn;
		}
		public ScentMarker(World worldIn, Vector3d positionIn, EnumCreatureType typeIn, int bonusTime)
		{
			this(worldIn, positionIn, typeIn);
		}
		public ScentMarker(World worldIn, CompoundNBT compound)
		{
			this(worldIn, new Vector3d(compound.getDouble("X"), compound.getDouble("Y"), compound.getDouble("Z")), EnumCreatureType.fromName(compound.getString("Type")));
			
			if(compound.contains("Pings", 9))
			{
				ListNBT pingList = compound.getList("Pings", 10);
				for(int i=0; i<pingList.size(); i++)
					addConnection(new Connection(pingList.getCompound(i)));
			}
		}
		
		public CompoundNBT writeToNBT(CompoundNBT compound)
		{
			compound.putDouble("X", position.x);
			compound.putDouble("Y", position.y);
			compound.putDouble("Z", position.z);
			compound.putString("Type", type.getString());
			
			if(!isDead())
			{
				ListNBT pingList = new ListNBT();
				for(Connection connection : getConnections())
					pingList.add(connection.writeToNBT(new CompoundNBT()));
				compound.put("Pings", pingList);
			}
			
			return compound;
		}
		
		public float alpha()
		{
			return Math.min(1F, (float)duration() / (float)DEFAULT_DURATION);
		}
		
		public void decay(World worldIn)
		{
			int decay = 1;
			BlockPos pos = new BlockPos(position);
			if(worldIn.isRainingAt(pos))
				decay = 2;
			else if(worldIn.getFluidState(pos).isTagged(FluidTags.WATER))
				decay = 4;
			
			for(Connection connection : getConnections())
				connection.decay(decay);
			this.connections.removeIf((connection) -> { return connection.isDead(); });
		}
		
		public void kill(){ this.connections.clear(); }
		
		public boolean isDead(){ return this.connections.isEmpty(); }
		
		public Vector3d position(){ return this.position; }
		
		public List<Connection> getConnections(){ return this.connections; }
		public ScentMarker addConnection(Vector3d connection)
		{
			addConnection(connection, DEFAULT_DURATION);
			return this;
		}
		public void addConnection(Vector3d connection, int duration)
		{
			addConnection(new Connection(connection, duration));
		}
		private void addConnection(Connection connectionIn)
		{
			this.connections.add(connectionIn);
		}
		
		public int duration()
		{
			if(isDead()) return 0;
			
			int duration = 0;
			for(Connection connection : getConnections())
				duration += connection.duration;
			return duration;
		}
		
		public EnumCreatureType type(){ return this.type; }
		
		/** Combines two given scent markers into one stronger marker of the same scent */
		public static ScentMarker merge(ScentMarker markerA, ScentMarker markerB)
		{
			if(markerB.type != markerA.type) return null;
			
			double sumDuration = markerA.duration() + markerB.duration();
			double strengthA = markerA.duration() / sumDuration;
			double strengthB = markerB.duration() / sumDuration;
			double posX = (markerA.position.x * strengthA) + (markerB.position.x * strengthB);
			double posY = (markerA.position.y * strengthA) + (markerB.position.y * strengthB);
			double posZ = (markerA.position.z * strengthA) + (markerB.position.z * strengthB);
			
			ScentMarker markerC = new ScentMarker(markerA.world, new Vector3d(posX, posY, posZ), markerA.type);
			for(Connection connection : markerA.getConnections())
				markerC.addConnection(connection);
			for(Connection connection : markerB.getConnections())
				markerC.addConnection(connection);
			
			markerA.kill();
			markerB.kill();
			
			return markerC;
		}
		
		public static class Connection
		{
			private final Vector3d position;
			private int duration;
			
			public Connection(Vector3d posIn, int durIn)
			{
				this.position = posIn;
				this.duration = durIn;
			}
			public Connection(CompoundNBT compound)
			{
				this.position = new Vector3d(compound.getDouble("X"), compound.getDouble("Y"), compound.getDouble("Z"));
				this.duration = compound.getInt("Duration");
			}
			
			public void decay(int i){ this.duration -= i; }
			public boolean isDead(){ return this.duration <= 0; }
			
			public Vector3d position(){ return this.position; }
			public float alpha(){ return (float)this.duration / (float)ScentMarker.DEFAULT_DURATION; }
			
			public CompoundNBT writeToNBT(CompoundNBT compound)
			{
				compound.putInt("Duration", duration);
				compound.putDouble("X", position.x);
				compound.putDouble("Y", position.y);
				compound.putDouble("Z", position.z);
				return compound;
			}
		}
	}
}
