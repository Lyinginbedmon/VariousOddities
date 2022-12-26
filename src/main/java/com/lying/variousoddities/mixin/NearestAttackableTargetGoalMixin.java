package com.lying.variousoddities.mixin;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.lying.variousoddities.api.entity.IFactionMob;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.faction.FactionReputation.EnumAttitude;
import com.lying.variousoddities.faction.FactionReputation.EnumInteraction;
import com.lying.variousoddities.world.savedata.FactionManager;
import com.lying.variousoddities.world.savedata.FactionManager.Faction;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;

@Mixin(NearestAttackableTargetGoal.class)
public class NearestAttackableTargetGoalMixin
{
	@ModifyVariable(method = "<init>(Lnet/minecraft/entity/Mob;Ljava/lang/Class;IZZLjava/util/function/Predicate;)V", at = @At("HEAD"), ordinal = 0)
	private static Predicate<LivingEntity> modifyPredicate(Predicate<LivingEntity> targetPredicate, Mob goalOwnerIn, Class<? extends LivingEntity> classIn)
	{
		if(targetPredicate == null)
			return getPredicate(goalOwnerIn);
		else
		    return targetPredicate.and(getPredicate(goalOwnerIn));
	}
	
	private static Predicate<LivingEntity> getPredicate(LivingEntity goalOwnerIn)
	{
		return new Predicate<LivingEntity>()
		{
			public boolean test(LivingEntity target)
			{
				// Undead mobs do not target other undead
				if(goalOwnerIn.canChangeDimensions() && goalOwnerIn.getMobType() == MobType.UNDEAD)
					if(target.getMobType() == MobType.UNDEAD)
						return false;
				
				// Mobs do not attack creatures that have mind-controlled them somehow
				LivingData mobData = LivingData.getCapability(goalOwnerIn);
				if(mobData != null && mobData.isTargetingHindered(target))
					return false;
				
				// Faction mobs do not attack mobs with good reputation
				if(goalOwnerIn instanceof IFactionMob)
				{
					FactionManager factionManager = FactionManager.get(target.getLevel());
					Faction ownerFaction = factionManager.getFaction(goalOwnerIn);
					if(ownerFaction != null)
						if(target.getType() == EntityType.PLAYER)
						{
							PlayerData data = PlayerData.getCapability((Player)target);
							if(data != null)
							{
								int reputation = data.reputation.getReputation(ownerFaction.name);
								if(reputation == Integer.MIN_VALUE)
								{
									data.reputation.setReputation(ownerFaction.name, ownerFaction.startingRep);
									reputation = ownerFaction.startingRep;
								}
								return EnumAttitude.fromRep(reputation).allowsInteraction(EnumInteraction.ATTACK);
							}
						}
						else if(target instanceof IFactionMob)
						{
							Faction inputFaction = factionManager.getFaction(target);
							if(inputFaction != null)
								return ownerFaction.relationWith(inputFaction.name).allowsInteraction(EnumInteraction.ATTACK);
						}
				}
				return true;
			}
		};
	}
}
