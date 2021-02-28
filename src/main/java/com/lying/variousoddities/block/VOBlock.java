package com.lying.variousoddities.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class VOBlock extends Block
{
	public static boolean isntSolid(BlockState state, IBlockReader reader, BlockPos pos){ return false; }
	
	public VOBlock(String nameIn, AbstractBlock.Properties properties)
	{
		super(properties);
	}
	
	public VOBlock(String nameIn, Material materialIn)
	{
		this(nameIn, AbstractBlock.Properties.create(materialIn));
	}
	
	public VOBlock(String nameIn, Material materialIn, MaterialColor colorIn)
	{
		this(nameIn, AbstractBlock.Properties.create(materialIn, colorIn));
	}
}
