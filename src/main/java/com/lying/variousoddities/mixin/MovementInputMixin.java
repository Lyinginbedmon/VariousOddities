package com.lying.variousoddities.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.condition.Conditions;
import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.utility.VOHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(MovementInputFromOptions.class)
public class MovementInputMixin
{
	@Shadow
	@Final
	GameSettings gameSettings;
	
	@Inject(method = "tickMovement(Z)V", at = @At("HEAD"), cancellable = true)
	public void dazedPreventMovement(boolean forceDown, final CallbackInfo ci)
	{
		Player player = Minecraft.getInstance().player;
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
	
	@Inject(method = "tickMovement(Z)V", at = @At("RETURN"), cancellable = true)
	public void afraidPreventMovement(boolean forceDown, final CallbackInfo ci)
	{
		Player player = Minecraft.getInstance().player;
		if(player != null)
		{
			MovementInput input = (MovementInput)(Object)this;
			handleTerror(player, input, ci);
			
			PlayerData data = PlayerData.forPlayer(player);
			if(data != null && PlayerData.isPlayerSoulBound(player))
			{
				Entity body = data.getBody(player.getLevel());
				if(body != null && data.getSoulCondition().getWanderRange() >= 0D)
					handleBound(player, body.position(), data.getSoulCondition().getWanderRange(), input, ci);
			}
		}
	}
	
	private void clearInputs()
	{
		clearInputs((MovementInput)(Object)this);
	}
	
	private static void clearInputs(MovementInput input)
	{
		clearMoveInputs(input);
		input.jump = false;
		input.sneaking = false;
	}
	
	private static void clearMoveInputs(MovementInput input)
	{
		input.forwardKeyDown = false;
		input.backKeyDown = false;
		input.leftKeyDown = false;
		input.rightKeyDown = false;
		input.moveForward = 0F;
		input.moveStrafe = 0F;
	}
	
	private static Vec3 getAbsoluteMotion(Vec3 relative, float p_213299_1_, float facing)
	{
		double d0 = relative.lengthSqr();
		if (d0 < 1.0E-7D)
			return Vec3.ZERO;
		
		Vec3 vector3d = (d0 > 1.0D ? relative.normalize() : relative).scale((double)p_213299_1_);
		float f = Mth.sin(facing * ((float)Math.PI / 180F));
		float f1 = Mth.cos(facing * ((float)Math.PI / 180F));
		return new Vec3(vector3d.x * (double)f1 - vector3d.z * (double)f, vector3d.y, vector3d.z * (double)f1 + vector3d.x * (double)f);
	}
	
	private static void handleTerror(Player player, MovementInput input, final CallbackInfo ci)
	{
		LivingData data = LivingData.forEntity(player);
		if(data == null)
			return;
		
		List<LivingEntity> terrorisers = data.getMindControlled(Conditions.AFRAID, 16D);
		if(terrorisers.isEmpty())
			return;
		
		Vec3 currentPos = player.position();
		Vec3 move = getAbsoluteMotion(new Vec3(input.moveStrafe, 0D, input.moveForward), 1F, player.rotationYawHead).normalize();
		
		float slow = 1F;
		for(LivingEntity terroriser : terrorisers)
		{
			if(terroriser.isInvisibleTo(player) || !player.canEntityBeSeen(terroriser))
				continue;
			
			double dist = terroriser.distanceTo(player);
			// If movement would take us too close to terroriser, nullify or slow down
			if(dist > terroriser.distanceToSqr(currentPos.add(move)))
				if(dist <= (6D * 6D))
				{
					clearMoveInputs(input);
					ci.cancel();
					return;
				}
				else
					slow = Math.min(slow, (float)(Math.sqrt(dist) - 6) / 10F);
		}
		
		if(slow < 1F)
		{
			input.moveForward *= slow;
			input.moveStrafe *= slow;
		}
	}
	
	private static void handleBound(Player player, Vec3 boundPos, double maxDist, MovementInput input, final CallbackInfo ci)
	{
		Vec3 currentPos = player.position();
		Vec3 move = getAbsoluteMotion(new Vec3(input.moveStrafe, 0D, input.moveForward), 1F, player.rotationYawHead).normalize();
		
		double dist = boundPos.distanceTo(currentPos);
		double distB = boundPos.distanceTo(currentPos.add(move));
		
		if(distB >= maxDist)
		{
			clearMoveInputs(input);
			input.jump = false;
			ci.cancel();
			return;
		}
		
		maxDist *= 0.5D;
		if(distB > dist && dist > maxDist)
		{
			distB -= maxDist;
			
			float slow = 1F - (float)(distB / maxDist);
			input.moveForward *= slow;
			input.moveStrafe *= slow;
		}
	}
}
