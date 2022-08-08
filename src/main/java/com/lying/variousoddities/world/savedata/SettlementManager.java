package com.lying.variousoddities.world.savedata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.api.event.SettlementRegistryEvent;
import com.lying.variousoddities.api.world.settlement.Settlement;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.world.settlement.BoxRoom;
import com.lying.variousoddities.world.settlement.SettlementDummy;
import com.lying.variousoddities.world.settlement.SettlementGoblin;
import com.lying.variousoddities.world.settlement.SettlementKobold;
import com.lying.variousoddities.world.settlement.SettlementManagerServer;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

public abstract class SettlementManager extends SavedData
{
	public static final String DATA_NAME = Reference.ModInfo.MOD_ID+"_settlements";
	
	/** Global map of settlement type names to their corresponding classes */
	private static final Map<ResourceLocation, Class<? extends Settlement>> CLASS_MAP = new HashMap<>();
	
	public Map<Integer, Settlement> settlements = new HashMap<>();
	protected Level world;
	
	public SettlementManager(){ }
	
	public void setWorld(Level worldIn)
	{
		this.world = worldIn;
		if(worldIn != null && !isEmpty())
			notifyObservers();
	}
	
	public DimensionType getDim()
	{
		return this.world == null ? null : this.world.dimensionType();
	}
	
	/**
	 * Transmits client-side data of the given settlement to all players, largely for displaying rooms client-side.<br>
	 * Also used to remove settlements from the client when passed a valid index and a null settlement.
	 */
	public abstract void notifyObservers(int index, @Nullable Settlement settlement);
	
	/** Notifies all players of all settlements */
	public abstract void notifyObservers();
	
	/** Notifies the given player of all settlements in this world */
	public abstract void notifyObserver(Player par1Player);
	
	/** Notifies the given player of the given settlement */
	public abstract void notifyObserver(Player par1Player, int index, @Nullable Settlement settlement);
	
	/** Register a settlement using its typeName() */
	public static boolean registerSettlement(Settlement settlement)
	{
		return registerSettlement(settlement.typeName(), settlement.getClass());
	}
	
	/**
	 * Register a settlement class with a given type name.<br>
	 * Returns false if a class with that name already exists.
	 */
	public static boolean registerSettlement(ResourceLocation nameIn, Class<? extends Settlement> classIn)
	{
		if(CLASS_MAP.containsKey(nameIn))
		{
			VariousOddities.log.error("Attempted to register duplicate settlement with id "+nameIn);
			return false;
		}
		CLASS_MAP.put(nameIn, classIn);
		return true;
	}
	
	/**
	 * Returns a collection of all available settlement types
	 */
	public static Collection<ResourceLocation> getSettlementTypes()
	{
		return CLASS_MAP.keySet();
	}
	
	public static Class<? extends Settlement> getSettlementByType(ResourceLocation nameIn)
	{
		if(CLASS_MAP.containsKey(nameIn))
			return CLASS_MAP.get(nameIn);
		return null;
	}
	
	public static ResourceLocation getTypeBySettlement(Settlement settlementIn){ return getTypeBySettlement(settlementIn.getClass()); }
	public static ResourceLocation getTypeBySettlement(Class<? extends Settlement> classIn)
	{
		for(ResourceLocation name : CLASS_MAP.keySet())
			if(CLASS_MAP.get(name) == classIn)
				return name;
		return null;
	}
	
	/** Creates a new settlement, with no rooms, from the given type name and storage NBT. */
	@SuppressWarnings("deprecation")
	public static Settlement createSettlementFromNBT(ResourceLocation nameIn, CompoundTag compound)
	{
		if(CLASS_MAP.containsKey(nameIn))
		{
			Settlement settlement = null;
			Class<? extends Settlement> settlementClass = CLASS_MAP.get(nameIn);
			try
			{
				settlement = CLASS_MAP.get(nameIn).newInstance();
			}
			catch (Exception e)
			{
				VariousOddities.log.error("Couldn't instantiate new settlement from class "+settlementClass.getSimpleName()+" (type name: "+nameIn+")");
			}
			if(settlement != null)
				settlement.readFromNBT(compound);
			return settlement;
		}
		return null;
	}
	
	public CompoundTag save(CompoundTag compound)
	{
		ListTag settlementList = new ListTag();
		for(int index : settlements.keySet())
			settlementList.add(settlementToNBT(index, getSettlementByIndex(index), new CompoundTag()));
		compound.put("Settlements", settlementList);
		return compound;
	}
	
