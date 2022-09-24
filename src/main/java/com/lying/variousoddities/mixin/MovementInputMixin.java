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
import com.lying.variousoddities.init.VOMobEffects;
import com.lying.variousoddities.utility.VOHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(KeyboardInput.class)
public class MovementInputMixin
{
	@Shadow
	@Final
	Options options;
	
	@Inject(method = "tick(Z)V", at = @At("HEAD"), cancellable = true)
	public void dazedPreventMovement(boolean forceDown, final CallbackInfo ci)
	{
		Player player = Minecraft.getInstance().player;
		if(player != null)
		{
			if(!VOHelper.isCreativeOrSpectator(player))
				if(VOMobEffects.isPotionVisible(player, VOMobEffects.DAZED) /*|| (playerData != null && playerData.getBodyCondition() == BodyCondition.UNCONSCIOUS)*/)
				{
					clearInputs();
					ci.cancel();
				}
		}
	}
	
	@Inject(method = "tick(Z)V", at = @At("RETURN"), cancellable = true)
	public void afraidPreventMovement(boolean forceDown, final CallbackInfo ci)
	{
		Player player = Minecraft.getInstance().player;
		if(player != null)
		{
			KeyboardInput input = (KeyboardInput)(Object)this;
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
		clearInputs((KeyboardInput)(Object)this);
	}
	
	private static void clearInputs(KeyboardInput input)
	{
		clearMoveInputs(input);
		input.jumping = false;
		input.shiftKeyDown = false;
	}
	
	private static void clearMoveInputs(KeyboardInput input)
	{
		input.up = false;
		input.down = false;
		input.left = false;
		input.right = false;
		input.forwardImpulse = 0F;
		input.leftImpulse = 0F;
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
	
	private static void handleTerror(Player player, KeyboardInput input, final CallbackInfo ci)
	{
		LivingData data = LivingData.forEntity(player);
		if(data == null)
			return;
		
		List<LivingEntity> terrorisers = data.getMindControlled(Conditions.AFRAID, 16D);
		if(terrorisers.isEmpty())
			return;
		
		Vec3 currentPos = player.position();
		Vec3 move = getAbsoluteMotion(new Vec3(input.leftImpulse, 0D, input.forwardImpulse), 1F, player.yHeadRot).normalize();
		
		float slow = 1F;
		for(LivingEntity terroriser : terrorisers)
		{
			if(terroriser.isInvisibleTo(player) || !player.hasLineOfSight(terroriser))
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
			input.forwardImpulse *= slow;
			input.leftImpulse *= slow;
		}
	}
	
	private static void handleBound(Player player, Vec3 boundPos, double maxDist, KeyboardInput input, final CallbackInfo ci)
	{
		Vec3 currentPos = player.position();
		Vec3 move = getAbsoluteMotion(new Vec3(input.leftImpulse, 0D, input.forwardImpulse), 1F, player.yHeadRot).normalize();
		
		double dist = boundPos.distanceTo(currentPos);
		double distB = boundPos.distanceTo(currentPos.add(move));
		
		if(distB >= maxDist)
		{
			clearMoveInputs(input);
			input.jumping = false;
			ci.cancel();
			return;
		}
		
		maxDist *= 0.5D;
		if(distB > dist && dist > maxDist)
		{
			distB -= maxDist;
			
			float slow = 1F - (float)(distB / maxDist);
			input.forwardImpulse *= slow;
			input.leftImpulse *= slow;
		}
	}
}
