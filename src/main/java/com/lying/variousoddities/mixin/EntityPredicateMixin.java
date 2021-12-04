package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.capabilities.PlayerData;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;

@Mixin(EntityPredicate.class)
public class EntityPredicateMixin
{
	@Inject(method = "canTarget(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z", at = @At("RETURN"), cancellable = true)
	public void canTarget(LivingEntity attacker, LivingEntity victim, final CallbackInfoReturnable<Boolean> ci)
	{
		if(victim != null && victim.getType() == EntityType.PLAYER)
			if(!PlayerData.isPlayerNormalFunction(victim))
			{
				if(attacker instanceof MobEntity)
				{
					MobEntity mob = (MobEntity)attacker;
					if(mob.getAttackTarget() != null && mob.getAttackTarget().equals(victim))
						mob.setAttackTarget(null);
				}
				
				ci.setReturnValue(false);
				ci.cancel();
			}
	}
}
