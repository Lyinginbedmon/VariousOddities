package com.lying.variousoddities.api.world.settlement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.lying.variousoddities.world.settlement.BoxRoom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.state.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public interface SettlementRoomBehaviour
{
	public default boolean isRoomAppropriate(BoxRoom room, World worldIn)
	{
		return true;
	}
	
	/**
	 * Called by the hive, generally responsible for requesting supervision
	 */
	public default void functionCasual(BoxRoom room, ServerWorld worldIn, Random rand)
	{
		function(room, worldIn, rand);
	}
	
	/**
	 * Called by mobs  when they complete EntityAIOperateRoom
	 */
	public void function(BoxRoom room, ServerWorld worldIn, Random rand);
	
	public default void dismantle(BoxRoom room, ServerWorld worldIn){ }
	
	public static List<BlockPos> findAllBlock(BoxRoom room, World worldIn, Block block)
	{
		return findAllBlock(room, worldIn, block, Collections.emptyList());
	}
	
	public static List<BlockPos> findAllBlock(BoxRoom room, World worldIn, Block block, Collection<Property<?>> properties)
	{
		List<BlockPos> blocks = new ArrayList<>();
		for(int x=0; x < room.sizeX(); x++)
			for(int y=0; y < room.sizeY(); y++)
				for(int z=0; z < room.sizeZ(); z++)
				{
					BlockPos pos = room.min().add(x, y, z);
					BlockState state = worldIn.getBlockState(pos);
					if(state.getBlock() == block && (properties.isEmpty() || state.getProperties().equals(properties)))
						blocks.add(pos);
				}
		return blocks;
	}
	
	public static List<LivingEntity> getEntitiesWithin(BoxRoom room, World worldIn)
	{
		return getEntitiesWithin(room, worldIn, LivingEntity.class);
	}
	
	public static <T extends LivingEntity> List<T> getEntitiesWithin(BoxRoom room, World worldIn, Class<T> classIn)
	{
		return getEntitiesWithin(room, worldIn, classIn, Predicates.alwaysTrue());
	}
	
	public static <T extends LivingEntity> List<T> getEntitiesWithin(BoxRoom room, World worldIn, Class<T> classIn, Predicate<T> predicateIn)
	{
		return worldIn.getEntitiesWithinAABB(classIn, room.getBounds(), predicateIn);
	}
}