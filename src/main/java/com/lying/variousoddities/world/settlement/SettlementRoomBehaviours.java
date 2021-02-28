package com.lying.variousoddities.world.settlement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.lying.variousoddities.api.world.settlement.Settlement;
import com.lying.variousoddities.api.world.settlement.SettlementRoomBehaviour;
import com.lying.variousoddities.entity.passive.EntityKobold;
import com.lying.variousoddities.init.VOBlocks;
import com.lying.variousoddities.world.savedata.SettlementManager;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class SettlementRoomBehaviours
{
	public static final SettlementRoomBehaviour KOBOLD_NEST = new KoboldRoomBehaviourNest();
	
	public static class KoboldRoomBehaviourNest implements SettlementRoomBehaviour
	{
		private static final int EGGS_PER_GUARD = 3;
		private final Block EGG = VOBlocks.EGG_KOBOLD;
		
		private static final Predicate<EntityKobold> hasEgg = new Predicate<EntityKobold>()
				{
					public boolean apply(EntityKobold input)
					{
						return input.isCarryingEgg();
					}
				};
		private static final Predicate<EntityKobold> isGuard = new Predicate<EntityKobold>()
				{
					public boolean apply(EntityKobold input)
					{
						return input.isHatcheryGuardian();
					}
				};
		
		public static boolean canIncludeBlock(BlockPos pos, IWorld world)
		{
			return !world.canBlockSeeSky(pos);
		}
		
		public void functionCasual(BoxRoom room, ServerWorld worldIn, Random rand)
		{
			/*
			 * Count number of eggs
			 * Count number of nearby guardians
			 * If the ratio of 1 guard per 3 eggs is not met, recruit a nearby kobold as a new guardian
			 */
			AxisAlignedBB bounds = room.getBounds().grow(16D);
			if(SettlementRoomBehaviour.findAllBlock(room, worldIn, EGG).size() > Math.max(1, worldIn.getEntitiesWithinAABB(EntityKobold.class, bounds, isGuard).size() * EGGS_PER_GUARD))
			{
				// Recruit new guard
				for(EntityKobold kobold : SettlementRoomBehaviour.getEntitiesWithin(room, worldIn, EntityKobold.class, Predicates.and(Predicates.not(hasEgg), Predicates.not(isGuard))))
				{
					kobold.setHatcheryGuardian(true);
					break;
				}
			}
		}
		
		public void function(BoxRoom room, ServerWorld worldIn, Random rand)
		{
			/*
			 * Place a new egg
			 */
			List<BlockPos> eggs = SettlementRoomBehaviour.findAllBlock(room, worldIn, EGG);
			if(eggs.isEmpty())
			{
				// If no eggs currently exist, place closest to the core of the nest with a solid neighbouring block
				BlockPos eggPos = getRandomEggSite(room, worldIn, rand);
				if(eggPos != null)
					koboldDepositEgg(room, eggPos, worldIn);
				else
				{
					// If no suitable position was found, presume this nest is uninhabitable
					SettlementManager manager = SettlementManager.get(worldIn);
					Settlement settlement = manager.getSettlementAt(room.getCore());
					if(settlement != null)
					{
						settlement.removeRoom(room);
						return;
					}
				}
			}
			else
			{
				// Find the nearest unoccupied neighbour position to promote egg clustering
				eggs.sort(new Comparator<BlockPos>()
						{
							public int compare(BlockPos o1, BlockPos o2)
							{
								double dist1 = rand.nextInt();
								double dist2 = rand.nextInt();
								return dist1 < dist2 ? -1 : dist1 > dist2 ? 1 : 0;
							}
						});
				for(BlockPos egg : eggs)
				{
					List<BlockPos> openSlots = new ArrayList<>();
					for(int y=-1; y<2; y++)
						for(Direction face : Direction.Plane.HORIZONTAL)
						{
							BlockPos testPos = egg.offset(face).add(0, y, 0);
							if(isPositionValidForEgg(testPos, worldIn))
								openSlots.add(testPos);
						}
					
					if(!openSlots.isEmpty())
					{
						openSlots.sort(new Comparator<BlockPos>()
								{
									public int compare(BlockPos o1, BlockPos o2)
									{
										int eggs1 = countEggsAround(o1, worldIn);
										int eggs2 = countEggsAround(o2, worldIn);
										return eggs1 < eggs2 ? 1 : eggs1 > eggs2 ? -1 : 0;
									}
								});
						koboldDepositEgg(room, openSlots.get(0), worldIn);
						return;
					}
				}
			}
		}
		
		private int countEggsAround(BlockPos pos, World world)
		{
			int tally = 0;
			for(Direction face : Direction.Plane.HORIZONTAL)
				if(world.getBlockState(pos.offset(face)).getBlock() == EGG)
					tally++;
			return tally;
		}
		
		/**
		 * Place an egg at the given position and, if successful, set an egg-carrying kobold to no longer have an egg.
		 */
		private void koboldDepositEgg(BoxRoom room, BlockPos pos, World worldIn)
		{
			if(worldIn.setBlockState(pos, EGG.getDefaultState()))
				for(EntityKobold kobold : SettlementRoomBehaviour.getEntitiesWithin(room, worldIn, EntityKobold.class, hasEgg))
				{
					kobold.setCarryingEgg(false);
					return;
				}
		}
		
		/**
		 * Returns true if the given location is suitable for an egg.<br>
		 * This is the case if the block can be replaced, there is a solid top side below, and it cannot see the sky.
		 */
		public static boolean isPositionValidForEgg(BlockPos pos, World world)
		{
			BlockState state = world.getBlockState(pos);
			if(state.getMaterial().isReplaceable() && Block.hasSolidSideOnTop(world, pos.down()))
				return world.isAreaLoaded(pos, 1) && canIncludeBlock(pos, world);
			return false;
		}
		
		/**
		 * Returns true if the given position has an opaque horizontaly neighbouring block.<br>
		 * Used to ensure new clutches are built along walls and edges
		 */
		public boolean hasSolidNeighbour(BlockPos pos, World world)
		{
			for(Direction face : Direction.Plane.HORIZONTAL)
				if(world.getBlockState(pos.offset(face)).isOpaqueCube(world, pos))
					return true;
			return false;
		}
		
		/**
		 * Attempts to find a new site to start building a clutch.
		 */
		public BlockPos getRandomEggSite(BoxRoom room, World world, Random rand)
		{
			BlockPos eggSite = null;
			BlockPos core = room.getCore();
			int sizeX = room.sizeX();
			int sizeY = room.sizeY();
			int sizeZ = room.sizeZ();
			
			int attempts = 100;
			do
			{
				double moveX = rand.nextInt(sizeX)	- (sizeX / 2);
				double moveY = rand.nextInt(sizeY)	- (sizeY / 2);
				double moveZ = rand.nextInt(sizeZ)	- (sizeZ / 2);
				eggSite = core.add(moveX, moveY, moveZ);
			}
			while(!(isPositionValidForEgg(eggSite, world) && hasSolidNeighbour(eggSite, world)) && --attempts > 0);
			
			return attempts >= 0 ? eggSite : null;
		}
	}
}
