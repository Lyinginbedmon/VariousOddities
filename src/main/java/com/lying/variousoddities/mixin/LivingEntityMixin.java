package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.capabilities.LivingData;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.common.util.LazyOptional;

@Mixin(LivingEntity.class)
public class LivingEntityMixin extends EntityMixin
{
	@Shadow
	public float getHealth(){ return 0F; }
	
	@Shadow
	public float getMaxHealth(){ return 0F; }
	
	@Inject(method = "baseTick", at = @At("TAIL"))
	public void baseTick(CallbackInfo callbackInfo)
	{
		LazyOptional<LivingData> livingCap = this.getCapability(LivingData.CAPABILITY, null);
		if(livingCap.isPresent())
		{
			LivingData livingData = livingCap.orElseThrow(() -> new RuntimeException("No living data found in mixin"));
			if(livingData.overrideAir())
				this.setAir(livingData.getAir());
		}
	}
}
