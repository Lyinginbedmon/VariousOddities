package com.lying.variousoddities.tileentity;

import java.util.UUID;

import javax.annotation.Nullable;

import com.lying.variousoddities.init.VOTileEntities;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockVoxelShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class TileEntityPhylactery extends TileEntity implements ITickableTileEntity
{
	private static int HEAL_RATE = Reference.Values.TICKS_PER_SECOND * 15;
	
	private ITextComponent ownerName = null;
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
	
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);
		
		if(this.ownerName != null)
			compound.putString("OwnerName", ITextComponent.Serializer.toJson(this.ownerName));
		
		if(this.ownerUUID != null)
			compound.putUniqueId("OwnerUUID", this.ownerUUID);
		
		compound.putBoolean("IsPlayer", this.isPlayer);
		compound.putInt("TimeSincePlaced", this.timeSincePlaced);
		compound.putDouble("MaxSize", this.maxSize);
		if(this.unoccupiedTicks > 0)
			compound.putInt("Unoccupied", this.unoccupiedTicks);
		
		return compound;
	}
	
	public void read(BlockState state, CompoundNBT nbt)
	{
		super.read(state, nbt);
		if(nbt.contains("OwnerName", 8))
			this.ownerName = ITextComponent.Serializer.getComponentFromJson(nbt.getString("OwnerName"));
		
		if(nbt.contains("OwnerUUID", 11))
			this.ownerUUID = nbt.getUniqueId("OwnerUUID");
		
		this.isPlayer = nbt.getBoolean("IsPlayer");
		this.timeSincePlaced = nbt.getInt("TimeSincePlaced");
		this.maxSize = nbt.getDouble("MaxSize");
		this.unoccupiedTicks = nbt.getInt("Unoccupied");
		
		markDirty();
	}
	
	public void tick()
	{
		World world = this.getWorld();
		double mistRadius = getMistRadius();
		++this.timeSincePlaced;
		if(getMistRadius() != mistRadius)
			markDirty();
		
		// Identify the owner entity if possible
		if(owner == null)
		{
			if(isPlayer)
				this.owner = world.getPlayerByUuid(ownerUUID);
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
			if(!world.isRemote)
			{
				ServerPlayerEntity player = (ServerPlayerEntity)this.owner;
				if(player.func_241140_K_().distanceSq(getPos()) > 0)
					player.func_242111_a(world.getDimensionKey(), getPos(), 0F, true, false);
			}
		
		// Lich is healed whilst in dungeon mist
		if(!world.isRemote)
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
				this.owner.addPotionEffect(new EffectInstance(Effects.RESISTANCE, Reference.Values.TICKS_PER_SECOND * 12, 0, true, true));
		}
	}
	
	public void markDirty()
	{
		if(getWorld() == null || getWorld().isRemote)
			return;
		
		ServerWorld world = (ServerWorld)getWorld();
		world.markAndNotifyBlock(getPos(), world.getChunkAt(getPos()), getBlockState(), getBlockState(), 0, 0);
	}
	
	@Nullable
	public LivingEntity getOwner() { return this.owner; }
	
	public boolean isOwner(LivingEntity entity) { return entity.getUniqueID().equals(ownerUUID); }
	
	public double getMistRadius() { return Math.min(this.maxSize, Math.floorDiv(this.timeSincePlaced, Reference.Values.TICKS_PER_MINUTE * 5) * 3D); }
	
	public boolean isInsideMist(LivingEntity entity)
	{
		return isInsideMist(entity.getPosition());
	}
	
	public boolean isInsideMist(BlockPos pos)
	{
		return Math.sqrt(pos.distanceSq(getPos())) <= getMistRadius();
	}
	
	public static boolean isValidForMist(BlockPos pos, World world)
	{
		BlockState state = world.getBlockState(pos);
		if(state.isSolid() || !state.getFluidState().isEmpty())
			return false;
		
		BlockState down = world.getBlockState(pos.down());
		return !down.getFluidState().isEmpty() || down.func_242698_a(world, pos, Direction.UP, BlockVoxelShape.CENTER);
	}
	
	public CompoundNBT getUpdateTag() { return this.write(new CompoundNBT()); }
	
	@Nullable
	public SUpdateTileEntityPacket getUpdatePacket() { return new SUpdateTileEntityPacket(this.getPos(), -1, this.getUpdateTag()); }
	
	public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SUpdateTileEntityPacket pkt) { this.read(this.getBlockState(), pkt.getNbtCompound()); }
}
