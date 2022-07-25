package com.lying.variousoddities.block;

import java.util.Random;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class BlockVOEmptyDrops extends VOBlock
{
	public BlockVOEmptyDrops(String nameIn, BlockBehaviour.Properties properties)
	{
		super(properties);
	}
	
	public BlockVOEmptyDrops(String nameIn, Material materialIn)
	{
		super(nameIn, materialIn);
	}
	
	public BlockVOEmptyDrops(String nameIn, Material materialIn, MaterialColor mapColorIn)
	{
		super(nameIn,materialIn, mapColorIn);
	}
	
    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(Random random)
    {
        return 0;
    }
    
    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(BlockState state, Random rand, int fortune)
    {
        return Items.AIR;
    }
}