	public void read(CompoundTag compound)
	{
		this.settlements.clear();
		ListTag settlements = compound.getList("Settlements", 10);
		for(int i=0; i<settlements.size(); i++)
		{
			CompoundTag data = settlements.getCompound(i);
			int index = data.getInt("Index");
			Settlement settlement = NBTToSettlement(data);
			if(settlement != null && index >= 0)
				add(index, settlement);
		}
	}
	
	public abstract SettlementManager fromNBT(CompoundTag compound);
	
	public static SettlementManager get(Level worldIn)
	{
		if(worldIn.isClientSide)
			return VariousOddities.proxy.getSettlementManager(worldIn);
		else
		{
			SettlementManagerServer instance = ((ServerLevel)worldIn).getDataStorage().get(SettlementManager::fromNBT, SettlementManager.DATA_NAME);
			instance.setWorld(worldIn);
			return instance;
		}
	}
	
	/** Returns the settlement with the lowest index and custom title at the given position, if any */
	public Settlement getTitleSettlementAt(BlockPos pos)
	{
		for(Settlement settlement : this.settlements.values())
			if(settlement.hasRooms() && settlement.getTitleRange() >= 0)
				for(BoxRoom room : settlement.getRooms())
					if(room.getBounds().inflate(settlement.getTitleRange()).contains(new Vec3(pos.getX(), pos.getY(), pos.getZ())))
						return settlement;
		return null;
	}
	
	public boolean isEmpty(){ return this.settlements.isEmpty(); }
	
	/**
	 * Stores the client-relevant settlement information, including rooms and index, to NBT data.<br>
	 * Used when transmitting the settlement to clients.
	 */
	public static CompoundTag settlementToClientNBT(int index, Settlement settlement, CompoundTag compound)
	{
		compound.putInt("Index", index);
		if(settlement != null)
			settlementToClientNBT(settlement, compound);
		return compound;
	}
	
	/**
	 * Stores the full settlement information, including rooms and index, to NBT data.<br>
	 * Used when storing the settlement in world data.
	 */
	public static CompoundTag settlementToNBT(int index, Settlement settlement, CompoundTag compound)
	{
		compound.putInt("Index", index);
		settlementToNBT(settlement, compound);
		return compound;
	}
	
	public static CompoundTag settlementToClientNBT(Settlement settlement, CompoundTag compound)
	{
		compound.putString("Type", getTypeBySettlement(settlement).toString());
		if(settlement.hasCustomName())
			compound.putString("CustomName", settlement.getCustomName());
		
		CompoundTag display = new CompoundTag();
			if(settlement.hasTitle())
				display.putString("Title", Component.Serializer.toJson(settlement.getTitle()));
			display.putInt("Range", settlement.getTitleRange());
		compound.put("Display", display);
		
		if(settlement.hasRooms())
			compound.put("Rooms", Settlement.roomsToList(settlement.getRooms()));
		compound.put("Tag", settlement.writeClientData(new CompoundTag()));
		return compound;
	}
	
	/**
	 * Stores the full settlement information, including rooms and index, to NBT data.<br>
	 * Used when storing the settlement in world data.
	 */
	public static CompoundTag settlementToNBT(Settlement settlement, CompoundTag compound)
	{
		compound.putString("Type", getTypeBySettlement(settlement).toString());
		if(settlement.hasNoAI())
			compound.putBoolean("NoAI", settlement.hasNoAI());
		if(settlement.isInvulnerable())
			compound.putBoolean("Invulnerable", settlement.isInvulnerable());
		if(settlement.hasCustomName())
			compound.putString("CustomName", settlement.getCustomName());
		
		CompoundTag display = new CompoundTag();
			if(settlement.hasTitle())
				display.putString("Title", Component.Serializer.toJson(settlement.getTitle()));
			display.putInt("Range", settlement.getTitleRange());
		compound.put("Display", display);
		
		compound.put("Tag", settlement.writeToNBT(new CompoundTag()));
		if(settlement.hasRooms())
			compound.put("Rooms", Settlement.roomsToList(settlement.getRooms()));
		return compound;
	}
	
