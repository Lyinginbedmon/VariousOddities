package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.api.event.FireworkExplosionEvent;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

@Mixin(FireworkRocketEntity.class)
public class FireworkRocketEntityMixin extends EntityMixin
{
	@Shadow private static EntityDataAccessor<ItemStack> DATA_ID_FIREWORKS_ITEM;
	
	@Inject(method = "explode", at = @At("HEAD"))
	public void onFireworkBlast(CallbackInfo callbackInfo)
	{
		ItemStack stack = this.getEntityData().get(DATA_ID_FIREWORKS_ITEM);
		CompoundTag stackData = !stack.isEmpty() ? stack.getTagElement("Fireworks") : null;
		MinecraftForge.EVENT_BUS.post(new FireworkExplosionEvent(this.level, this.getX(), this.getY(), this.getZ(), stackData));
	}
}
