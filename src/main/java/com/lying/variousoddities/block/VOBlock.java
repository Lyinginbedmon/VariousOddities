package com.lying.variousoddities.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class VOBlock extends Block
{
	public static boolean isntSolid(BlockState state, Level reader, BlockPos pos){ return false; }
	
	public VOBlock(BlockBehaviour.Properties properties)
	{
		super(properties);
	}
	
	public VOBlock(String nameIn, Material materialIn)
	{
		this(BlockBehaviour.Properties.of(materialIn));
	}
	
	public VOBlock(String nameIn, Material materialIn, MaterialColor colorIn)
	{
		this(BlockBehaviour.Properties.of(materialIn, colorIn));
	}
}
