package com.lying.variousoddities.tileentity;

import java.util.UUID;

import com.lying.variousoddities.init.VOTileEntities;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class TileEntityPhylactery extends TileEntity implements ITickableTileEntity
{
	private ITextComponent ownerName = null;
	private UUID ownerUUID = null;
	
	private LivingEntity owner = null;
	private boolean isPlayer = false;
	
	private int timeSincePlaced = 0;
	
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
	}
	
	public void tick()
	{
		World world = this.getWorld();
		++this.timeSincePlaced;
		
		// Identify the owner entity if possible
		if(owner == null)
		{
			if(isPlayer)
				this.owner = world.getPlayerByUuid(ownerUUID);
			else
				;
			// Mob phylactery handling currently not implemented
			
			return;
		}
		
		// Update owner name if it has (somehow) changed
		if(!this.ownerName.equals(this.owner.getDisplayName()))
			this.ownerName = this.owner.getDisplayName();
		
		if(isPlayer)
		{
			// Ensure player will respawn near phylactery
			if(!world.isRemote)
				((ServerPlayerEntity)this.owner).func_242111_a(world.getDimensionKey(), getPos(), 0F, true, false);
		}
	}
}
