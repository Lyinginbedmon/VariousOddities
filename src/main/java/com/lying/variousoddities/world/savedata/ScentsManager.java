package com.lying.variousoddities.world.savedata;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	private static final double MERGE_DIST = 3D;

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
					Vector3d pos1 = o1.getPosition();
					Vector3d pos2 = o2.getPosition();
					
					double dist1 = o1.world.getClosestPlayer(pos1.x, pos1.y, pos1.z, -1D, EntityPredicates.NOT_SPECTATING).getDistanceSq(pos1);
					double dist2 = o1.world.getClosestPlayer(pos2.x, pos2.y, pos2.z, -1D, EntityPredicates.NOT_SPECTATING).getDistanceSq(pos2);
					return dist1 < dist2 ? 1 : dist1 > dist2 ? -1 : MARKER_SORT_DURA.compare(o1, o2);
				}
			};
	
	protected Map<EnumCreatureType, List<ScentMarker>> scentMap = new HashMap<>();
	
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
		
		for(EnumCreatureType type : scentMap.keySet())
			scentMap.get(type).forEach((marker) -> { marker.decay(world); });
		
		if(world.isRemote)
			return;
		else if(--scentTimer <= 0)
		{
			scentTimer = REFRESH_RATE;
			boolean shouldSync = false;
			for(EnumCreatureType type : scentMap.keySet())
				if(scentMap.get(type).removeIf(ScentMarker::isDead))
					shouldSync = true;
			
			if(shouldSync)
				PacketHandler.sendToAll((ServerWorld)world, new PacketSyncScents(this));
		}
	}
	
	public List<ScentMarker> getAllScents()
	{
		List<ScentMarker> scents = Lists.newArrayList();
		for(EnumCreatureType type : scentMap.keySet())
			scents.addAll(scentMap.get(type));
		return scents;
	}
	
	public void addScentMarker(Vector3d position, Vector3d connection, EnumCreatureType type)
	{
		addScentMarker(new ScentMarker(this.world, type).addConnection(position, connection));
	}
	
	public void addScentMarker(ScentMarker newMarker)
	{
		List<ScentMarker> scentsOfType = scentMap.getOrDefault(newMarker.type, Lists.newArrayList());
		
		ScentMarker markerAdded = new ScentMarker(this.world, newMarker.writeToNBT(new CompoundNBT()));
		int scents = scentsOfType.size();
		boolean merged = false;
		for(int i=0; i<scents; i++)
		{
			ScentMarker neighbour = scentsOfType.get(i);
			if(neighbour.getPosition().distanceTo(newMarker.getPosition()) < MERGE_DIST)
			{
				neighbour = ScentMarker.merge(neighbour, newMarker);
				scentsOfType.set(i, neighbour);
				merged = true;
				break;
			}
		}
		
		if(!merged)
			scentsOfType.add(newMarker);
		
		scentMap.put(newMarker.type, scentsOfType);
		
		if(isOverflowing())
			cleanScents();
		
		if(!world.isRemote)
			PacketHandler.sendToAll((ServerWorld)world, new PacketAddScent(markerAdded));
	}
	
	private boolean isOverflowing()
	{
		return totalScents() > MAX_SCENTS;
	}
	
	public int totalScents()
	{
		int tally = 0;
		for(EnumCreatureType type : scentMap.keySet())
			tally += scentMap.get(type).size();
		return tally;
	}
	
	public CompoundNBT write(CompoundNBT compound)
	{
		compound.putInt("Timer", scentTimer);
		ListNBT scentList = new ListNBT();
		for(EnumCreatureType type : scentMap.keySet())
		{
			CompoundNBT typeData = new CompoundNBT();
			typeData.putString("Type", type.getString());
			ListNBT typeMarkers = new ListNBT();
			scentMap.get(type).forEach((marker) -> { typeMarkers.add(marker.writeToNBT(new CompoundNBT())); });
			typeData.put("Markers", typeMarkers);
			
			scentList.add(typeData);
		}
		compound.put("Scents", scentList);
		return compound;
	}
	
	public void read(CompoundNBT compound)
	{
		this.scentTimer = compound.getInt("Timer");
		scentMap.clear();
		ListNBT scentList = compound.getList("Scents", 10);
		for(int i=0; i<scentList.size(); i++)
		{
			CompoundNBT typeData = scentList.getCompound(i);
			EnumCreatureType type = EnumCreatureType.fromName(typeData.getString("Type"));
			ListNBT typeMarkers = typeData.getList("Markers", 10);
			List<ScentMarker> markers = Lists.newArrayList();
			for(int j=0; j<typeMarkers.size(); j++)
				markers.add(new ScentMarker(this.world, typeMarkers.getCompound(j)));
			scentMap.put(type, markers);
		}
	}
	
	public void cleanScents()
	{
		for(EnumCreatureType type : scentMap.keySet())
		{
			List<ScentMarker> scents = scentMap.get(type);
			if(scents.removeIf(ScentMarker::isDead))
				scentMap.put(type, scents);
		}
		
		int total = totalScents();
		if(total > MAX_SCENTS)
		{
			List<ScentMarker> scents = getAllScents();
			scents.sort(MARKER_SORT_DIST);
			while(total > MAX_SCENTS)
			{
				ScentMarker removed = scents.remove(scents.size() - 1);
				
				List<ScentMarker> ofType = scentMap.get(removed.type);
				ofType.remove(removed);
				scentMap.put(removed.type, ofType);
			}
		}
	}
	
	/** Returns the nearest marker of the given type within the given maximum range */
	public ScentMarker getNearestMarkerOfType(Vector3d position, double maxDist, EnumCreatureType type)
	{
		ScentMarker nearest = null;
		double minDist = Double.MAX_VALUE;
		for(ScentMarker marker : getScentsOfType(type))
		{
			if(marker.isDead()) continue;
			double dist = marker.getPosition().distanceTo(position);
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
		return scentMap.getOrDefault(type, Lists.newArrayList());
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
		private static final double SPEED = 0.01D;
		
		private Vector3d positionPrev;
		private Vector3d originalPos;
		
		private final EnumCreatureType type;
		
		private List<Connection> connections = Lists.newArrayList();
		
		private final World world;
		
		public ScentMarker(World worldIn, EnumCreatureType typeIn)
		{
			this.world = worldIn;
			this.type = typeIn;
		}
		public ScentMarker(World worldIn, Vector3d positionIn, EnumCreatureType typeIn, int bonusTime)
		{
			this(worldIn, typeIn);
			this.positionPrev = positionIn;
		}
		public ScentMarker(World worldIn, CompoundNBT compound)
		{
			this(worldIn, EnumCreatureType.fromName(compound.getString("Type")));
			this.positionPrev = new Vector3d(compound.getDouble("X"), compound.getDouble("Y"), compound.getDouble("Z"));
			
			if(compound.contains("Pings", 9))
			{
				ListNBT pingList = compound.getList("Pings", 10);
				for(int i=0; i<pingList.size(); i++)
					addConnection(new Connection(pingList.getCompound(i)));
			}
		}
		
		public CompoundNBT writeToNBT(CompoundNBT compound)
		{
			compound.putString("Type", type.getString());
			
			compound.putDouble("X", getPosition().x);
			compound.putDouble("Y", getPosition().y);
			compound.putDouble("Z", getPosition().z);
			
			compound.putDouble("OriginX", origin().x);
			compound.putDouble("OriginY", origin().y);
			compound.putDouble("OriginZ", origin().z);
			
			if(!isDead())
			{
				ListNBT pingList = new ListNBT();
				for(Connection connection : getConnections())
					pingList.add(connection.writeToNBT(new CompoundNBT()));
				compound.put("Pings", pingList);
			}
			
			return compound;
		}
		
		public int color()
		{
			switch(this.type)
			{
				case ABERRATION:	return 7733503;
				case ANIMAL:		return 12960347;
				case CONSTRUCT:		return 12156416;
				case DRAGON:		return 6881280;
				case ELEMENTAL:		return 16730880;
				case FEY:			return 7793611;
				case GIANT:			return 11679540;
				case MAGICAL_BEAST:			return 12960511;
				case MONSTROUS_HUMANOID:	return 13331561;
				case OOZE:			return 3858591;
				case OUTSIDER:		return 5976726;
				case PLANT:			return 27136;
				case UNDEAD:		return 1975070;
				case VERMIN:		return 5707798;
				case HUMANOID:
				default:
					return 13344911;
			}
		}
		
		public float alpha()
		{
			return Math.min(1F, (float)duration() / (float)DEFAULT_DURATION);
		}
		
		public void decay(World worldIn)
		{
			if(connections.size() > 1)
			{
				Vector3d position = position();
				if(position.distanceTo(getPosition()) > 0)
					this.positionPrev = nextPosition();
			}
			
			int decay = 1;
			BlockPos pos = new BlockPos(positionPrev);
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
		
		/** Returns the position of this marker in the last tick */
		public Vector3d getPosition(float partialTicks)
		{
			if(positionPrev == null)
				positionPrev = position();
			
			Vector3d offset = nextPosition().subtract(positionPrev);
			double len = offset.length() * partialTicks;
			return this.positionPrev.add(offset.normalize().mul(len, len, len));
		}
		
		public Vector3d getPosition(){ return getPosition(1F); }
		
		private Vector3d nextPosition()
		{
			Vector3d truePos = position();
			Vector3d offset = truePos.subtract(positionPrev);
			if(offset.length() == 0)
				return truePos;
			
			double len = Math.min(SPEED, offset.length());
			return this.positionPrev.add(offset.normalize().mul(len, len, len));
		}
		
		/** Returns the average end position of all connections.<br>The "true" position of this marker. */
		private Vector3d position()
		{
			double x = 0, y = 0, z = 0;
			for(Connection connection : this.connections)
			{
				x += connection.positionA.x;
				y += connection.positionA.y;
				z += connection.positionA.z;
			}
			x /= connections.size();
			y /= connections.size();
			z /= connections.size();
			
			Vector3d pos = new Vector3d(x, y, z);
			if(originalPos == null)
				originalPos = pos;
			return pos;
		}
		
		public Vector3d origin(){ return originalPos; }
		
		public List<Connection> getConnections(){ return this.connections; }
		public ScentMarker addConnection(Vector3d positionA, Vector3d positionB)
		{
			addConnection(positionA, positionB, DEFAULT_DURATION);
			return this;
		}
		public void addConnection(Vector3d positionA, Vector3d positionB, int duration)
		{
			addConnection(new Connection(positionA, positionB, duration));
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
			markerA.connections.addAll(markerB.connections);
			markerB.kill();
			
			return markerA;
		}
		
		public static class Connection
		{
			private Vector3d positionA, positionB;
			private int duration;
			
			public Connection(Vector3d posIn, Vector3d fromIn, int durIn)
			{
				this.positionA = posIn;
				this.positionB = fromIn;
				this.duration = durIn;
			}
			public Connection(CompoundNBT compound)
			{
				this.positionA = new Vector3d(compound.getDouble("X1"), compound.getDouble("Y1"), compound.getDouble("Z1"));
				this.positionB = new Vector3d(compound.getDouble("X2"), compound.getDouble("Y2"), compound.getDouble("Z2"));
				this.duration = compound.getInt("Duration");
			}
			
			public void decay(int i){ this.duration -= i; }
			public boolean isDead(){ return this.duration <= 0; }
			
			public Vector3d position(){ return this.positionB; }
			public float alpha(){ return (float)this.duration / (float)ScentMarker.DEFAULT_DURATION; }
			
			public CompoundNBT writeToNBT(CompoundNBT compound)
			{
				compound.putInt("Duration", duration);
				
				compound.putDouble("X1", positionA.x);
				compound.putDouble("Y1", positionA.y);
				compound.putDouble("Z1", positionA.z);
				
				compound.putDouble("X2", positionB.x);
				compound.putDouble("Y2", positionB.y);
				compound.putDouble("Z2", positionB.z);
				return compound;
			}
		}
	}
}
