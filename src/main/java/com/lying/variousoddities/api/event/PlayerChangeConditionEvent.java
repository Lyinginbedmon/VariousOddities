package com.lying.variousoddities.api.event;

import javax.annotation.Nonnull;

import com.lying.variousoddities.capabilities.PlayerData.BodyCondition;
import com.lying.variousoddities.capabilities.PlayerData.SoulCondition;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class PlayerChangeConditionEvent extends PlayerEvent
{
	private final BodyCondition oldBody, newBody;
	private final SoulCondition oldSoul, newSoul;
	
	public PlayerChangeConditionEvent(@Nonnull PlayerEntity player, BodyCondition oldBodyIn, BodyCondition newBodyIn, SoulCondition oldSoulIn, SoulCondition newSoulIn)
	{
		super(player);
		this.oldBody = oldBodyIn;
		this.newBody = newBodyIn;
		this.oldSoul = oldSoulIn;
		this.newSoul = newSoulIn;
	}
	
	public PlayerChangeConditionEvent(PlayerEntity player, BodyCondition oldBodyIn, BodyCondition newBodyIn)
	{
		this(player, oldBodyIn, newBodyIn, null, null);
	}
	
	public PlayerChangeConditionEvent(PlayerEntity player, SoulCondition oldSoulIn, SoulCondition newSoulIn)
	{
		this(player, null, null, oldSoulIn, newSoulIn);
	}
	
	public boolean bodyChange(){ return oldBody != newBody; }
	
	public boolean soulChange(){ return oldSoul != newSoul; }
	
	public BodyCondition getOldBody(){ return this.oldBody; }
	public BodyCondition getNewBody(){ return this.newBody; }
	
	public SoulCondition getOldSoul(){ return this.oldSoul; }
	public SoulCondition getNewSoul(){ return this.newSoul; }
}