	/** Recreates a settlement from NBT data, including rooms */
	public static Settlement NBTToSettlement(CompoundTag data)
	{
		if(!data.contains("Type", 8))
			return null;
		ResourceLocation type = new ResourceLocation(data.getString("Type"));
		CompoundTag storage = data.contains("Tag", 10) ? data.getCompound("Tag") : new CompoundTag();
		Settlement settlement = createSettlementFromNBT(type, storage);
		if(settlement != null)
		{
			if(data.contains("NoAI"))
				settlement.setNoAI(data.getBoolean("NoAI"));
			if(data.contains("Invulnerable"))
				settlement.setInvulnerable(data.getBoolean("Invulnerable"));
			if(data.contains("CustomName", 8))
				settlement.setCustomName(data.getString("CustomName"));
			
			CompoundTag display = data.getCompound("Display");
			if(display.contains("Title"))
				settlement.setTitle(Component.Serializer.fromJsonLenient(display.getString("Title")));
			settlement.setTitleRange(display.getInt("Range"));
			
			if(data.contains("Rooms"))
				settlement.addRooms(Settlement.listToRooms(data.getList("Rooms", 10)));
		}
		return settlement;
	}
	
	public Collection<Settlement> getSettlements()
	{
		return settlements.values();
	}
	
	public Settlement getSettlementAt(Vec3 pos)
	{
		return getSettlementAt(new BlockPos(pos.x, pos.y, pos.z));
	}
	
	public Settlement getSettlementAt(BlockPos pos)
	{
		for(int index : settlements.keySet())
		{
			Settlement settlement = getSettlementByIndex(index);
			if(settlement.containsPosition(pos))
				return settlement;
		}
		return null;
	}
	
	public int getSettlementIndexAt(BlockPos pos)
	{
		for(int index : settlements.keySet())
		{
			Settlement settlement = getSettlementByIndex(index);
			if(settlement.containsPosition(pos))
				return index;
		}
		return -1;
	}
	
	public Settlement getSettlementByIndex(int index)
	{
		if(settlements.containsKey(index))
		{
			Settlement settlement = settlements.get(index);
			settlement.setWorld(world);
			return settlement;
		}
		return null;
	}
	
	public Settlement getSettlementByName(String nameIn)
	{
		for(Settlement settlement : settlements.values())
			if(settlement.hasCustomName() && settlement.getCustomName().equalsIgnoreCase(nameIn))
				return settlement;
		return null;
	}
	
	public int getIndexBySettlement(Settlement settlement)
	{
		for(int index : settlements.keySet())
			if(settlements.get(index).equals(settlement))
				return index;
		
		return -1;
	}
	
	public List<Settlement> getSettlementsOfType(ResourceLocation nameIn)
	{
		List<Settlement> ofType = new ArrayList<>();
		for(Settlement settlement : settlements.values())
			if(getTypeBySettlement(settlement).equals(nameIn))
				ofType.add(settlement);
		return ofType;
	}
	
	public int add(Settlement settlementIn)
	{
		int newID = 0;
		if(!settlements.isEmpty())
			for(int index : settlements.keySet())
				if(index >= newID)
					newID = index + 1;
		
		add(newID, settlementIn);
		return newID;
	}
	
	public boolean add(int index, Settlement settlementIn)
	{
		if(settlementIn == null || index < 0)
			return false;
		
		settlements.put(index, settlementIn);
		notifyObservers(index, settlementIn);
		markDirty();
		return true;
	}
	
	public boolean remove(int index)
	{
		settlements.remove(index);
		notifyObservers(index, null);
		markDirty();
		return true;
	}
	
	public boolean removeAll(Collection<? extends Integer> indices)
	{
		boolean success = false;
		for(Integer index : indices)
			success = remove(index) || success;
		return success;
	}
	
	public boolean remove(Settlement settlementIn)
	{
		int index = getIndexBySettlement(settlementIn);
		if(index >= 0)
		{
			if(world != null && !world.isClientSide)
				settlementIn.invalidate();
			
			settlements.remove(index);
			notifyObservers(index, null);
			markDirty();
			return true;
		}
		return false;
	}
	
	public void clear()
	{
		this.settlements.clear();
		markDirty();
	}
	
	static
	{
		registerSettlement(SettlementDummy.TYPE_NAME, SettlementDummy.class);
		registerSettlement(SettlementKobold.TYPE_NAME, SettlementKobold.class);
		registerSettlement(SettlementGoblin.TYPE_NAME, SettlementGoblin.class);
		MinecraftForge.EVENT_BUS.post(new SettlementRegistryEvent());
	}
}
