package com.lying.variousoddities.tileentity;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lying.variousoddities.init.VOBlockEntities;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityPhylactery extends BlockEntity
{
	private static int HEAL_RATE = Reference.Values.TICKS_PER_SECOND * 15;
	
	private Component ownerName = null;
	private UUID ownerUUID = null;
	
	private LivingEntity owner = null;
	private boolean isPlayer = false;
	
	private float lastKnownHealth = 0F;
	private int healTicks = HEAL_RATE;
	
	private double maxSize = 64D;
	private int timeSincePlaced = 0;
	private int unoccupiedTicks = 0;
	
	public TileEntityPhylactery(BlockPos pos, BlockState state)
	{
		super(VOBlockEntities.PHYLACTERY, pos, state);
	}
	
	public void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);
		
		if(this.ownerName != null)
			compound.putString("OwnerName", Component.Serializer.toJson(this.ownerName));
		
		if(this.ownerUUID != null)
			compound.putUUID("OwnerUUID", this.ownerUUID);
		
		compound.putBoolean("IsPlayer", this.isPlayer);
		compound.putInt("TimeSincePlaced", this.timeSincePlaced);
		compound.putDouble("MaxSize", this.maxSize);
		if(this.unoccupiedTicks > 0)
			compound.putInt("Unoccupied", this.unoccupiedTicks);
	}
	
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		if(nbt.contains("OwnerName", 8))
			this.ownerName = Component.Serializer.fromJson(nbt.getString("OwnerName"));
		
		if(nbt.contains("OwnerUUID", 11))
			this.ownerUUID = nbt.getUUID("OwnerUUID");
		
		this.isPlayer = nbt.getBoolean("IsPlayer");
		this.timeSincePlaced = nbt.getInt("TimeSincePlaced");
		this.maxSize = nbt.getDouble("MaxSize");
		this.unoccupiedTicks = nbt.getInt("Unoccupied");
		
		markDirty();
	}
	
	public static void serverTick(Level world, BlockPos pos, BlockState state, TileEntityPhylactery phylactery)
	{
		double mistRadius = phylactery.getMistRadius();
		++phylactery.timeSincePlaced;
		if(phylactery.getMistRadius() != mistRadius)
			phylactery.markDirty();
		
		// Identify the owner entity if possible
		if(phylactery.owner == null)
		{
			if(phylactery.isPlayer)
				phylactery.owner = world.getPlayerByUUID(phylactery.ownerUUID);
			else
			{
				// Mob phylactery handling currently not implemented
				if(phylactery.unoccupiedTicks++ > (Reference.Values.TICKS_PER_MINUTE * 20 * 7))
					 // Respawn mob 7 days after loss
					 ;
			}
			
			return;
		}
		else
			phylactery.unoccupiedTicks = 0;
		
		// Update owner name if it has (somehow) changed
		if(phylactery.ownerName == null || !phylactery.ownerName.equals(phylactery.owner.getDisplayName()))
			phylactery.ownerName = phylactery.owner.getDisplayName();
		
		// Players respawn near their phylactery
		if(phylactery.isPlayer)
			if(!world.isClientSide)
			{
				ServerPlayer player = (ServerPlayer)phylactery.owner;
				if(player.getRespawnPosition().distSqr(pos) > 0)
					player.setRespawnPosition(world.dimension(), pos, 0F, true, false);
			}
		
		// Lich is healed whilst in dungeon mist
		if(!world.isClientSide)
		{
			boolean ownerInMist = phylactery.isInsideMist(phylactery.owner);
			if(phylactery.owner.getHealth() < phylactery.lastKnownHealth)
				phylactery.healTicks = HEAL_RATE;
			else if(--phylactery.healTicks < 0)
			{
				if(ownerInMist)
					phylactery.owner.heal(2F);
				phylactery.healTicks = HEAL_RATE;
			}
			phylactery.lastKnownHealth = phylactery.owner.getHealth();
			
			if(ownerInMist && phylactery.timeSincePlaced % 80 == 0)
				phylactery.owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Reference.Values.TICKS_PER_SECOND * 12, 0, true, true));
		}
	}
	
	public void markDirty()
	{
		if(getLevel() == null || getLevel().isClientSide)
			return;
		
		ServerLevel world = (ServerLevel)getLevel();
		world.markAndNotifyBlock(getBlockPos(), world.getChunkAt(getBlockPos()), getBlockState(), getBlockState(), 0, 0);
	}
	
	@Nullable
	public LivingEntity getOwner() { return this.owner; }
	
	public boolean isOwner(LivingEntity entity) { return entity.getUUID().equals(ownerUUID); }
	
	public double getMistRadius() { return Math.min(this.maxSize, Math.floorDiv(this.timeSincePlaced, Reference.Values.TICKS_PER_MINUTE * 5) * 3D); }
	
	public boolean isInsideMist(LivingEntity entity)
	{
		return isInsideMist(entity.blockPosition());
	}
	
	public boolean isInsideMist(BlockPos pos)
	{
		return Math.sqrt(pos.distSqr(getBlockPos())) <= getMistRadius();
	}
	
	public static boolean isValidForMist(BlockPos pos, Level world)
	{
		BlockState state = world.getBlockState(pos);
		if(state.isCollisionShapeFullBlock(world, pos) || !state.getFluidState().isEmpty())
			return false;
		
		BlockState down = world.getBlockState(pos.below());
		return !down.getFluidState().isEmpty() || down.isFaceSturdy(world, pos, Direction.UP, SupportType.CENTER);
	}

	@Nonnull
	public final CompoundTag getUpdateTag()
	{
		CompoundTag compound = new CompoundTag();
		saveAdditional(compound);
		return compound;
	}
	
	@Nullable
	public final ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
	
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
	{
		this.load(pkt.getTag());
	}
}
