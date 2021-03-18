package com.lying.variousoddities.tileentity;

import java.util.ArrayList;
import java.util.List;

import com.lying.variousoddities.api.world.settlement.EnumRoomFunction;
import com.lying.variousoddities.init.VOTileEntities;
import com.lying.variousoddities.world.settlement.BoxRoom;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class TileEntityDraftingTable extends VOTileEntity
{
	public String customName = "";
	private BlockPos min, max;
	private EnumRoomFunction function = EnumRoomFunction.NONE;
	private CompoundNBT tag = new CompoundNBT();
	
	List<BlockPos> mustInclude = new ArrayList<>();
	
	/**
	 * Bit mask defining which values of this table can be edited.
	 * 1 = Min co-ordinate
	 * 2 = Max co-ordinate
	 * 4 = Function
	 * 8 = Custom name
	 */
	private int lockMask = 0;
	
	public TileEntityDraftingTable()
	{
		super(VOTileEntities.TABLE_DRAFTING);
	}
	
	public void read(BlockState state, CompoundNBT nbt)
	{
		super.read(state, nbt);
		this.loadFromNbt(nbt);
	}
	
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);
		return this.saveToNbt(compound);
	}
	
	public void loadFromNbt(CompoundNBT compound)
	{
		if(compound.contains("CustomName", 8))
			this.customName = compound.getString("CustomName").replace(" ", "_");
		
		if(compound.contains("Start", 10))
			this.min = NBTUtil.readBlockPos(compound.getCompound("Start"));
		
		if(compound.contains("End", 10))
			this.max = NBTUtil.readBlockPos(compound.getCompound("End"));
		
		if(compound.contains("Function", 8))
			this.function = EnumRoomFunction.fromString(compound.getString("Function"));
		
		if(compound.contains("Tag", 10))
			this.tag = compound.getCompound("Tag");
		
		if(compound.contains("Locked", 3))
			this.lockMask = compound.getInt("Locked");
		
		if(compound.contains("MustInclude", 9))
		{
			ListNBT includes = compound.getList("MustInclude", 10);
			for(int i=0; i<includes.size(); i++)
				this.mustInclude.add(NBTUtil.readBlockPos(includes.getCompound(i)));
		}
		
		markDirty();
	}
	
	public CompoundNBT saveToNbt(CompoundNBT compound)
	{
		if(!canAlter(8) && hasCustomName())
			compound.putString("CustomName", getCustomName());
		
		if(!canAlter(1))
			compound.put("Start", NBTUtil.writeBlockPos(this.min));
		
		if(!canAlter(2))
			compound.put("End", NBTUtil.writeBlockPos(this.max));
		
		if(!canAlter(4))
			compound.putString("Function", getFunction().getString());
		
		if(!this.tag.isEmpty())
			compound.put("Tag", this.tag);
		
		if(this.lockMask > 0)
			compound.putInt("Locked", this.lockMask);
		
		if(!mustInclude.isEmpty())
		{
			ListNBT list = new ListNBT();
			for(BlockPos pos : mustInclude)
				list.add(NBTUtil.writeBlockPos(pos));
			compound.put("MustInclude", list);
		}
		return compound;
	}
	
	public int bitMask(){ return this.lockMask; }
	public void setMask(int bitIn){ this.lockMask = Math.max(0, Math.min(15, bitIn)); }
	
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
				min = getPos().add(-1, -1, -1);
			
			markDirty();
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
				max = getPos().add(1, 1, 1);
			
			markDirty();
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
		BlockPos min = min().add(x, y, z);
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
		included.add(getPos());
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
		BlockPos max = max().add(x, y, z);
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
		included.add(getPos());
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
		markDirty();
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
		markDirty();
	}
	
	public EnumRoomFunction getFunction(){ return this.function; }
	
	public void setFunction(EnumRoomFunction function)
	{
		if(function == null)
			function = EnumRoomFunction.NONE;
		
		this.function = function;
		
		markDirty();
	}
	
	public boolean hasCustomName(){ return this.customName != null && this.customName.length() > 0; }
	
	public String getCustomName(){ return !hasCustomName() ? "" : this.customName; }
	
	public void setCustomName(String nameIn)
	{
		this.customName = nameIn.replace(" ", "_");
		markDirty();
	}
	
	public BoxRoom getRoom()
	{
		BoxRoom room = new BoxRoom(this.min, this.max);
		
		room.setFunction(getFunction());
		
		if(hasCustomName())
			room.setName(getCustomName());
		
		if(!tag.isEmpty())
			room.setTagCompound(tag);
		
		return room;
	}
	
	public void markDirty()
	{
		super.markDirty();
		if(getWorld() != null && !getWorld().isRemote)
		{
			System.out.println("Syncing clients with drafting table");
			for(PlayerEntity player : getWorld().getPlayers())
				if(player.getDistanceSq(new Vector3d(getPos().getX() + 0.5D, getPos().getY() + 0.5D, getPos().getZ() + 0.5D)) < (64 * 64))
					((ServerPlayerEntity)player).connection.sendPacket(getUpdatePacket());
		}
	}
}
