package com.lying.variousoddities.entity;

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class EntityBodyCorpse extends AbstractBody
{
	private static final DataParameter<Integer> TIMER = EntityDataManager.<Integer>createKey(EntityBodyCorpse.class, DataSerializers.VARINT);
	
	private boolean persistent = false;
	
	public EntityBodyCorpse(EntityType<? extends EntityBodyCorpse> type, World worldIn)
	{
		super(type, worldIn);
	}
    
    public static boolean canSpawnAt(EntityType<?> animal, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
	    return true;
    }
	
	@Nullable
	public static EntityBodyCorpse createCorpseFrom(@Nonnull LivingEntity living)
	{
		if(living == null) return null;
		EntityBodyCorpse corpse = new EntityBodyCorpse(VOEntities.CORPSE, living.getEntityWorld());
		corpse.copyFrom(living);
		return corpse;
	}
	
	public void registerData()
	{
		super.registerData();
		getDataManager().register(TIMER, Reference.Values.TICKS_PER_MINUTE * 15);
	}
	
	public void readAdditional(CompoundNBT compound)
	{
		super.readAdditional(compound);
		this.persistent = compound.getBoolean("PersistenceRequired");
		getDataManager().set(TIMER, compound.getInt("TicksRemaining"));
	}
	
	public void writeAdditional(CompoundNBT compound)
	{
		super.writeAdditional(compound);
		compound.putBoolean("PersistenceRequired", this.persistent);
		compound.putInt("TicksRemaining", getTicksRemaining());
	}
	
	public void tick()
	{
		super.tick();
		
		if(!persistent)
		{
			getDataManager().set(TIMER, Math.max(0, getTicksRemaining() - 1));
			if(getTicksRemaining() == 0 && getEntityWorld().isRemote)
				onKillCommand();
		}
	}
	
	public int getTicksRemaining(){ return getDataManager().get(TIMER).intValue(); }
}
