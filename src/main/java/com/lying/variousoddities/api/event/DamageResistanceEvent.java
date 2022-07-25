package com.lying.variousoddities.api.event;

import com.lying.variousoddities.species.types.TypeHandler.DamageResist;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
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
