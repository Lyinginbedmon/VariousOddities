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
	private EnumRoomFunction function = EnumRoomFunction.NONE;
	public String customName = null;
	private CompoundNBT tag = new CompoundNBT();
	
	private BoxRoom room = null;
	
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
		this.lockMask = compound.getInt("Locked");
		if(compound.contains("Room"))
			this.room = new BoxRoom(compound.getCompound("Room"));
		
		this.customName = compound.getString("CustomName").replace(" ", "_");
		
		this.function = EnumRoomFunction.valueOf(compound.getString("Function"));
		
		ListNBT includes = compound.getList("MustInclude", 10);
		for(int i=0; i<includes.size(); i++)
			this.mustInclude.add(NBTUtil.readBlockPos(includes.getCompound(i)));
	}
	
	public CompoundNBT saveToNbt(CompoundNBT compound)
	{
		compound.putInt("Locked", this.lockMask);
		if(this.room != null)
			compound.put("Room", getRoom().writeToNBT(new CompoundNBT()));
		
		compound.putString("CustomName", getCustomName());
		
		compound.putString("Function", getFunction().name().toLowerCase());
		
		ListNBT list = new ListNBT();
		for(BlockPos pos : mustInclude)
			list.add(NBTUtil.writeBlockPos(pos));
		compound.put("MustInclude", list);
		return compound;
	}
	
	public int bitMask(){ return this.lockMask; }
	public void setMask(int bitIn){ this.lockMask = Math.max(0, Math.min(15, bitIn)); }
	
	public boolean canAlter(int bit)
	{
		return !Boolean.valueOf((lockMask & bit) > 0);
	}
	
	public void initialise()
	{
		double offset = (double)BoxRoom.MIN_SIZE * 0.5D;
		Vector3d posMid = new Vector3d(getPos().getX() + 0.5D, getPos().getY() + 0.5D, getPos().getZ() + 0.5D).subtract(offset, offset, offset);
		BlockPos min = new BlockPos(posMid.x, posMid.y, posMid.z);
		BlockPos max = min.add(BoxRoom.MIN_SIZE, BoxRoom.MIN_SIZE, BoxRoom.MIN_SIZE);
		this.room = new BoxRoom(min, max);
	}
	
	public BlockPos min()
	{
		if(room == null)
			initialise();
		return room.min();
	}
	
	public BlockPos max()
	{
		if(room == null)
			initialise();
		return room.max();
	}
	
	public BlockPos size()
	{
		if(room == null)
			initialise();
		return room.size();
	}
	
	public boolean areBoundsValid(BlockPos posA, BlockPos posB)
	{
		int xMin = Math.min(posA.getX(), posB.getX());
		int xMax = Math.max(posA.getX(), posB.getX());
		
		int yMin = Math.min(posA.getY(), posB.getY());
		int yMax = Math.max(posA.getY(), posB.getY());
		if(yMin < 1 || yMax > 255)
			return false;
		
		int zMin = Math.min(posA.getZ(), posB.getZ());
		int zMax = Math.max(posA.getZ(), posB.getZ());
		
		int lenX = xMax - xMin + 1;
		int lenY = yMax - yMin + 1;
		int lenZ = zMax - zMin + 1;
		if(lenX < BoxRoom.MIN_SIZE || lenX > 16)
			return false;
		if(lenY < BoxRoom.MIN_SIZE || lenY > 16)
			return false;
		if(lenZ < BoxRoom.MIN_SIZE || lenZ > 16)
			return false;
		
		List<BlockPos> included = new ArrayList<>();
		included.addAll(mustInclude);
		included.add(getPos());
		for(BlockPos pos : included)
		{
			if(pos.getX() < xMin || pos.getX() > xMax)
				return false;
			
			if(pos.getY() < yMin || pos.getY() > yMax)
				return false;
			
			if(pos.getZ() < zMin || pos.getZ() > zMax)
				return false;
		}
		
		return true;
	}
	
	public void setBounds(BlockPos posA, BlockPos posB)
	{
		room.set(posA, posB);
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
	
	public String getCustomName(){ return !hasCustomName() ? "" : this.customName.replace(" ", "_"); }
	
	public void setCustomName(String nameIn)
	{
		this.customName = nameIn.replace(" ", "_");
		markDirty();
	}
	
	public BoxRoom getRoom()
	{
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
			for(PlayerEntity player : getWorld().getPlayers())
				if(player.getDistanceSq(new Vector3d(getPos().getX() + 0.5D, getPos().getY() + 0.5D, getPos().getZ() + 0.5D)) < (64 * 64))
					((ServerPlayerEntity)player).connection.sendPacket(getUpdatePacket());
	}
}
