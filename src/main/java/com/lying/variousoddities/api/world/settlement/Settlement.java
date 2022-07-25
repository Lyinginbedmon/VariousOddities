package com.lying.variousoddities.api.world.settlement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Predicate;
import com.lying.variousoddities.world.settlement.BoxRoom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Interface class describing a type of settlement to be used by mobs.<br>
 * Classes must be registered with the SettlementManager at startup.<br>
 * New settlements in the world must be added to the SettlementManager to function.
 * @author Lying
 */
public interface Settlement
{
	ResourceLocation typeName();
	
	default boolean equals(Settlement settlementIn)
	{
		if(settlementIn == null)
			return false;
		
		if(!typeName().equals(settlementIn.typeName()))
			return false;
		
		if(hasCustomName() != settlementIn.hasCustomName())
			return false;
		
		if(hasCustomName() && !getCustomName().equals(settlementIn.getCustomName()))
			return false;
		
		if(population() != settlementIn.population())
			return false;
		
		for(LivingEntity resident : getResidents())
			if(!settlementIn.getResidents().contains(resident))
				return false;
		
		if(hasRooms() != settlementIn.hasRooms())
			return false;
		
		if(getRooms().size() != settlementIn.getRooms().size())
			return false;
		
		for(BoxRoom room : getRooms())
			if(!settlementIn.getRooms().contains(room))
				return false;
		
		if(hasNoAI() != settlementIn.hasNoAI())
			return false;
		
		if(isInvulnerable() != settlementIn.isInvulnerable())
			return false;
		
		if(isAbandoned() != settlementIn.isAbandoned())
			return false;
		
		return true;
	}
	
	/**
	 * Binds the given settlement to the given world.<br>
	 * Called by the SettlementManager during instantiation.
	 * @param worldIn
	 */
	void setWorld(Level worldIn);
	
	default boolean hasTitle(){ return getTitle() != null; }
	
	Component getTitle();
	
	void setTitle(Component textComponent);
	
	default int getTitleRange(){ return 5; }
	
	default void setTitleRange(int par1Int){ }
	
	/** Returns true if this settlement has a custom name. */
	default boolean hasCustomName(){ return getCustomName() != null && getCustomName().length() > 0; }
	
	/** Returns the custom name of this settlement, if any. */
	String getCustomName();
	
	/** Sets the custom name of this settlement. */
	void setCustomName(String nameIn);
	
	/**
	 * Updates any internal logic of this settlement.<br>
	 * This may involve the greater management of rooms as well as any other functions.
	 */
	default void update(){ }
	
	/** Returns true if the settlement should be discarded, typically due to being empty. */
	default boolean isAbandoned(){ return population() == 0 && !hasRooms(); }
	
	/** Returns the current number of residents in this settlement. */
	default int population(){ return getResidents().size(); }
	
	List<LivingEntity> getResidents();
	
	/** Disseminates the destruction of this settlement, such as by notifying residents and dismantling rooms */
	void invalidate();
	
	boolean isDirty();
	void setDirty(boolean bool);
	default void markDirty(){ setDirty(true); }
	
	/**
	 * Returns true if the settlement updates should not occur on this settlement.<br>
	 * This typically means no self-management effects, and does not affect room functions.
	 */
	boolean hasNoAI();
	void setNoAI(boolean par1Bool);
	
	/**
	 * Returns true if the settlement should not be removed.<br>
	 * This would typically be due to an invalid marker object or being abandoned.
	 */
	boolean isInvulnerable();
	void setInvulnerable(boolean par1Bool);
	
	/** Returns true if this settlement has a marker object, typically a specific block. */
	boolean hasMarker();
	
	/** Sets the marker object, usually a BlockPos. */
	void setMarker(Object objIn);
	
	/**
	 * Returns true if the marker object for this settlement remains valid.<br>
	 * If this check fails, the settlement is invalidated. 
	 */
	boolean validateMarker();
	
	/**
	 * Returns true if this settlement has any associated structures.<br>
	 * Settlements usually have at least one.
	 */
	default boolean hasRooms(){ return !getRooms().isEmpty(); }
	
	default boolean hasRoomOfType(EnumRoomFunction roomType)
	{
		for(BoxRoom room : getRooms())
			if(room.hasFunction() && room.getFunction() == roomType)
				return true;
		return false;
	}
	
	default List<BoxRoom> getRoomsOfType(EnumRoomFunction function)
	{
		List<BoxRoom> rooms = new ArrayList<>();
		for(BoxRoom room : getRooms())
			if(room.hasFunction() && room.getFunction() == function)
				rooms.add(room);
		return rooms;
	}
	
	default boolean hasRoomAt(BlockPos pos)
	{
		for(BoxRoom room : getRooms())
			if(room.contains(pos))
				return true;
		return false;
	}
	
