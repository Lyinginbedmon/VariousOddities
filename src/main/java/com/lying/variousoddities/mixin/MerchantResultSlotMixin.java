package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.base.Predicate;
import com.lying.variousoddities.api.event.PlayerTradeEvent;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.MerchantResultSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

@Mixin(MerchantResultSlot.class)
public class MerchantResultSlotMixin
{
	private static final Predicate<LivingEntity> MERCHANT = new Predicate<LivingEntity>()
			{
				public boolean apply(LivingEntity input)
				{
					return input instanceof IMerchant && ((IMerchant)input).getCustomer() != null;
				}
			};
	
	@Inject(method = "onTake", at = @At("RETURN"))
	public void performTrade(PlayerEntity thePlayer, ItemStack stack, CallbackInfoReturnable<?> callback)
	{
		if(thePlayer.getEntityWorld().isRemote)
			return;
		
		LivingEntity merchant = null;
		for(LivingEntity entity : thePlayer.getEntityWorld().getEntitiesWithinAABB(LivingEntity.class, thePlayer.getBoundingBox().grow(6D), MERCHANT))
		{
			if(((IMerchant)entity).getCustomer() == thePlayer)
			{
				merchant = entity;
				break;
			}
		}
		
		MinecraftForge.EVENT_BUS.post(new PlayerTradeEvent(thePlayer, merchant, stack));
	}
}
