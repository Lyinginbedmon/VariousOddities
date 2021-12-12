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
	
//	@Inject(method = "tickMovement(Z)V", at = @At("HEAD"), cancellable = true)
//	public void possessedTransmitMovement(boolean forceDown, final CallbackInfo ci)
//	{
//		PlayerEntity player = Minecraft.getInstance().player;
//		if(player != null)
//		{
//			PlayerData playerData = PlayerData.forPlayer(player);
//			if(playerData != null && playerData.isPossessing())
//			{
//				boolean forwardKeyDown = this.gameSettings.keyBindForward.isKeyDown();
//				boolean backKeyDown = this.gameSettings.keyBindBack.isKeyDown();
//				boolean leftKeyDown = this.gameSettings.keyBindLeft.isKeyDown();
//				boolean rightKeyDown = this.gameSettings.keyBindRight.isKeyDown();
//				float moveForward = forwardKeyDown == backKeyDown ? 0.0F : (forwardKeyDown ? 1.0F : -1.0F);
//				float moveStrafe = leftKeyDown == rightKeyDown ? 0.0F : (leftKeyDown ? 1.0F : -1.0F);
//				boolean jump = this.gameSettings.keyBindJump.isKeyDown();
//				boolean sneaking = this.gameSettings.keyBindSneak.isKeyDown();
//				if(forceDown)
//				{
//					moveStrafe = (float)((double)moveStrafe * 0.3D);
//					moveForward = (float)((double)moveForward * 0.3D);
//				}
//				PacketHandler.sendToServer(new PacketPossessionControl(moveStrafe, moveForward, sneaking, jump));
//				
//				MobEntity mob = (MobEntity)playerData.getPossessed();
//				if(mob != null)
//				{
//					mob.getMoveHelper().strafe(moveForward, moveStrafe);
//					mob.setSneaking(sneaking);
//					if(jump)
//						mob.getJumpController().setJumping();
//				}
//				
//				clearInputs();
//				ci.cancel();
//			}
//		}
//	}
	
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
