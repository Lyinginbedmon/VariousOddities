package com.lying.variousoddities.api.event;

import com.lying.variousoddities.api.world.settlement.Settlement;
import com.lying.variousoddities.world.savedata.SettlementManager;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired by SettlementManager during startup.<br>
 * Used to add third-party settlement types.
 * @author Lying
 */
public class SettlementRegistryEvent extends Event
{
	public void registerSettlement(ResourceLocation nameIn, Class<? extends Settlement> classIn)
	{
		SettlementManager.registerSettlement(nameIn, classIn);
	}
	
	public void registerSettlement(Settlement objIn)
	{
		SettlementManager.registerSettlement(objIn);
	}
}
