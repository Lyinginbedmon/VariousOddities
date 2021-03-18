package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.api.event.FireworkExplosionEvent;

import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraftforge.common.MinecraftForge;

@Mixin(FireworkRocketEntity.class)
public class FireworkRocketEntityMixin extends EntityMixin
{
	@Shadow private static DataParameter<ItemStack> FIREWORK_ITEM;
	
	@Inject(method = "func_213893_k", at = @At("HEAD"))
	public void onFireworkBlast(CallbackInfo callbackInfo)
	{
		ItemStack stack = this.getDataManager().get(FIREWORK_ITEM);
		CompoundNBT stackData = !stack.isEmpty() ? stack.getChildTag("Fireworks") : null;
		MinecraftForge.EVENT_BUS.post(new FireworkExplosionEvent(this.world, this.getPosX(), this.getPosY(), this.getPosZ(), stackData));
	}
}
