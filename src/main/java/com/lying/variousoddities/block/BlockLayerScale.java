package com.lying.variousoddities.block;

import java.util.Random;

import com.lying.variousoddities.init.VOItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockLayerScale extends VOBlock 
{
	protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
	
	public BlockLayerScale(BlockBehaviour.Properties properties)
	{
		super(properties.strength(0.5F).sound(SoundType.GRASS).noCollission().noOcclusion().isViewBlocking(VOBlock::isntSolid));
	}
	
    public Item getItemDropped(BlockState state, Random rand, int fortune)
	{
		return VOItems.SCALE_KOBOLD;
	}
    
    public int quantityDropped(Random random)
    {
        return 1 + random.nextInt(2);
    }
    
    public boolean canSilkHarvest(Level world, BlockPos pos, BlockState state, Player player)
    {
    	return true;
    }
	
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
    {
        return SHAPE;
    }
    
//    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos)
//    {
//    	return true;
//    }
    
    public boolean isOpaqueCube(BlockState state){ return false; }
    public boolean isFullCube(BlockState state){ return false; }
//    public BlockRenderLayer getBlockLayer(){ return BlockRenderLayer.CUTOUT; }
    
    public PushReaction getPushReaction(BlockState state)
    {
    	return PushReaction.DESTROY;
    }
	
//    public boolean canPlaceBlockAt(Level worldIn, BlockPos pos)
//    {
//        return super.canPlaceBlockAt(worldIn, pos) && worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP);
//    }
    
    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
//    @SuppressWarnings("deprecation")
//	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
//    {
//        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
//        this.checkAndDropBlock(worldIn, pos, state);
//    }
    
//    protected void checkAndDropBlock(Level worldIn, BlockPos pos, BlockState state)
//    {
//        if(!this.canBlockStay(worldIn, pos, state))
//            worldIn.destroyBlock(pos, true);
//    }
    
    public boolean canBlockStay(Level worldIn, BlockPos pos, BlockState state)
    {
        return worldIn.getBlockState(pos.below()).isFaceSturdy(worldIn, pos, Direction.UP);
    }
}
