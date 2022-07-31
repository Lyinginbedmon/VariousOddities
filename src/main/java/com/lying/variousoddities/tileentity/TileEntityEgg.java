package com.lying.variousoddities.tileentity;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.block.BlockEgg;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityEgg extends VOTileEntity
{
//	private static final Random eggRandom = new Random();
//	private final int fakeID;
	
	protected int ticksToHatching;
	
	public TileEntityEgg(BlockEntityType<?> typeIn, BlockPos pos, BlockState state)
	{
		super(typeIn, pos, state);
//		fakeID = eggRandom.nextInt(1000);
		ticksToHatching = Math.abs(getInitialHatchTime());
	}
	
	public static void clientTick(Level world, BlockPos pos, BlockState state, TileEntityEgg egg) { }
	
	public static void serverTick(Level world, BlockPos pos, BlockState state, TileEntityEgg egg)
	{
		if(!(egg.getBlockState().getBlock() instanceof BlockEgg))
		{
			world.removeBlockEntity(pos);
			VariousOddities.log.warn("Removing orphaned egg tile "+pos);
			return;
		}
		
		int prevBreak = egg.getBreakProgress();
		egg.ticksToHatching -= Math.signum(egg.ticksToHatching);
		
		if(egg.ticksToHatching <= 0)
		{
			if(egg.canHatch())
				egg.hatchEgg();
		}
		else if(egg.ticksToHatching < egg.getHatchingTime())
		{
			if(!egg.canHatch())
				egg.ticksToHatching = egg.getHatchingTime();
			else
			{
				int newBreak = egg.getBreakProgress();
				if(prevBreak != newBreak)
				{
					for(int i=0; i <5; i++)
						egg.spawnBreakParticle();
//					if(!world.isClientSide) world.sendBlockBreakProgress(-fakeID, getPos(), newBreak);
		            world.playSound((Player)null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 0.7F, 0.9F + world.random.nextFloat() * 0.2F);
				}
			}
		}
	}
	
	/** Returns true if the world isn't overpopulated with the type of creature produced by this egg */
	private boolean canHatch()
	{
		if(getLevel().isClientSide) return false;
		
		MobCategory entityClass = getCreatureClass();
		NaturalSpawner.SpawnState manager = ((ServerLevel)getLevel()).getChunkSource().getLastSpawnState();
		return manager.getMobCategoryCounts().getInt(entityClass) < entityClass.getMaxInstancesPerChunk();
	}
	
	private MobCategory getCreatureClass()
	{
		LivingEntity hatchling = getHatchling(getLevel());
		return hatchling.getClassification(true);
	}
	
	public void hatchEgg()
	{
		double coreX = getBlockPos().getX() + 0.5D;
		double coreZ = getBlockPos().getZ() + 0.5D;
		
		LivingEntity baby = getHatchling(getLevel());
		baby.setPos(coreX, getBlockPos().getY() + 0.2D, coreZ);
		
		getLevel().addFreshEntity(baby);
		((BlockEgg<?>)getLevel().getBlockState(getBlockPos()).getBlock()).onHatch(getBlockPos(), getLevel());
	}
	
	public float getHatchingProgress()
	{
		return 1.0F - (float)Math.min(getHatchingTime(), this.ticksToHatching) / (float)getHatchingTime();
	}
	public int getBreakProgress()
	{
		return (int)(getHatchingProgress() * 10F);
	}
	
	private void spawnBreakParticle()
	{
        getLevel().levelEvent(2001, getBlockPos(), Block.getId(getBlockState()));
	}
	
	public int getTicksToHatching(){ return this.ticksToHatching; }
	
	/** Ticks between placing the egg and hatching */
	public abstract int getInitialHatchTime();
	/** How long it takes the hatchling to crack through the egg */
	public abstract int getHatchingTime();
	public abstract LivingEntity getHatchling(Level worldIn);
	
    public void saveAdditional(CompoundTag compound)
    {
        super.saveAdditional(compound);
        compound.putInt("TimeToHatch", this.ticksToHatching);
    }
    
    public void load(CompoundTag compound)
    {
        super.load(compound);
        this.ticksToHatching = compound.getInt("TimeToHatch");
    }
}
