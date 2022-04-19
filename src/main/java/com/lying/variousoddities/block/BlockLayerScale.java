package com.lying.variousoddities.block;

import java.util.Random;

import com.lying.variousoddities.init.VOItems;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class BlockLayerScale extends VOBlock 
{
	protected static final VoxelShape SHAPE = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
	
	public BlockLayerScale(AbstractBlock.Properties properties)
	{
		super(properties.hardnessAndResistance(0.5F).sound(SoundType.PLANT).notSolid().setOpaque(VOBlock::isntSolid));
	}
	
    public Item getItemDropped(BlockState state, Random rand, int fortune)
	{
		return VOItems.SCALE_KOBOLD;
	}
    
    public int quantityDropped(Random random)
    {
        return 1 + random.nextInt(2);
    }
    
    public boolean canSilkHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player)
    {
    	return true;
    }
	
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
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
	
//    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
//    {
//        return super.canPlaceBlockAt(worldIn, pos) && worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP);
//    }
    
    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
//    @SuppressWarnings("deprecation")
//	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
//    {
//        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
//        this.checkAndDropBlock(worldIn, pos, state);
//    }
    
//    protected void checkAndDropBlock(World worldIn, BlockPos pos, BlockState state)
//    {
//        if(!this.canBlockStay(worldIn, pos, state))
//            worldIn.destroyBlock(pos, true);
//    }
    
    public boolean canBlockStay(IWorld worldIn, BlockPos pos, BlockState state)
    {
        return worldIn.getBlockState(pos.down()).isTopSolid(worldIn, pos, null, Direction.UP);
    }
}
