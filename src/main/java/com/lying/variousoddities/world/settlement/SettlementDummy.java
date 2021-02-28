package com.lying.variousoddities.world.settlement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.lying.variousoddities.api.world.settlement.EnumRoomFunction;
import com.lying.variousoddities.api.world.settlement.Settlement;
import com.lying.variousoddities.api.world.settlement.SettlementRoomBehaviour;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

/**
 * A simple inert settlement type that notifies players of its name and that of any rooms they enter within it.
 * @author Lying
 *
 */
public class SettlementDummy implements Settlement
{
	public static final ResourceLocation TYPE_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID,"dummy");
	private List<BoxRoom> rooms = new ArrayList<>();
	
	private boolean isDirty = false;
	private boolean noAI = false;
	private boolean invulnerable = false;
	
	private String customName = "";
	private ITextComponent title = null;
	private int range = 5;
	
	@SuppressWarnings("unused")
	private World world = null;
	
	public SettlementDummy(){ }
	public SettlementDummy(World worldIn)
	{
		setWorld(worldIn);
	}
	
	public ResourceLocation typeName(){ return TYPE_NAME; }
	
	public void setWorld(World worldIn)
	{
		this.world = worldIn;
	}
	
	public List<LivingEntity> getResidents()
	{
		return Collections.emptyList();
	}
	
	public void invalidate()
	{
		
	}
	
	public boolean isDirty()
	{
		return this.isDirty;
	}
	
	public void setDirty(boolean bool)
	{
		this.isDirty = bool;
	}
	
	public boolean hasNoAI(){ return this.noAI; }
	public void setNoAI(boolean par1Bool){ this.noAI = par1Bool; }
	
	public boolean isInvulnerable(){ return this.invulnerable; }
	public void setInvulnerable(boolean par1Bool){ this.invulnerable = par1Bool; }
	
	public boolean hasMarker()
	{
		return false;
	}
	
	public void setMarker(Object objIn){ }
	
	public boolean validateMarker()
	{
		return false;
	}
	
	public List<BoxRoom> getRooms()
	{
		return rooms;
	}
	
	public SettlementRoomBehaviour getBehaviourForRoom(EnumRoomFunction function)
	{
		return null;
	}
	
	public void addRoom(BoxRoom roomIn)
	{
		rooms.add(roomIn);
		markDirty();
	}
	
	public void addRoom(int index, BoxRoom roomIn)
	{
		if(rooms.size() < index && index >= 0)
		{
			rooms.set(index, roomIn);
			markDirty();
		}
	}
	
	public boolean removeRoom(BoxRoom roomIn)
	{
		if(rooms.remove(roomIn))
		{
			markDirty();
			return true;
		}
		return false;
	}
	
	public void clearRooms()
	{
		rooms.clear();
		markDirty();
	}
	
	public boolean containsPosition(BlockPos pos)
	{
		for(BoxRoom room : rooms)
			if(room.getBounds().grow(5D).contains(new Vector3d(pos.getX(), pos.getY(), pos.getZ())))
				return true;
		return false;
	}
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		
	}
	
	public void setCustomName(String nameIn)
	{
		this.customName = nameIn;
	}
	
	public String getCustomName()
	{
		return this.customName;
	}
	
	public ITextComponent getTitle(){ return this.title; }
	
	public void setTitle(ITextComponent textComponent){ this.title = textComponent; }
	
	public int getTitleRange(){ return this.range; }
	
	public void setTitleRange(int par1Int){ this.range = par1Int; }
}
