package com.lying.variousoddities.tileentity;

import java.util.UUID;

import javax.annotation.Nullable;

import com.lying.variousoddities.init.VOTileEntities;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.BlockVoxelShape;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityPhylactery extends BlockEntity implements ITickableTileEntity
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
	
	public TileEntityPhylactery()
	{
		super(VOTileEntities.PHYLACTERY);
	}
	
	public CompoundTag write(CompoundTag compound)
	{
		super.write(compound);
		
		if(this.ownerName != null)
			compound.putString("OwnerName", Component.Serializer.toJson(this.ownerName));
		
		if(this.ownerUUID != null)
			compound.putUUID("OwnerUUID", this.ownerUUID);
		
		compound.putBoolean("IsPlayer", this.isPlayer);
		compound.putInt("TimeSincePlaced", this.timeSincePlaced);
		compound.putDouble("MaxSize", this.maxSize);
		if(this.unoccupiedTicks > 0)
			compound.putInt("Unoccupied", this.unoccupiedTicks);
		
		return compound;
	}
	
	public void read(BlockState state, CompoundTag nbt)
	{
		super.read(state, nbt);
		if(nbt.contains("OwnerName", 8))
			this.ownerName = Component.Serializer.getComponentFromJson(nbt.getString("OwnerName"));
		
		if(nbt.contains("OwnerUUID", 11))
			this.ownerUUID = nbt.getUUID("OwnerUUID");
		
		this.isPlayer = nbt.getBoolean("IsPlayer");
		this.timeSincePlaced = nbt.getInt("TimeSincePlaced");
		this.maxSize = nbt.getDouble("MaxSize");
		this.unoccupiedTicks = nbt.getInt("Unoccupied");
		
		markDirty();
	}
	
	public void tick()
	{
		Level world = this.getLevel();
		double mistRadius = getMistRadius();
		++this.timeSincePlaced;
		if(getMistRadius() != mistRadius)
			markDirty();
		
		// Identify the owner entity if possible
		if(owner == null)
		{
			if(isPlayer)
				this.owner = world.getPlayerByUUID(ownerUUID);
			else
			{
				// Mob phylactery handling currently not implemented
				if(this.unoccupiedTicks++ > (Reference.Values.TICKS_PER_MINUTE * 20 * 7))
					 // Respawn mob 7 days after loss
					 ;
			}
			
			return;
		}
		else
			this.unoccupiedTicks = 0;
		
		// Update owner name if it has (somehow) changed
		if(this.ownerName == null || !this.ownerName.equals(this.owner.getDisplayName()))
			this.ownerName = this.owner.getDisplayName();
		
		// Players respawn near their phylactery
		if(isPlayer)
			if(!world.isClientSide)
			{
				ServerPlayer player = (ServerPlayer)this.owner;
				if(player.getRespawnPosition().distSqr(getBlockPos()) > 0)
					player.setRespawnPosition(world.dimension(), getBlockPos(), 0F, true, false);
			}
		
		// Lich is healed whilst in dungeon mist
		if(!world.isClientSide)
		{
			boolean ownerInMist = isInsideMist(this.owner);
			if(this.owner.getHealth() < this.lastKnownHealth)
				this.healTicks = HEAL_RATE;
			else if(--this.healTicks < 0)
			{
				if(ownerInMist)
					this.owner.heal(2F);
				this.healTicks = HEAL_RATE;
			}
			this.lastKnownHealth = this.owner.getHealth();
			
			if(ownerInMist && this.timeSincePlaced % 80 == 0)
				this.owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Reference.Values.TICKS_PER_SECOND * 12, 0, true, true));
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
		return !down.getFluidState().isEmpty() || down.func_242698_a(world, pos, Direction.UP, BlockVoxelShape.CENTER);
	}
	
	public CompoundTag getUpdateTag() { return this.write(new CompoundTag()); }
	
	@Nullable
	public SUpdateTileEntityPacket getUpdatePacket() { return new SUpdateTileEntityPacket(this.getBlockPos(), -1, this.getUpdateTag()); }
	
	public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SUpdateTileEntityPacket pkt) { this.read(this.getBlockState(), pkt.getNbtCompound()); }
}
