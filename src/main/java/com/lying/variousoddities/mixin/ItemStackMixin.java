package com.lying.variousoddities.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.enchantment.TemporaryEnchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(ItemStack.class)
public class ItemStackMixin
{
	@Inject(method = "inventoryTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;IZ)V", at = @At("HEAD"))
	public void inventoryTick(World worldIn, Entity entityIn, int itemSlot, boolean isSelected, CallbackInfo ci)
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
