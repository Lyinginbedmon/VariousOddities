package com.lying.variousoddities.tileentity;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.block.BlockEgg;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityEgg extends VOTileEntity implements ITickableTileEntity
{
//	private static final Random eggRandom = new Random();
//	private final int fakeID;
	
	protected int ticksToHatching;
	
	public TileEntityEgg(BlockEntityType<?> typeIn)
	{
		super(typeIn);
//		fakeID = eggRandom.nextInt(1000);
		ticksToHatching = Math.abs(getInitialHatchTime());
	}
	
	public void tick()
	{
		if(!(this.getBlockState().getBlock() instanceof BlockEgg))
		{
			getLevel().removeBlockEntity(getBlockPos());
			VariousOddities.log.warn("Removing orphaned egg tile "+getBlockPos());
			return;
		}
		
		int prevBreak = getBreakProgress();
		this.ticksToHatching -= Math.signum(this.ticksToHatching);
		
		Level world = getLevel();
		if(this.ticksToHatching <= 0)
		{
			if(!world.isClientSide && canHatch())
				hatchEgg();
		}
		else if(this.ticksToHatching < getHatchingTime())
		{
			if(!canHatch())
				this.ticksToHatching = getHatchingTime();
			else
			{
				int newBreak = getBreakProgress();
				if(prevBreak != newBreak)
				{
					for(int i=0; i <5; i++)
						spawnBreakParticle();
//					if(!world.isClientSide) world.sendBlockBreakProgress(-fakeID, getPos(), newBreak);
		            world.playSound((Player)null, getBlockPos(), SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 0.7F, 0.9F + world.random.nextFloat() * 0.2F);
				}
			}
		}
	}
	
	/** Returns true if the world isn't overpopulated with the type of creature produced by this egg */
	private boolean canHatch()
	{
		if(getLevel().isClientSide) return false;
		
		EntityClassification entityClass = getCreatureClass();
		EntityDensityManager manager = ((ServerLevel)getLevel()).getChunkProvider().func_241101_k_();
		return manager.func_234995_b_().getInt(entityClass) < entityClass.getMaxNumberOfCreature();
	}
	
	private EntityClassification getCreatureClass()
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
		((BlockEgg)getLevel().getBlockState(getBlockPos()).getBlock()).onHatch(getBlockPos(), getLevel());
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
        getLevel().playEvent(2001, getBlockPos(), Block.getId(getBlockState()));
	}
	
	public int getTicksToHatching(){ return this.ticksToHatching; }
	
	/** Ticks between placing the egg and hatching */
	public abstract int getInitialHatchTime();
	/** How long it takes the hatchling to crack through the egg */
	public abstract int getHatchingTime();
	public abstract LivingEntity getHatchling(Level worldIn);
	
    public CompoundTag write(CompoundTag compound)
    {
        super.write(compound);
        compound.putInt("TimeToHatch", this.ticksToHatching);
        return compound;
    }
    
    public void read(BlockState state, CompoundTag compound)
    {
        super.read(state, compound);
        this.ticksToHatching = compound.getInt("TimeToHatch");
    }
}
