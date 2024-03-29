package com.lying.variousoddities.init;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.*;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class VOCapabilities
{
	public static final Capability<LivingData> LIVING_DATA = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<AbilityData> ABILITY_DATA = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<PlayerData> PLAYER_DATA = CapabilityManager.get(new CapabilityToken<>() {});
	
	public static void onRegisterCapabilities(RegisterCapabilitiesEvent event)
	{
		VariousOddities.log.info("Registered data capabilities");
		event.register(LivingData.class);
		event.register(AbilityData.class);
		event.register(PlayerData.class);
		
	}
}
