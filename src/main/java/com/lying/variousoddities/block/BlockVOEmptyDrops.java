package com.lying.variousoddities.block;

import java.util.Random;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class BlockVOEmptyDrops extends VOBlock
{
	public BlockVOEmptyDrops(String nameIn, AbstractBlock.Properties properties)
	{
		super(nameIn, properties);
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
