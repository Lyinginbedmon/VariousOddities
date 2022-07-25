package com.lying.variousoddities.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.enchantment.TemporaryEnchantment;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

@Mixin(ItemStack.class)
public class ItemStackMixin
{
	@Inject(method = "inventoryTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;IZ)V", at = @At("HEAD"))
	public void inventoryTick(Level worldIn, Entity entityIn, int itemSlot, boolean isSelected, CallbackInfo ci)
	{
		ItemStack stack = (ItemStack)(Object)this;
		if(stack.isEnchanted())
		{
			Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
			for(Enchantment enchant : enchantments.keySet())
				if(enchant instanceof TemporaryEnchantment)
					((TemporaryEnchantment)enchant).inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		}
	}
}
