package com.lying.variousoddities.api.event;

import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Event.HasResult;

public abstract class LivingBreathingEvent
{
	/**
	 * Fired by EntityMixin whenever getMaxAirSupply is called for a LivingEntity
	 * @author Lying
	 *
	 */
	public static class LivingMaxAirEvent extends LivingEvent
	{
		private int maxAir;
		
		public LivingMaxAirEvent(LivingEntity living, int maxAirIn)
		{
			super(living);
			maxAir = maxAirIn;
		}
		
		public int maxAir() { return this.maxAir; }
		
		public void setMaxAir(int par1Int) { this.maxAir = par1Int; }
	}
	
	@HasResult
	public static class LivingCanBreatheFluidEvent extends LivingEvent
	{
		private final FluidState state;
		
		public LivingCanBreatheFluidEvent(LivingEntity living, @Nullable FluidState stateIn)
		{
			super(living);
			this.state = stateIn;
		}
		
		@Nullable
		public FluidState state() { return this.state; }
	}
}