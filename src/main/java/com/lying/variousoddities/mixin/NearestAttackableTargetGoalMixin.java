package com.lying.variousoddities.mixin;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.lying.variousoddities.world.savedata.TypesManager;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;

@Mixin(NearestAttackableTargetGoal.class)
public class NearestAttackableTargetGoalMixin
{
	@ModifyVariable(method = "<init>(Lnet/minecraft/entity/MobEntity;Ljava/lang/Class;IZZLjava/util/function/Predicate;)V", at = @At("HEAD"), ordinal = 0)
	private static Predicate<LivingEntity> modifyPredicate(Predicate<LivingEntity> targetPredicate, MobEntity goalOwnerIn, Class<? extends LivingEntity> classIn)
	{
		if(targetPredicate == null)
		{
			return new Predicate<LivingEntity>()
			{
				public boolean test(LivingEntity target)
				{
					TypesManager manager = TypesManager.get(target.getEntityWorld());
					if(manager.isUndead(goalOwnerIn) && goalOwnerIn.isNonBoss())
						return !manager.isUndead(target);
					return true;
				}
			};
		}
		else
		    return targetPredicate.and(new Predicate<LivingEntity>()
			{
				public boolean test(LivingEntity target)
				{
					TypesManager manager = TypesManager.get(target.getEntityWorld());
					if(manager.isUndead(goalOwnerIn) && goalOwnerIn.isNonBoss())
						return !manager.isUndead(target);
					return true;
				}
			});
	}
}
