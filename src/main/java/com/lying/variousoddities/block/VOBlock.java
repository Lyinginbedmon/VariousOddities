package com.lying.variousoddities.block;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class VOBlock extends Block
{
	public static boolean isntSolid(BlockState state, BlockGetter reader, BlockPos pos){ return false; }
	
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
	
	@SuppressWarnings({ "unchecked" })
	@Nullable
	protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> typeA, BlockEntityType<E> typeB, BlockEntityTicker<? super E> ticker)
	{
		return typeB == typeA ? (BlockEntityTicker<A>)ticker : null;
	}
}
