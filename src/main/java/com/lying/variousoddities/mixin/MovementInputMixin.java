package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.utility.VOHelper;

import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(MovementInputFromOptions.class)
public class MovementInputMixin
{
	@Shadow
	GameSettings gameSettings;
	
	@Inject(method = "tickMovement(Z)V", at = @At("HEAD"), cancellable = true)
	public void dazedPreventMovement(boolean forceDown, final CallbackInfo ci)
	{
		PlayerEntity player = Minecraft.getInstance().player;
		if(player != null)
		{
			if(!VOHelper.isCreativeOrSpectator(player))
				if(VOPotions.isPotionVisible(player, VOPotions.DAZED) /*|| (playerData != null && playerData.getBodyCondition() == BodyCondition.UNCONSCIOUS)*/)
				{
					clearInputs();
					ci.cancel();
				}
		}
	}
	
	private void clearInputs()
	{
		MovementInput input = (MovementInput)(Object)this;
		input.forwardKeyDown = false;
		input.backKeyDown = false;
		input.leftKeyDown = false;
		input.rightKeyDown = false;
		input.moveForward = 0F;
		input.moveStrafe = 0F;
		input.jump = false;
		input.sneaking = false;
	}
}
