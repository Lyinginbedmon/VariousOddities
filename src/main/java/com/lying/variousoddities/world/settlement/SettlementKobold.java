package com.lying.variousoddities.world.settlement;

import com.lying.variousoddities.api.world.settlement.EnumRoomFunction;
import com.lying.variousoddities.api.world.settlement.SettlementRoomBehaviour;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class SettlementKobold extends SettlementDummy
{
	public static final ResourceLocation TYPE_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "kobold");
	
	public SettlementKobold(){ super(); }
	public SettlementKobold(World worldIn){ super(worldIn); }
	
	public ResourceLocation typeName(){ return TYPE_NAME; }
	
	public SettlementRoomBehaviour getBehaviourForRoom(EnumRoomFunction function)
	{
		switch(function)
		{
			case NEST:
				return SettlementRoomBehaviours.KOBOLD_NEST;
			default:
				return null;
		}
	}
}
