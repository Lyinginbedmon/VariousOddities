package com.lying.variousoddities.block;

import javax.annotation.Nullable;

import com.lying.variousoddities.entity.passive.EntityMarimo;
import com.lying.variousoddities.init.VOEntities;
import com.mojang.math.Vector3d;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;

public class BlockMoss extends BlockVOEmptyDrops
{
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    
	public BlockMoss(BlockBehaviour.Properties properties)
	{
		super("moss_block", properties.sound(SoundType.SLIME_BLOCK).strength(0.5F, 3.0F).requiresCorrectToolForDrops());//.harvestLevel(1).harvestTool(ToolType.SHOVEL));
        this.registerDefaultState(this.defaultBlockState().setValue(NORTH, Boolean.valueOf(false)).setValue(EAST, Boolean.valueOf(false)).setValue(SOUTH, Boolean.valueOf(false)).setValue(WEST, Boolean.valueOf(false)).setValue(UP, Boolean.valueOf(false)).setValue(DOWN, Boolean.valueOf(false)));
	}
    
    public boolean isTransparent(BlockState stateIn){ return true; }
	
    protected boolean canSilkHarvest()
    {
        return false;
    }
	
    /**
     * Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated
     */
    public void destroy(Level worldIn, BlockPos pos, BlockState state)
    {
        super.destroy(worldIn, pos, state);
        if(!worldIn.isClientSide)
        {
        	boolean spawnMarimo = false;
        	for(Direction face : Direction.values())
        	{
        		if(worldIn.getBlockState(pos.relative(face)).getMaterial() == Material.WATER)
        		{
        			worldIn.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
        			spawnMarimo = true;
        			break;
        		}
        	}
        	
        	if(spawnMarimo)
        	{
	        	Vector3d posCentre = new Vector3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
	        	for(int i=0; i<(worldIn.getRandom().nextInt(3) + 1); i++)
	        	{
	        		EntityMarimo marimo = (EntityMarimo)VOEntities.MARIMO.get().spawn((ServerLevel)worldIn, null, null, pos, MobSpawnType.NATURAL, false, false);
	        		double xOff = (worldIn.getRandom().nextDouble() - 0.5D) * 0.25D;
	        		double yOff = (worldIn.getRandom().nextDouble() - 0.5D) * 0.25D;
	        		double zOff = (worldIn.getRandom().nextDouble() - 0.5D) * 0.25D;
	        		marimo.setPos(posCentre.x + xOff, posCentre.y + yOff, posCentre.z + zOff);
	        		marimo.setYRot(worldIn.getRandom().nextFloat() * 360F);
	        		
	        		double motionX = (worldIn.getRandom().nextDouble() - 0.5D) * 0.1D;
	        		double motionY = (worldIn.getRandom().nextDouble() - 0.5D) * 0.1D;
	        		double motionZ = (worldIn.getRandom().nextDouble() - 0.5D) * 0.1D;
	        		marimo.setDeltaMovement(motionX, motionY, motionZ);
	        		
	        		worldIn.addFreshEntity(marimo);
	        	}
        	}
        }
    }
    
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
    	builder.add(NORTH, EAST, WEST, SOUTH, UP, DOWN);
    }
    
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
    	BlockState state = defaultBlockState();
    	for(Direction face : Direction.values())
    		state = updatePostPlacement(state, face, null, context.getLevel(), context.getClickedPos(), null);
    	
    	return state;
    }
    
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, Level worldIn, BlockPos currentPos, BlockPos facingPos)
    {
    	boolean isWater = isNeighbourWater(worldIn, currentPos, facing);
    	switch(facing)
    	{
			case DOWN:	return stateIn.setValue(DOWN, isWater);
			case UP:	return stateIn.setValue(UP, isWater);
			case EAST:	return stateIn.setValue(EAST, isWater);
			case NORTH:	return stateIn.setValue(NORTH, isWater);
			case SOUTH:	return stateIn.setValue(SOUTH, isWater);
			case WEST:	return stateIn.setValue(WEST, isWater);
			default:	return stateIn;
    	}
    }
    
    public boolean isNeighbourWater(Level worldIn, BlockPos pos, Direction face)
    {
    	return worldIn.getBlockState(pos.relative(face)).getMaterial() == Material.WATER;
    }
}
