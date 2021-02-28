package com.lying.variousoddities.tileentity;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.block.BlockEgg;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner.EntityDensityManager;

public abstract class TileEntityEgg extends VOTileEntity implements ITickableTileEntity
{
//	private static final Random eggRandom = new Random();
//	private final int fakeID;
	
	protected int ticksToHatching;
	
	public TileEntityEgg(TileEntityType<?> typeIn)
	{
		super(typeIn);
//		fakeID = eggRandom.nextInt(1000);
		ticksToHatching = Math.abs(getInitialHatchTime());
	}
	
	public void tick()
	{
		if(!(getWorld().getBlockState(getPos()).getBlock() instanceof BlockEgg))
		{
			getWorld().removeTileEntity(getPos());
			VariousOddities.log.warn("Removing orphaned egg tile "+getPos());
			return;
		}
		
		int prevBreak = getBreakProgress();
		this.ticksToHatching -= Math.signum(this.ticksToHatching);
		
		World world = getWorld();
		if(this.ticksToHatching <= 0)
		{
			if(!world.isRemote && canHatch())
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
//					if(!world.isRemote) world.sendBlockBreakProgress(-fakeID, getPos(), newBreak);
		            world.playSound((PlayerEntity)null, getPos(), SoundEvents.ENTITY_TURTLE_EGG_HATCH, SoundCategory.BLOCKS, 0.7F, 0.9F + world.rand.nextFloat() * 0.2F);
				}
			}
		}
	}
	
	/** Returns true if the world isn't overpopulated with the type of creature produced by this egg */
	private boolean canHatch()
	{
		if(getWorld().isRemote) return false;
		
		EntityClassification entityClass = getCreatureClass();
		EntityDensityManager manager = ((ServerWorld)getWorld()).getChunkProvider().func_241101_k_();
		return manager.func_234995_b_().getInt(entityClass) < entityClass.getMaxNumberOfCreature();
	}
	
	private EntityClassification getCreatureClass()
	{
		LivingEntity hatchling = getHatchling(getWorld());
		return hatchling.getClassification(true);
	}
	
	public void hatchEgg()
	{
		double coreX = getPos().getX() + 0.5D;
		double coreZ = getPos().getZ() + 0.5D;
		
		LivingEntity baby = getHatchling(getWorld());
		baby.setPosition(coreX, getPos().getY() + 0.2D, coreZ);
		
		getWorld().addEntity(baby);
		((BlockEgg)getWorld().getBlockState(getPos()).getBlock()).onHatch(getPos(), getWorld());
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
        getWorld().playEvent(2001, pos, Block.getStateId(getBlockState()));
	}
	
	public int getTicksToHatching(){ return this.ticksToHatching; }
	
	/** Ticks between placing the egg and hatching */
	public abstract int getInitialHatchTime();
	/** How long it takes the hatchling to crack through the egg */
	public abstract int getHatchingTime();
	public abstract LivingEntity getHatchling(World worldIn);
	
    public CompoundNBT write(CompoundNBT compound)
    {
        super.write(compound);
        compound.putInt("TimeToHatch", this.ticksToHatching);
        return compound;
    }
    
    public void read(BlockState state, CompoundNBT compound)
    {
        super.read(state, compound);
        this.ticksToHatching = compound.getInt("TimeToHatch");
    }
}
