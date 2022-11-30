package com.lying.variousoddities.tileentity;

import java.util.ArrayList;
import java.util.List;

import com.lying.variousoddities.api.world.settlement.EnumRoomFunction;
import com.lying.variousoddities.init.VOBlockEntities;
import com.lying.variousoddities.world.settlement.BoxRoom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TileEntityDraftingTable extends VOTileEntity
{
	public String title = "";
	private BlockPos min, max;
	private EnumRoomFunction function = EnumRoomFunction.NONE;
	private CompoundTag tag = new CompoundTag();
	
	private boolean showBounds = false;
	List<BlockPos> mustInclude = new ArrayList<>();
	
	/**
	 * Bit mask defining which values of this table can be edited.
	 * 1 = Min co-ordinate
	 * 2 = Max co-ordinate
	 * 4 = Function
	 * 8 = Custom name
	 */
	private int lockMask = 0;
	
	public TileEntityDraftingTable(BlockPos pos, BlockState state)
	{
		super(VOBlockEntities.TABLE_DRAFTING.get(), pos, state);
	}
	
	public void load(CompoundTag nbt)
	{
		super.load( nbt);
		this.loadFromNbt(nbt);
	}
	
	public void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);
		this.saveToNbt(compound);
	}
	
	public void loadFromNbt(CompoundTag compound)
	{
		if(compound.contains("CustomName", 8))
			this.title = compound.getString("CustomName");
		
		this.min = NbtUtils.readBlockPos(compound.getCompound("Start"));
		
		this.max = NbtUtils.readBlockPos(compound.getCompound("End"));
		
		this.function = EnumRoomFunction.fromString(compound.getString("Function"));
		
		if(compound.contains("Tag", 10))
			this.tag = compound.getCompound("Tag");
		
		if(compound.contains("Locked", 3))
			this.lockMask = compound.getInt("Locked");
		
		if(compound.contains("MustInclude", 9))
		{
			ListTag includes = compound.getList("MustInclude", 10);
			for(int i=0; i<includes.size(); i++)
				this.mustInclude.add(NbtUtils.readBlockPos(includes.getCompound(i)));
		}
		
		this.showBounds = compound.getBoolean("ShowBounds");
		
		super.setChanged();
	}
	
	public CompoundTag saveToNbt(CompoundTag compound)
	{
		if(hasTitle())
			compound.putString("CustomName", getTitle());
		
		compound.put("Start", NbtUtils.writeBlockPos(this.min()));
		
		compound.put("End", NbtUtils.writeBlockPos(this.max()));
		
		compound.putString("Function", getFunction().getSerializedName());
		
		if(!this.tag.isEmpty())
			compound.put("Tag", this.tag);
		
		if(this.lockMask > 0)
			compound.putInt("Locked", this.lockMask);
		
		if(!mustInclude.isEmpty())
		{
			ListTag list = new ListTag();
			for(BlockPos pos : mustInclude)
				list.add(NbtUtils.writeBlockPos(pos));
			compound.put("MustInclude", list);
		}
		
		compound.putBoolean("ShowBounds", this.showBounds);
		
		return compound;
	}
	
	public boolean showBoundaries(){ return this.showBounds; }
	public void toggleBoundaries(){ this.showBounds = !this.showBounds; setChanged(); }
	
	public int bitMask(){ return this.lockMask; }
	public void setMask(int bitIn)
	{
		this.lockMask = Math.max(0, Math.min(15, bitIn));
		setChanged();
	}
	
	/**
	 * Returns true if the given variable can be modified.<br>
	 * 1 = Min co-ordinate<br>
	 * 2 = Max co-ordinate<br>
	 * 4 = Function<br>
	 * 8 = Custom name<br>
	 */
	public boolean canAlter(int bit)
	{
		return canAlter(bit, this.lockMask);
	}
	
	/**
	 * Returns true if the given variable can be modified.<br>
	 * 1 = Min co-ordinate<br>
	 * 2 = Max co-ordinate<br>
	 * 4 = Function<br>
	 * 8 = Custom name<br>
	 */
	public static boolean canAlter(int bit, int bitMask)
	{
		return !Boolean.valueOf((bitMask & bit) > 0);
	}
	
	public BlockPos min()
	{
		if(min == null)
		{
			if(max != null)
			{
				min = max;
				moveMin(-BoxRoom.MIN_SIZE, -BoxRoom.MIN_SIZE, -BoxRoom.MIN_SIZE);
			}
			else
				min = getBlockPos().offset(-1, -1, -1);
			
			setChanged();
		}
		return min;
	}
	
	public BlockPos max()
	{
		if(max == null)
		{
			if(min != null)
			{
				max = min;
				moveMax(BoxRoom.MIN_SIZE, BoxRoom.MIN_SIZE, BoxRoom.MIN_SIZE);
			}
			else
				max = getBlockPos().offset(1, 1, 1);
			
			setChanged();
		}
		return max;
	}
	
	public BlockPos size()
	{
		int sizeX = this.max().getX() - this.min().getX();
		int sizeY = this.max().getY() - this.min().getY();
		int sizeZ = this.max().getZ() - this.min().getZ();
		return new BlockPos(sizeX, sizeY, sizeZ);
	}
	
	public boolean canApplyToMin(int x, int y, int z)
	{
		BlockPos min = min().offset(x, y, z);
		if(min.getY() < 1 || min.getY() > 255)
			return false;
		
		int lenX = max().getX() - min.getX();
		if(lenX < BoxRoom.MIN_SIZE || lenX > 16)
			return false;
		
		int lenY = max().getY() - min.getY();
		if(lenY < BoxRoom.MIN_SIZE || lenY > 16)
			return false;
		
		int lenZ = max().getZ() - min.getZ();
		if(lenZ < BoxRoom.MIN_SIZE || lenZ > 16)
			return false;
		
		List<BlockPos> included = new ArrayList<>();
		included.addAll(mustInclude);
		included.add(getBlockPos());
		for(BlockPos pos : included)
		{
			if(pos.getX() < min.getX())
				return false;
			
			if(pos.getY() < min.getY())
				return false;
			
			if(pos.getZ() < min.getZ())
				return false;
		}
		
		return true;
	}
	
	public boolean canApplyToMax(int x, int y, int z)
	{
		BlockPos max = max().offset(x, y, z);
		if(max.getY() < 1 || max.getY() > 255)
			return false;
		
		int lenX = max.getX() - min().getX();
		if(lenX < BoxRoom.MIN_SIZE || lenX > 16)
			return false;
		
		int lenY = max.getY() - min().getY();
		if(lenY < BoxRoom.MIN_SIZE || lenY > 16)
			return false;
		
		int lenZ = max.getZ() - min().getZ();
		if(lenZ < BoxRoom.MIN_SIZE || lenZ > 16)
			return false;
		
		List<BlockPos> included = new ArrayList<>();
		included.addAll(mustInclude);
		included.add(getBlockPos());
		for(BlockPos pos : included)
		{
			if(pos.getX() > max.getX())
				return false;
			
			if(pos.getY() > max.getY())
				return false;
			
			if(pos.getZ() > max.getZ())
				return false;
		}
		
		return true;
	}
	
	public void moveMin(int x, int y, int z)
	{
		if(min == null) min();
		
		if(!canAlter(1))
			return;
		
		int maxX = this.max().getX() - BoxRoom.MIN_SIZE;
		int posX = Math.min(maxX, this.min().getX() + x);
		
		int maxY = Math.max(1, Math.min(255, this.max().getY() - BoxRoom.MIN_SIZE));
		int posY = Math.min(maxY, this.min().getY() + y);
		
		int maxZ = this.max().getZ() - BoxRoom.MIN_SIZE;
		int posZ = Math.min(maxZ, this.min().getZ() + z);
		
		this.min = new BlockPos(posX, posY, posZ);
		setChanged();
	}
	
	public void moveMax(int x, int y, int z)
	{
		if(max == null) max();
		
		if(!canAlter(2))
			return;
		
		int minX = this.min().getX() + BoxRoom.MIN_SIZE;
		int posX = Math.max(minX, this.max().getX() + x);
		
		int minY = Math.max(1, Math.min(255, this.min().getY() + BoxRoom.MIN_SIZE));
		int posY = Math.max(minY, this.max().getY() + y);
		
		int minZ = this.min().getZ() + BoxRoom.MIN_SIZE;
		int posZ = Math.max(minZ, this.max().getZ() + z);
		
		this.max = new BlockPos(posX, posY, posZ);
		setChanged();
	}
	
	public EnumRoomFunction getFunction(){ return this.function; }
	
	public void setFunction(EnumRoomFunction function)
	{
		if(function == null)
			function = EnumRoomFunction.NONE;
		
		this.function = function;
		
		setChanged();
	}
	
	public boolean hasTitle(){ return this.title != null && this.title.length() > 0; }
	
	public String getTitle(){ return !hasTitle() ? "" : this.title; }
	
	public void setTitle(String nameIn)
	{
		this.title = nameIn;
		setChanged();
	}
	
	public static BoxRoom getRoomFromNBT(CompoundTag tableData)
	{
		if(!tableData.contains("Locked", 3) || tableData.getInt("Locked") != 15)
			return null;
		
		String name = tableData.contains("CustomName", 8) ? tableData.getString("CustomName") : "";
		BlockPos min = NbtUtils.readBlockPos(tableData.getCompound("Start"));
		BlockPos max = NbtUtils.readBlockPos(tableData.getCompound("End"));
		EnumRoomFunction function = EnumRoomFunction.fromString(tableData.getString("Function"));
		CompoundTag tag = tableData.contains("Tag", 10) ? tableData.getCompound("Tag") : new CompoundTag();
		
		BoxRoom room = new BoxRoom(min, max);
		room.setTitle(Component.literal(name));
		room.setFunction(function);
		room.setTagCompound(tag);
		
		return room;
	}
	
	public void setChanged()
	{
		super.setChanged();
		if(getLevel() != null && !getLevel().isClientSide)
		{
			BlockPos pos = getBlockPos();
			for(Player player : getLevel().players())
				if(player.distanceToSqr(new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)) < (64 * 64))
					((ServerPlayer)player).connection.send(getUpdatePacket());
		}
	}
	
	public void writePacketNBT(CompoundTag compound)
	{
		saveToNbt(compound);
	}
	
	public void readPacketNBT(CompoundTag compound)
	{
		loadFromNbt(compound);
	}
}
