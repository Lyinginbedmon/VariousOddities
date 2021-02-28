package com.lying.variousoddities.block;

import javax.annotation.Nullable;

import com.lying.variousoddities.entity.passive.EntityMarimo;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;

public class BlockMoss extends BlockVOEmptyDrops
{
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    
	public BlockMoss(AbstractBlock.Properties properties)
	{
		super("moss_block", properties.sound(SoundType.SLIME).hardnessAndResistance(0.5F, 3.0F).harvestLevel(1).harvestTool(ToolType.SHOVEL));
        this.setDefaultState(this.stateContainer.getBaseState().with(NORTH, Boolean.valueOf(false)).with(EAST, Boolean.valueOf(false)).with(SOUTH, Boolean.valueOf(false)).with(WEST, Boolean.valueOf(false)).with(UP, Boolean.valueOf(false)).with(DOWN, Boolean.valueOf(false)));
	}
    
    public boolean isTransparent(BlockState stateIn){ return true; }
	
    protected boolean canSilkHarvest()
    {
        return false;
    }
	
    /**
     * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
     */
    public void onPlayerDestroy(IWorld worldIn, BlockPos pos, BlockState state)
    {
        super.onPlayerDestroy(worldIn, pos, state);
        if(!worldIn.isRemote())
        {
        	boolean spawnMarimo = false;
        	for(Direction face : Direction.values())
        	{
        		if(worldIn.getBlockState(pos.offset(face)).getMaterial() == Material.WATER)
        		{
        			worldIn.setBlockState(pos, Blocks.WATER.getDefaultState(), 3);
        			spawnMarimo = true;
        			break;
        		}
        	}
        	
        	if(spawnMarimo)
        	{
	        	Vector3d posCentre = new Vector3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
	        	for(int i=0; i<(worldIn.getRandom().nextInt(3) + 1); i++)
	        	{
	        		EntityMarimo marimo = (EntityMarimo)VOEntities.MARIMO.spawn((ServerWorld)worldIn, null, null, pos, SpawnReason.NATURAL, false, false);
	        		double xOff = (worldIn.getRandom().nextDouble() - 0.5D) * 0.25D;
	        		double yOff = (worldIn.getRandom().nextDouble() - 0.5D) * 0.25D;
	        		double zOff = (worldIn.getRandom().nextDouble() - 0.5D) * 0.25D;
	        		marimo.setPosition(posCentre.x + xOff, posCentre.y + yOff, posCentre.z + zOff);
	        		marimo.rotationYaw = worldIn.getRandom().nextFloat() * 360F;
	        		
	        		double motionX = (worldIn.getRandom().nextDouble() - 0.5D) * 0.1D;
	        		double motionY = (worldIn.getRandom().nextDouble() - 0.5D) * 0.1D;
	        		double motionZ = (worldIn.getRandom().nextDouble() - 0.5D) * 0.1D;
	        		marimo.setMotion(motionX, motionY, motionZ);
	        		
	        		worldIn.addEntity(marimo);
	        	}
        	}
        }
    }
    
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
    	builder.add(NORTH, EAST, WEST, SOUTH, UP, DOWN);
    }
    
    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
    	BlockState state = getDefaultState();
    	for(Direction face : Direction.values())
    		state = updatePostPlacement(state, face, null, context.getWorld(), context.getPos(), null);
    	
    	return state;
    }
    
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
    {
    	boolean isWater = isNeighbourWater(worldIn, currentPos, facing);
    	switch(facing)
    	{
			case DOWN:	return stateIn.with(DOWN, isWater);
			case UP:	return stateIn.with(UP, isWater);
			case EAST:	return stateIn.with(EAST, isWater);
			case NORTH:	return stateIn.with(NORTH, isWater);
			case SOUTH:	return stateIn.with(SOUTH, isWater);
			case WEST:	return stateIn.with(WEST, isWater);
			default:	return stateIn;
    	}
    }
    
    public boolean isNeighbourWater(IWorld worldIn, BlockPos pos, Direction face)
    {
    	return worldIn.getBlockState(pos.offset(face)).getMaterial() == Material.WATER;
    }
}
