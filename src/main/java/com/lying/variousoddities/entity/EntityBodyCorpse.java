package com.lying.variousoddities.entity;

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
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
		getDataManager().set(TIMER, compound.getInt("TicksRemaining"));
	}
	
	public void writeAdditional(CompoundNBT compound)
	{
		super.writeAdditional(compound);
		compound.putInt("TicksRemaining", getTicksRemaining());
	}
	
	public boolean shouldBindIfPersistent(){ return false; }
	
	public void tick()
	{
		super.tick();
		
		if(!isPersistenceRequired())
		{
			if(isPlayer())
			{
				LivingEntity soul = getSoul();
				if(soul == null || !soul.isAlive())
					return;
				
				if(PlayerData.isPlayerSoulBound(soul))
					moveWithinRangeOf(this, soul, PlayerData.forPlayer((PlayerEntity)soul).getSoulCondition().getWanderRange());
				
				if(!PlayerData.isPlayerBodyDead((PlayerEntity)soul) && !getEntityWorld().isRemote)
					this.onKillCommand();
			}
			
			if(getInventory().isEmpty() && !getEntityWorld().isRemote)
			{
				getDataManager().set(TIMER, Math.max(0, getTicksRemaining() - 1));
				if(getTicksRemaining() == 0)
					this.onKillCommand();
			}
		}
	}
	
	public int getTicksRemaining(){ return getDataManager().get(TIMER).intValue(); }
}
