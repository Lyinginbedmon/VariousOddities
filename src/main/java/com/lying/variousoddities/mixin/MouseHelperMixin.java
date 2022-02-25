package com.lying.variousoddities.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.util.MouseSmoother;
import net.minecraft.entity.LivingEntity;

@Mixin(MouseHelper.class)
public class MouseHelperMixin
{
	Minecraft minecraft = Minecraft.getInstance();
	MouseSmoother xSmoother = new MouseSmoother();
	MouseSmoother ySmoother = new MouseSmoother();
	
	@Shadow
	private double xVelocity;
	@Shadow
	private double yVelocity;
	@Shadow
	private double lastLookTime = Double.MIN_VALUE;
	@Shadow
	private boolean mouseGrabbed;
	
//	@Inject(method = "updatePlayerLook()V", at = @At("HEAD"))
//	public void updatePlayerLook(final CallbackInfo ci)
//	{
//		double partialTicks = NativeUtil.getTime() - this.lastLookTime;
//		if(this.mouseGrabbed && this.minecraft.isGameFocused())
//		{
//			double movement = this.minecraft.gameSettings.mouseSensitivity * (double)0.6F + (double)0.2F;
//			double d5 = movement * movement * movement * 8.0D;
//			double yaw;
//			double pitch;
//			if(this.minecraft.gameSettings.smoothCamera)
//			{
//				yaw = this.xSmoother.smooth(this.xVelocity * d5, partialTicks * d5);
//				pitch = this.ySmoother.smooth(this.yVelocity * d5, partialTicks * d5);
//			}
//			else
//			{
//				this.xSmoother.reset();
//				this.ySmoother.reset();
//				yaw = this.xVelocity * d5;
//				pitch = this.yVelocity * d5;
//			}
//			
//			pitch *= this.minecraft.gameSettings.invertMouse ? -1D : 1D;
//			
//			if(this.minecraft.player != null)
//			{
//				PlayerData data = PlayerData.forPlayer(this.minecraft.player);
//				if(data != null && data.isPossessing())
//					rotateEntity(yaw, pitch, data.getPossessed());
//			}
//		}
//	}
	
	private void rotateEntity(double yaw, double pitch, @Nullable LivingEntity possessed)
	{
		if(possessed != null)
		{
//			VOHelper.addRotationToEntityHead(possessed, yaw, pitch);
//			PacketHandler.sendToServer(new PacketPossessionLook(yaw, pitch));
		}
	}
}
