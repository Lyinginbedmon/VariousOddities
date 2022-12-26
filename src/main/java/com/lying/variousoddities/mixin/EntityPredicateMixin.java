package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;

import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

@Mixin(EntityPredicate.class)
public class EntityPredicateMixin
{
	@Inject(method = "canTarget(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z", at = @At("RETURN"), cancellable = true)
	public void canTarget(LivingEntity attacker, LivingEntity victim, final CallbackInfoReturnable<Boolean> ci)
	{
		if(victim == null)
			return;
		
		LivingData data = LivingData.getCapability(attacker);
		if(data != null && data.isTargetingHindered(victim))
		{
			ci.setReturnValue(false);
			ci.cancel();
		}
		else if(victim.getType() == EntityType.PLAYER)
			if(!PlayerData.isPlayerNormalFunction(victim))
			{
				if(attacker instanceof Mob)
				{
					Mob mob = (Mob)attacker;
					if(mob.getTarget() != null && mob.getTarget().equals(victim))
						mob.setTarget(null);
				}
				
				ci.setReturnValue(false);
				ci.cancel();
			}
	}
}
