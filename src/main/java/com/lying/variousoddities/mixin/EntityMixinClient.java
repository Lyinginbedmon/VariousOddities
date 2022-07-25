package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.abilities.AbilityVision;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
		
		Player player = Minecraft.getInstance().player;
		if(player != null && ent != player)
		{
			if(PlayerData.isPlayerSoulBound(player) && PlayerData.isPlayerBody(player, ent))
				ci.setReturnValue(true);
			
			double dist = Math.sqrt(player.distanceToSqr(ent));
			for(AbilityVision vision : AbilityRegistry.getAbilitiesOfType(player, AbilityVision.class))
				if(vision != null && vision.isInRange(dist) && vision.testEntity(ent, player))
				{
					ci.setReturnValue(true);
					break;
				}
		}
	}
}