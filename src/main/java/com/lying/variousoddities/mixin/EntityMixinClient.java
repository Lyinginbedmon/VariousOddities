package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.species.abilities.AbilityBlindsight;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.abilities.AbilityTremorsense;

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
			double dist = Math.sqrt(player.getDistanceSq(ent));
			if(AbilityRegistry.hasAbility(player, AbilityTremorsense.REGISTRY_NAME))
			{
				AbilityTremorsense tremorsense = (AbilityTremorsense)AbilityRegistry.getAbilityByName(player, AbilityTremorsense.REGISTRY_NAME);
				if(tremorsense.isInRange(dist) && tremorsense.testEntity(ent, player))
					ci.setReturnValue(true);
			}
			
			if(AbilityRegistry.hasAbility(player, AbilityBlindsight.REGISTRY_NAME))
			{
				AbilityBlindsight blindsight = (AbilityBlindsight)AbilityRegistry.getAbilityByName(player, AbilityBlindsight.REGISTRY_NAME);
				if(blindsight.isInRange(dist) && blindsight.testEntity(ent, player))
					ci.setReturnValue(true);
			}
		}
	}
}