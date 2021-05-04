package com.lying.variousoddities.api.event;

import com.lying.variousoddities.types.TypeHandler.DamageResist;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingEvent;

public class DamageResistanceEvent extends LivingEvent
{
	private final DamageSource source;
	private DamageResist resistance = DamageResist.NORMAL;
	
	public DamageResistanceEvent(DamageSource sourceIn, LivingEntity entityIn)
	{
		super(entityIn);
		source = sourceIn;
	}
	
	public DamageSource getSource(){ return source; }
	
	public DamageResist getResistance(){ return resistance; }
	public void setResistance(DamageResist resist){ this.resistance = resist; }
}
