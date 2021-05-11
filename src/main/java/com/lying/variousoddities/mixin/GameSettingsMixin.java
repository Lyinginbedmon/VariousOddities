package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.utility.VOBusClient;

import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.PointOfView;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(GameSettings.class)
public class GameSettingsMixin
{
	@Inject(method = "getPointOfView", at = @At("HEAD"), cancellable = true)
	public void forceFirstPerson(final CallbackInfoReturnable<PointOfView> ci)
	{
		if(VOBusClient.playerInWall())
			ci.setReturnValue(PointOfView.FIRST_PERSON);
		else if(VOPotions.isParalysed(Minecraft.getInstance().player))
			ci.setReturnValue(PointOfView.THIRD_PERSON_BACK);
	}
	
	@Inject(method = "setPointOfView(Lnet/minecraft/client/settings/PointOfView;)V", at = @At("HEAD"), cancellable = true)
	public void preventThirdPerson(final CallbackInfo ci)
	{
		if(VOBusClient.playerInWall())
			ci.cancel();
	}
}
