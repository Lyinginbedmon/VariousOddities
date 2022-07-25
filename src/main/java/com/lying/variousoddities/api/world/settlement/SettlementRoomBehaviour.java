package com.lying.variousoddities.api.world.settlement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.lying.variousoddities.world.settlement.BoxRoom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public interface SettlementRoomBehaviour
{
	public default boolean isRoomAppropriate(BoxRoom room, Level worldIn)
	{
		return true;
	}
	
	/**
	 * Called by the hive, generally responsible for requesting supervision
	 */
	public default void functionCasual(BoxRoom room, ServerLevel worldIn, RandomSource rand)
	{
		function(room, worldIn, rand);
	}
	
	/**
	 * Called by mobs  when they complete EntityAIOperateRoom
	 */
	public void function(BoxRoom room, ServerLevel worldIn, RandomSource rand);
	
	public default void dismantle(BoxRoom room, ServerLevel worldIn){ }
	
	public static List<BlockPos> findAllBlock(BoxRoom room, Level worldIn, Block block)
	{
		return findAllBlock(room, worldIn, block, Collections.emptyList());
	}
	
	public static List<BlockPos> findAllBlock(BoxRoom room, Level worldIn, Block block, Collection<Property<?>> properties)
	{
		List<BlockPos> blocks = new ArrayList<>();
		for(int x=0; x < room.sizeX(); x++)
			for(int y=0; y < room.sizeY(); y++)
				for(int z=0; z < room.sizeZ(); z++)
				{
					BlockPos pos = room.min().offset(x, y, z);
					BlockState state = worldIn.getBlockState(pos);
					if(state.getBlock() == block && (properties.isEmpty() || state.getProperties().equals(properties)))
						blocks.add(pos);
				}
		return blocks;
	}
	
	public static List<LivingEntity> getEntitiesWithin(BoxRoom room, Level worldIn)
	{
		return getEntitiesWithin(room, worldIn, LivingEntity.class);
	}
	
	public static <T extends LivingEntity> List<T> getEntitiesWithin(BoxRoom room, Level worldIn, Class<T> classIn)
	{
		return getEntitiesWithin(room, worldIn, classIn, Predicates.alwaysTrue());
	}
	
	public static <T extends LivingEntity> List<T> getEntitiesWithin(BoxRoom room, Level worldIn, Class<T> classIn, Predicate<T> predicateIn)
	{
		return worldIn.getEntitiesOfClass(classIn, room.getBounds(), predicateIn);
	}
}