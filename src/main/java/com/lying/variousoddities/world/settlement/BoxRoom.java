package com.lying.variousoddities.world.settlement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.lying.variousoddities.api.world.settlement.EnumRoomFunction;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class BoxRoom
{
	public static final int MIN_SIZE = 3;
	public static final int MAX_HEIGHT = 5;
	private static final List<BlockPos> EMPTY = new ArrayList<>();
	
	private List<BlockPos> cachedPoints = new ArrayList<>();
	private BlockPos min = null;
	private BlockPos max = null;
	
	private String customName = "";
	private ITextComponent title = null;
	private EnumRoomFunction function = EnumRoomFunction.NONE;
	private CompoundNBT tag = new CompoundNBT();
	
	public BoxRoom(BlockPos origin)
	{
		add(origin);
	}
	
	public BoxRoom(BlockPos... points)
	{
		for(BlockPos point : points)
			add(point);
	}
	
	public BoxRoom(CompoundNBT compound)
	{
		readFromNBT(compound);
	}
	
	public boolean equals(Object obj)
	{
		if(obj instanceof BoxRoom)
		{
			BoxRoom roomB = (BoxRoom)obj;
			if(!min().equals(roomB.min()))
				return false;
			
			if(!max().equals(roomB.max()))
				return false;
			
			if(hasFunction() != roomB.hasFunction())
				return false;
			else if(hasFunction() && getFunction() != roomB.getFunction())
				return false;
			
			if(hasCustomName() != roomB.hasCustomName())
				return false;
			else if(hasCustomName() && !getName().equals(roomB.getName()))
				return false;
			
			return true;
		}
		return false;
	}
	
	public BlockPos min(){ return this.min; }
	public BlockPos max(){ return this.max; }
	
	public boolean hasTitle(){ return getTitle() != null; }
	
	public ITextComponent getTitle(){ return this.title; }
	
	public boolean hasCustomName()
	{
		return this.customName != null && this.customName.length() > 0;
	}
	
	public void setName(String nameIn)
	{
		this.customName = nameIn;
	}
	
	public String getName()
	{
		return this.customName;
	}
	
	public BoxRoom setFunction(EnumRoomFunction roomIn)
	{
		if(roomIn == null) return this;
		this.function = roomIn;
		return this;
	}
	
	public EnumRoomFunction getFunction()
	{
		return this.function;
	}
	
	public boolean hasFunction()
	{
		return this.function != null && this.function != EnumRoomFunction.NONE;
	}
	
	public boolean hasTagCompound(){ return !this.tag.isEmpty(); }
	
	public CompoundNBT getTagCompound(){ return this.tag; }
	
	public void setTagCompound(CompoundNBT compound){ this.tag = compound; }
	
	public boolean couldBeUsefulSize(List<BlockPos> volume)
	{
		BlockPos min = min();
		if(!volume.contains(min.add(0, 0, MIN_SIZE)))
			return false;
		if(!volume.contains(min.add(0, MIN_SIZE, 0)))
			return false;
		if(!volume.contains(min.add(MIN_SIZE, 0, 0)))
			return false;
		return volume.contains(min().add(MIN_SIZE, MIN_SIZE, MIN_SIZE));
	}
	
	/**
	 * Returns true if the given box is of sufficient scale on all axises to be useful
	 * @param box
	 * @return
	 */
	public boolean isBoxUsefulSize()
	{
		BlockPos size = size();
		return size.getY() >= MIN_SIZE && size.getX() >= MIN_SIZE && size.getZ() >= MIN_SIZE;
	}
	
	public boolean hasSolidFoundation(World worldIn, float threshold)
	{
		int footprint = sizeX() * sizeZ();
		int tallyMin = (int)Math.ceil((double)footprint * threshold);
		int tally = 0;
		
		for(int z=0; z<sizeZ(); z++)
			for(int x=0; x<sizeX(); x++)
			{
				BlockPos point = min.add(x, 0, z);
				BlockPos down = point.down();
				BlockState stateAt = worldIn.getBlockState(point);
				BlockState stateBelow = worldIn.getBlockState(down);
				if(
						// Case 1: Creep with DOWN side at point
//					stateAt.getBlock() instanceof BlockHiveCreep && BlockHiveCreep.shouldConnectTo(down, worldIn) ||
						// Case 2: Solid block at point
					!worldIn.isAirBlock(point) && stateAt.isNormalCube(worldIn, point) ||
						// Case 3: Block with solid UP side below point
					!worldIn.isAirBlock(down) && stateBelow.isSolidSide(worldIn, down, Direction.UP)
					)
						if(++tally >= tallyMin) return true;
			}
		return false;
	}
	
	public AxisAlignedBB getBounds()
	{
		return new AxisAlignedBB
				(
						min.getX(), min.getY(), min.getZ(),
						max.getX()+1D, max.getY()+1D, max.getZ()+1D
				);
	}
	
	public BlockPos getCore()
	{
		int x = (min.getX() + max.getX()) / 2;
		int y = min.getY();
		int z = (min.getZ() + max.getZ()) / 2;
		return new BlockPos(x, y, z);
	}
	
	public void set(BlockPos pos)
	{
		set(pos, pos);
	}
	
	public void set(BlockPos posA, BlockPos posB)
	{
		if(posA == null || posB == null)
			return;
		
		this.min = new BlockPos(Math.min(posA.getX(), posB.getX()), Math.min(posA.getY(), posB.getY()), Math.min(posA.getZ(), posB.getZ()));
		this.max = new BlockPos(Math.max(posA.getX(), posB.getX()), Math.max(posA.getY(), posB.getY()), Math.max(posA.getZ(), posB.getZ()));
	}
	
	public void add(BlockPos pos)
	{
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		
		if(min == null)
			min = pos;
		if(max == null)
			max = pos;
		
		min = new BlockPos(Math.min(x, min.getX()), Math.min(y, min.getY()), Math.min(z, min.getZ()));
		max = new BlockPos(Math.max(x, max.getX()), Math.max(y, max.getY()), Math.max(z, max.getZ()));
	}
	
	public void addAll(Collection<BlockPos> blocks)
	{
		blocks.removeAll(getBlocks());
		for(BlockPos block : blocks)
			add(block);
	}
	
	public boolean contains(BlockPos pos)
	{
		if(min == null || max == null) return false;
		
		AxisAlignedBB bounds = getBounds();
		if(pos.getX() >= bounds.minX && pos.getX() < bounds.maxX)
			if(pos.getY() >= bounds.minY && pos.getY() < bounds.maxY)
				if(pos.getZ() >= bounds.minZ && pos.getZ() < bounds.maxZ)
					return true;
		return false;
	}
	
	public List<BlockPos> getBlocks()
	{
		if(this.cachedPoints.isEmpty())
		{
			List<BlockPos> blocks = new ArrayList<>();
			for(int x=min.getX(); x<=max.getX(); x++)
				for(int y=min.getY(); y<=max.getY(); y++)
					for(int z=min.getZ(); z<=max.getZ(); z++)
						blocks.add(new BlockPos(x, y, z));
			return blocks;
		}
		return this.cachedPoints;
	}
	
	public void drawBox(World worldIn, DyeColor col)
	{
		Block block = Blocks.WHITE_CONCRETE;
		switch(col)
		{
			case BLACK:			block = isBoxUsefulSize() ? Blocks.BLACK_CONCRETE : Blocks.BLACK_STAINED_GLASS;
			case BLUE:			block = isBoxUsefulSize() ? Blocks.BLUE_CONCRETE : Blocks.BLUE_STAINED_GLASS;
			case BROWN:			block = isBoxUsefulSize() ? Blocks.BROWN_CONCRETE : Blocks.BROWN_STAINED_GLASS;
			case CYAN:			block = isBoxUsefulSize() ? Blocks.CYAN_CONCRETE : Blocks.CYAN_STAINED_GLASS;
			case GRAY:			block = isBoxUsefulSize() ? Blocks.GRAY_CONCRETE : Blocks.GRAY_STAINED_GLASS;
			case GREEN:			block = isBoxUsefulSize() ? Blocks.GREEN_CONCRETE : Blocks.GREEN_STAINED_GLASS;
			case LIGHT_BLUE:	block = isBoxUsefulSize() ? Blocks.LIGHT_BLUE_CONCRETE : Blocks.LIGHT_BLUE_STAINED_GLASS;
			case LIGHT_GRAY:	block = isBoxUsefulSize() ? Blocks.LIGHT_GRAY_CONCRETE : Blocks.LIGHT_GRAY_STAINED_GLASS;
			case LIME:			block = isBoxUsefulSize() ? Blocks.LIME_CONCRETE : Blocks.LIME_STAINED_GLASS;
			case MAGENTA:		block = isBoxUsefulSize() ? Blocks.MAGENTA_CONCRETE : Blocks.MAGENTA_STAINED_GLASS;
			case ORANGE:		block = isBoxUsefulSize() ? Blocks.ORANGE_CONCRETE : Blocks.ORANGE_STAINED_GLASS;
			case PINK:			block = isBoxUsefulSize() ? Blocks.PINK_CONCRETE : Blocks.PINK_STAINED_GLASS;
			case PURPLE:		block = isBoxUsefulSize() ? Blocks.PURPLE_CONCRETE : Blocks.PURPLE_STAINED_GLASS;
			case RED:			block = isBoxUsefulSize() ? Blocks.RED_CONCRETE : Blocks.RED_STAINED_GLASS;
			case WHITE:			block = isBoxUsefulSize() ? Blocks.WHITE_CONCRETE : Blocks.WHITE_STAINED_GLASS;
			case YELLOW:		block = isBoxUsefulSize() ? Blocks.YELLOW_CONCRETE : Blocks.YELLOW_STAINED_GLASS;
		}
		
		for(BlockPos lego : getBlocks())
			worldIn.setBlockState(lego.up(25), block.getDefaultState());
	}
	
	public BlockPos size()
	{
		return new BlockPos(sizeX(), sizeY(), sizeZ());
	}
	
	public int sizeX()
	{
		return max.getX() - min.getX() + 1;
	}
	
	public int sizeY()
	{
		return max.getY() - min.getY() + 1;
	}
	
	public int sizeZ()
	{
		return max.getZ() - min.getZ() + 1;
	}
	
	public int volume()
	{
		return size().getX() * size().getY() * size().getZ();
	}
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		compound.put("Min", NBTUtil.writeBlockPos(min));
		compound.put("Max", NBTUtil.writeBlockPos(max));
		if(hasCustomName())
			compound.putString("CustomName", getName());
		if(hasTitle())
			compound.putString("Title", ITextComponent.Serializer.toJson(getTitle()));
		if(hasFunction())
			compound.putString("Function", this.function.name().toLowerCase());
		if(tag != null && !tag.isEmpty())
			compound.put("Tag", tag);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		if(compound.contains("Min", 10))
			add(NBTUtil.readBlockPos(compound.getCompound("Min")));
		
		if(compound.contains("Max", 10))
			add(NBTUtil.readBlockPos(compound.getCompound("Max")));
		
		if(compound.contains("CustomName"))
			setName(compound.getString("CustomName"));
		
		if(compound.contains("Title"))
			this.title = ITextComponent.Serializer.getComponentFromJsonLenient(compound.getString("Title"));
		
		if(compound.contains("Function"))
			setFunction(EnumRoomFunction.fromName(compound.getString("Function")));
		if(compound.contains("Tag"))
			setTagCompound(compound.getCompound("Tag"));
	}
	
	/**
	 * Grows the box along the X axis.
	 * @param legos The known block volume to grow within
	 * @return All new block positions added to the box, or null if the attempt failed
	 */
	public List<BlockPos> growX(List<BlockPos> legos)
	{
		List<BlockPos> containedLegos = new ArrayList<>();
		containedLegos.addAll(legos);
		containedLegos.removeIf(Predicates.not(new Predicate<BlockPos>()
		{
			public boolean apply(BlockPos input)
			{
				int xIn = input.getX();
				if(xIn != min.getX() + sizeX()) return false;
				
				int yIn = input.getY();
				int zIn = input.getZ();
				return zIn >= min.getZ() && zIn <= max.getZ() && yIn >= min.getY() && yIn <= max.getY();
			}
		}));
		
		if(containedLegos.size() == sizeZ() * sizeY())
		{
			addAll(containedLegos);
			return containedLegos;
		}
		else
			return EMPTY;
	}
	
	/**
	 * Grows the box along the Z axis.
	 * @param legos The known block volume to grow within
	 * @return All new block positions added to the box, or null if the attempt failed
	 */
	public List<BlockPos> growZ(List<BlockPos> legos)
	{
		List<BlockPos> containedLegos = new ArrayList<>();
		containedLegos.addAll(legos);
		containedLegos.removeIf(Predicates.not(new Predicate<BlockPos>()
		{
			public boolean apply(BlockPos input)
			{
				int zIn = input.getZ();
				if(zIn != min.getZ() + sizeZ()) return false;
				
				int xIn = input.getX();
				int yIn = input.getY();
				return xIn >= min.getX() && xIn <= max.getX() && yIn >= min.getY() && yIn <= max.getY();
			}
		}));
		
		if(containedLegos.size() == sizeX() * sizeY())
		{
			addAll(containedLegos);
			return containedLegos;
		}
		else
			return EMPTY;
	}
	
	/**
	 * Grows the box along the Y axis.
	 * @param legos The known block volume to grow within
	 * @return All new block positions added to the box, or an empty list if the attempt failed
	 */
	public List<BlockPos> growY(List<BlockPos> legos)
	{
		if(sizeY() >= MAX_HEIGHT) return EMPTY;
		List<BlockPos> containedLegos = new ArrayList<>();
		containedLegos.addAll(legos);
		containedLegos.removeIf(Predicates.not(new Predicate<BlockPos>()
		{
			public boolean apply(BlockPos input)
			{
				int yIn = input.getY();
				if(yIn != min.getY() + sizeY()) return false;
				
				int xIn = input.getX();
				int zIn = input.getZ();
				return xIn >= min.getX() && xIn <= max.getX() && zIn >= min.getZ() && zIn <= max.getZ();
			}
		}));
		
		if(containedLegos.size() == sizeX() * sizeZ())
		{
			addAll(containedLegos);
			return containedLegos;
		}
		else
			return EMPTY;
	}
	
	/**
	 * Grows the box along all axises
	 * @param legos The known block volume to grow within
	 * @return All new block positions added to the box, or null if the attempt failed
	 */
	public List<BlockPos> grow(List<BlockPos> legos)
	{
		List<BlockPos> containedLegos = new ArrayList<>();
		containedLegos.addAll(legos);
		containedLegos.removeIf(Predicates.not(new Predicate<BlockPos>()
		{
			public boolean apply(BlockPos input)
			{
				int xIn = input.getX();
				int yIn = input.getY();
				int zIn = input.getZ();
				
				if(xIn >= min.getX() && xIn <= min.getX() + sizeX())
					if(yIn >= min.getY() && yIn <= min.getY() + sizeY())
						if(zIn >= min.getZ() && zIn <= min.getZ() + sizeZ())
							return true;
				
				return false;
			}
		}));
		
		if(containedLegos.size() == (sizeX() + 1) * (sizeY() + 1) * (sizeZ() + 1))
		{
			addAll(containedLegos);
			return containedLegos;
		}
		else
			return EMPTY;
	}
	
	public boolean canSplit()
	{
		return sizeX()/2 >= MIN_SIZE || sizeZ()/2 >= MIN_SIZE;
	}
	
	/**
	 * Splits this room into two along its longest horizontal axis
	 * @return
	 */
	public Tuple<BoxRoom, BoxRoom> split()
	{
		if(!canSplit()) return null;
		BoxRoom roomA, roomB;
		
		int sizeX = sizeX();
		int sizeZ = sizeZ();
		if(sizeX/2 >= MIN_SIZE && sizeX > sizeZ)
		{
			int xLen = sizeX / 2;
			roomA = new BoxRoom(min(), min.add(xLen, sizeY(), sizeZ));
			roomB = new BoxRoom(min().add(xLen, 0, 0), min().add(sizeX - xLen, sizeY(), sizeZ));
		}
		else
		{
			int zLen = sizeZ / 2;
			roomA = new BoxRoom(min(), min.add(sizeX, sizeY(), sizeZ()));
			roomB = new BoxRoom(min().add(0, 0, zLen), min().add(sizeX, sizeY(), sizeZ - zLen));
		}
		
		return new Tuple<BoxRoom, BoxRoom>(roomA, roomB);
	}
}