	default BoxRoom getRoomAt(BlockPos pos)
	{
		for(BoxRoom room : getRooms())
			if(room.contains(pos))
				return room;
		return null;
	}
	
	/** 
	 * Returns true if there is at least one room in this settlement without an assigned function.<br>
	 * Functions are assigned by the settlement, not the SettlementManager.
	 */
	default boolean hasUnassignedRooms()
	{
		for(BoxRoom room : getRooms())
			if(!room.hasFunction())
				return true;
		return false;
	}
	
	/**
	 * Returns a list of all rooms in this settlement without an assigned function.<br>
	 * Functions are assigned by the settlement, not the SettlementManager.
	 */
	default List<BoxRoom> getUnassignedRooms()
	{
		List<BoxRoom> unassigned = new ArrayList<>();
		unassigned.addAll(getRooms());
		unassigned.removeIf(new Predicate<BoxRoom>()
		{
			public boolean apply(BoxRoom input)
			{
				return input.hasFunction();
			}
		});
		return unassigned;
	}
	
	/** Returns a list of all rooms controlled by this settlement. */
	List<BoxRoom> getRooms();
	
	/**
	 * Returns the SettlementRoomBehaviour for the given type of room in this type of settlement.<br>
	 * This controls how rooms of the given type function in this type of settlement.<br>
	 * @param function
	 * @return
	 */
	SettlementRoomBehaviour getBehaviourForRoom(EnumRoomFunction function);
	
	void addRoom(BoxRoom roomIn);
	void addRoom(int index, BoxRoom roomIn);
	default void addRooms(Collection<? extends BoxRoom> roomsIn)
	{
		for(BoxRoom room : roomsIn)
			addRoom(room);
	}
	boolean removeRoom(BoxRoom roomIn);
	default boolean removeRooms(Collection<? extends BoxRoom> roomsIn)
	{
		boolean success = false;
		for(BoxRoom room : roomsIn)
			success = success || removeRoom(room);
		return success;
	}
	void clearRooms();
	default int getIndexFromRoom(BoxRoom roomIn)
	{
		int index = 0;
		for(BoxRoom room : getRooms())
			if(roomIn.equals(room))
				return index;
			else
				index++;
		return -1;
	}
	
	default boolean containsRoom(BoxRoom input)
	{
		return containsRoom(input, getRooms());
	}
	default boolean containsRoom(BoxRoom input, Collection<? extends BoxRoom> rooms)
	{
		for(BoxRoom room : rooms)
			if(room.equals(input))
				return true;
		return false;
	}
	
	/**
	 * Returns true if the given position is within the boundaries of this settlement.<br>
	 * This may be an arbitrary territory region or simply within any of its rooms.
	 */
	boolean containsPosition(BlockPos pos);
	
	/**
	 * Writes internal data of the settlement to NBT.<br>
	 * Does NOT include rooms, custom name, AI, or invulnerability, which are stored by SettlementManager.
	 */
	CompoundTag writeToNBT(CompoundTag compound);
	void readFromNBT(CompoundTag compound);
	
	public static ListTag roomsToList(Collection<? extends BoxRoom> roomsIn)
	{
		ListTag rooms = new ListTag();
		for(BoxRoom room : roomsIn)
			rooms.add(room.writeToNBT(new CompoundTag()));
		return rooms;
	}
	
	public static List<BoxRoom> listToRooms(ListTag roomsIn)
	{
		List<BoxRoom> rooms = new ArrayList<>();
		for(int i=0; i<roomsIn.size(); i++)
			rooms.add(new BoxRoom(roomsIn.getCompound(i)));
		return rooms;
	}
	
	/**
	 * Writes all information necessary client-side to the given NBTTagCompound.<br>
	 * This is usually a simplified version of writeToNBT.
	 */
	public default CompoundTag writeClientData(CompoundTag compound)
	{
		if(hasCustomName())
			compound.putString("CustomName", getCustomName());
		return compound;
	}
	
	/**
	 * Checks if any room is sufficiently loaded and periodically updates those that are.<br>
	 * Returns true if any room is loaded, and hence the settlement itself should update.
	 * @param worldIn
	 * @param randIn
	 * @return
	 */
	@SuppressWarnings("deprecation")
	default boolean updateRooms(ServerLevel worldIn, RandomSource randIn)
	{
		boolean canUpdate = false;
		if(hasRooms())
			for(BoxRoom room : getRooms())
				if(worldIn.isAreaLoaded(room.min(), 1) && worldIn.isAreaLoaded(room.max(), 1))
				{
					SettlementRoomBehaviour roomBehaviour = getBehaviourForRoom(room.getFunction());
					if(room.hasFunction() && roomBehaviour != null && randIn.nextInt(50) == 0)
							getBehaviourForRoom(room.getFunction()).functionCasual(room, worldIn, randIn);
					canUpdate = true;
				}
		return canUpdate;
	}
}