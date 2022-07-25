package com.lying.variousoddities.api.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public class LivingWakeUpEvent extends LivingEvent
{
	private final boolean forced;
	
	public LivingWakeUpEvent(LivingEntity entity, boolean wasForced)
	{
		super(entity);
		this.forced = wasForced;
	}
	
	/**
	 * Returns true if this event was triggered by an external action, such as being attacked.
	 * @return
	 */
	public boolean wasWokenUp()
	{
		return this.forced;
	}
}
