package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.base.Predicate;
import com.lying.variousoddities.api.event.PlayerTradeEvent;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraftforge.common.MinecraftForge;

@Mixin(MerchantResultSlot.class)
public class MerchantResultSlotMixin
{
	private static final Predicate<LivingEntity> MERCHANT = new Predicate<LivingEntity>()
			{
				public boolean apply(LivingEntity input)
				{
					return input instanceof Merchant && ((Merchant)input).getTradingPlayer() != null;
				}
			};
	
	@Inject(method = "onTake", at = @At("RETURN"))
	public void performTrade(Player thePlayer, ItemStack stack, CallbackInfo callback)
	{
		if(thePlayer.getLevel().isClientSide)
			return;
		
		LivingEntity merchant = null;
		for(LivingEntity entity : thePlayer.getLevel().getEntitiesOfClass(LivingEntity.class, thePlayer.getBoundingBox().inflate(6D), MERCHANT))
		{
			if(((Merchant)entity).getTradingPlayer() == thePlayer)
			{
				merchant = entity;
				break;
			}
		}
		
		MinecraftForge.EVENT_BUS.post(new PlayerTradeEvent(thePlayer, merchant, stack));
	}
}
