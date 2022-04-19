package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.entity.AbstractBody;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.abilities.AbilityVision;
import com.lying.variousoddities.utility.VOHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(Entity.class)
public class EntityMixinClient
{
	@Inject(method = "isGlowing()Z", at = @At("HEAD"), cancellable = true)
	public void isGlowing(final CallbackInfoReturnable<Boolean> ci)
	{
		Entity ent = (Entity)(Object)this;
		
		PlayerEntity player = Minecraft.getInstance().player;
		if(player != null && ent != player)
		{
			if(PlayerData.isPlayerSoulBound(player) && PlayerData.isPlayerBody(player, ent))
				ci.setReturnValue(true);
			
			double dist = Math.sqrt(player.getDistanceSq(ent));
			for(AbilityVision vision : AbilityRegistry.getAbilitiesOfType(player, AbilityVision.class))
				if(vision != null && vision.isInRange(dist) && vision.testEntity(ent, player))
				{
					ci.setReturnValue(true);
					break;
				}
		}
	}
	
	@Inject(method = "tick()V", at = @At("TAIL"))
	public void tickTail(final CallbackInfo ci)
	{
		Entity ent = (Entity)(Object)this;
		PlayerEntity player = Minecraft.getInstance().player;
		if(player == null)
			return;
		
		if(ent != player && ent.isAlive())
		{
			if(PlayerData.isPlayerSoulBound(player) && PlayerData.isPlayerBody(player, ent) && !VOHelper.isCreativeOrSpectator(player))
				AbstractBody.moveWithinRangeOf(ent, player, PlayerData.forPlayer(player).getSoulCondition().getWanderRange());
		}
	}
}