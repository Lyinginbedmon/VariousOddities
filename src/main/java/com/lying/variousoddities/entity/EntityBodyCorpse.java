package com.lying.variousoddities.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.world.ForgeChunkManager;

public class EntityBodyCorpse extends AbstractBody
{
	private static final DataParameter<Integer> TIMER = EntityDataManager.<Integer>createKey(EntityBodyCorpse.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> LOADED = EntityDataManager.<Boolean>createKey(EntityBodyCorpse.class, DataSerializers.BOOLEAN);
	private static final DataParameter<BlockPos> LOAD_POS = EntityDataManager.<BlockPos>createKey(EntityBodyCorpse.class, DataSerializers.BLOCK_POS);
	
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
	
	public void registerData()
	{
		super.registerData();
		getDataManager().register(TIMER, Reference.Values.TICKS_PER_MINUTE * 15);
		getDataManager().register(LOADED, false);
		getDataManager().register(LOAD_POS, BlockPos.ZERO);
	}
	
	public void readAdditional(CompoundTag compound)
	{
		super.readAdditional(compound);
		getDataManager().set(TIMER, compound.getInt("TicksRemaining"));
		getDataManager().set(LOADED, compound.getBoolean("ChunkLoading"));
		getDataManager().set(LOAD_POS, NbtUtils.readBlockPos(compound.getCompound("ChunkLoadPos")));
	}
	
	public void writeAdditional(CompoundTag compound)
	{
		super.writeAdditional(compound);
		compound.putInt("TicksRemaining", getTicksRemaining());
		compound.putBoolean("ChunkLoading", getDataManager().get(LOADED).booleanValue());
		compound.put("ChunkLoadPos", NbtUtils.writeBlockPos(getDataManager().get(LOAD_POS)));
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
					this.onKillCommand();
			}
			
			if(getInventory().isEmpty() && !getLevel().isClientSide)
			{
				getDataManager().set(TIMER, Math.max(0, getTicksRemaining() - 1));
				if(getTicksRemaining() == 0)
					this.onKillCommand();
			}
		}
		
		if(isPlayer() && !getLevel().isClientSide)
			if(!isLoaded())
				loadChunks();
			else
			{
				ChunkPos current = new ChunkPos(getPosition());
				ChunkPos previous = new ChunkPos(getDataManager().get(LOAD_POS));
				
				if(current != previous)
				{
					unloadChunks(previous.x, previous.z);
					loadChunks();
					
					getDataManager().set(LOAD_POS, getPosition());
				}
			}
	}
	
	public int getTicksRemaining(){ return getDataManager().get(TIMER).intValue(); }
	
	public void kill()
	{
		super.kill();
		unloadChunks();
	}
	
	public boolean isLoaded(){ return getDataManager().get(LOADED).booleanValue(); }
	
	public void loadChunks()
	{
		if(getLevel().isClientSide) return;
		
		ServerLevel world = (ServerLevel)getLevel();
		for(int x=-1; x<2; x++)
			for(int z=-1; z<2; z++)
				ForgeChunkManager.forceChunk(world, Reference.ModInfo.MOD_ID, this, this.chunkCoordX + x, this.chunkCoordZ + z, true, true);
		
		getDataManager().set(LOADED, true);
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
		
		getDataManager().set(LOADED, false);
	}
}
