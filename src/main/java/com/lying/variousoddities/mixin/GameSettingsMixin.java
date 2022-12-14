package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.init.VOMobEffects;
import com.lying.variousoddities.utility.VOBusClient;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(Options.class)
public class GameSettingsMixin
{
	@Inject(method = "getCameraType()Lnet/minecraft/client/CameraType", at = @At("HEAD"), cancellable = true)
	public void forceFirstPerson(final CallbackInfoReturnable<CameraType> ci)
	{
		if(VOBusClient.playerInWall())
			ci.setReturnValue(CameraType.FIRST_PERSON);
		else if(VOMobEffects.isParalysed(Minecraft.getInstance().player))
			ci.setReturnValue(CameraType.THIRD_PERSON_BACK);
	}
	
	@Inject(method = "setCameraType(Lnet/minecraft/client/settings/CameraType;)V", at = @At("HEAD"), cancellable = true)
	public void preventThirdPerson(final CallbackInfo ci)
	{
		if(VOBusClient.playerInWall())
			ci.cancel();
	}
}
