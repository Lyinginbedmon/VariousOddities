package com.lying.variousoddities.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.world.ForgeChunkManager;

public class EntityBodyCorpse extends AbstractBody
{
	private static final EntityDataAccessor<Integer> TIMER = SynchedEntityData.defineId(EntityBodyCorpse.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> LOADED = SynchedEntityData.defineId(EntityBodyCorpse.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<BlockPos> LOAD_POS = SynchedEntityData.defineId(EntityBodyCorpse.class, EntityDataSerializers.BLOCK_POS);
	
	public EntityBodyCorpse(EntityType<? extends EntityBodyCorpse> type, Level worldIn)
	{
		super(type, worldIn);
	}
	
	@Nullable
	public static EntityBodyCorpse createCorpseFrom(@Nonnull LivingEntity living)
	{
		if(living == null) return null;
		EntityBodyCorpse corpse = new EntityBodyCorpse(VOEntities.CORPSE, living.getLevel());
		corpse.copyFrom(living, true);
		return corpse;
	}
	
	public void defineSynchedData()
	{
		super.defineSynchedData();
		getEntityData().define(TIMER, Reference.Values.TICKS_PER_MINUTE * 15);
		getEntityData().define(LOADED, false);
		getEntityData().define(LOAD_POS, BlockPos.ZERO);
	}
	
	public void readAdditional(CompoundTag compound)
	{
		super.readAdditional(compound);
		getEntityData().set(TIMER, compound.getInt("TicksRemaining"));
		getEntityData().set(LOADED, compound.getBoolean("ChunkLoading"));
		getEntityData().set(LOAD_POS, NbtUtils.readBlockPos(compound.getCompound("ChunkLoadPos")));
	}
	
	public void writeAdditional(CompoundTag compound)
	{
		super.writeAdditional(compound);
		compound.putInt("TicksRemaining", getTicksRemaining());
		compound.putBoolean("ChunkLoading", getEntityData().get(LOADED).booleanValue());
		compound.put("ChunkLoadPos", NbtUtils.writeBlockPos(getEntityData().get(LOAD_POS)));
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
				
				if(PlayerData.isPlayerSoulBound(soul) && this.isAlive())
					moveWithinRangeOf(this, soul, PlayerData.forPlayer((Player)soul).getSoulCondition().getWanderRange());
				
				if(!PlayerData.isPlayerBodyDead((Player)soul) && !getLevel().isClientSide)
					this.kill();
			}
			
			if(getInventory().isEmpty() && !getLevel().isClientSide)
			{
				getEntityData().set(TIMER, Math.max(0, getTicksRemaining() - 1));
				if(getTicksRemaining() == 0)
					this.kill();
			}
		}
		
		if(isPlayer() && !getLevel().isClientSide)
			if(!isLoaded())
				loadChunks();
			else
			{
				ChunkPos current = new ChunkPos(blockPosition());
				ChunkPos previous = new ChunkPos(getEntityData().get(LOAD_POS));
				
				if(current != previous)
				{
					unloadChunks(previous.x, previous.z);
					loadChunks();
					
					getEntityData().set(LOAD_POS, blockPosition());
				}
			}
	}
	
	public int getTicksRemaining(){ return getEntityData().get(TIMER).intValue(); }
	
	public void kill()
	{
		super.kill();
		unloadChunks();
	}
	
	public boolean isLoaded(){ return getEntityData().get(LOADED).booleanValue(); }
	
	public void loadChunks()
	{
		if(getLevel().isClientSide) return;
		
		ServerLevel world = (ServerLevel)getLevel();
		for(int x=-1; x<2; x++)
			for(int z=-1; z<2; z++)
				ForgeChunkManager.forceChunk(world, Reference.ModInfo.MOD_ID, this, this.chunkCoordX + x, this.chunkCoordZ + z, true, true);
		
		getEntityData().set(LOADED, true);
	}
	
	public void unloadChunks()
	{
		unloadChunks(this.chunkCoordX, this.chunkCoordZ);
	}
	
	public void unloadChunks(int chunkX, int chunkZ)
	{
		if(getLevel().isClientSide) return;
		
		ServerLevel world = (ServerLevel)getLevel();
		for(int x=-1; x<2; x++)
			for(int z=-1; z<2; z++)
				ForgeChunkManager.forceChunk(world, Reference.ModInfo.MOD_ID, this, chunkX + x, chunkZ + z, false, true);
		
		getEntityData().set(LOADED, false);
	}
}
