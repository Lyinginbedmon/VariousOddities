package com.lying.variousoddities.api.event;

import javax.annotation.Nullable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * Fired whenever a player trades with a merchant via the MerchantResultSlot class.<br>
 * This event is not {@link Cancelable}.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * @author Lying<br>
 */
public class PlayerTradeEvent extends PlayerEvent
{
	private final ItemStack productStack;
	private final LivingEntity trader;
	
	public PlayerTradeEvent(PlayerEntity player, @Nullable LivingEntity traderIn, ItemStack reward)
	{
		super(player);
		trader = traderIn;
		productStack = reward.copy();
	}
	
	@Nullable
	public LivingEntity getTrader(){ return trader; }
	public ItemStack getProduct(){ return productStack; }
}
