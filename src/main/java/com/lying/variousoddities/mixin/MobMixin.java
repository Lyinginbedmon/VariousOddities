package com.lying.variousoddities.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.init.VOMobEffects;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketMobLoseTrack;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.AbilityVision;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.world.savedata.ScentsManager;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(Mob.class)
public class MobMixin extends LivingEntityMixin 
{
	@Inject(method = "playAmbientSound()V", at = @At("HEAD"), cancellable = true)
	public void playAmbientSound(final CallbackInfo ci)
	{
		Mob entity = (Mob)(Object)this;
		if(entity.hasEffect(VOMobEffects.SILENCED))
			ci.cancel();
	}
	
	@Inject(method = "playHurtSound", at = @At("HEAD"), cancellable = true)
	public void playHurtSound(final CallbackInfo ci)
	{
		Mob entity = (Mob)(Object)this;
		if(entity.hasEffect(VOMobEffects.SILENCED))
			ci.cancel();
	}
	
	private int ticksSinceLastSeen = 0;
	
	@Inject(method = "updateAITasks()V", at = @At("HEAD"), cancellable = true)
	public void mobAIHalting(final CallbackInfo ci)
	{
		LivingEntity entity = (LivingEntity)(Object)this;
		// Mobs paralysed or dazed do not update their AI tasks
		if(VOMobEffects.isParalysed(entity) || entity.hasEffect(VOMobEffects.DAZED))
			ci.cancel();
		
		// Mobs unable to detect their attack target beyond arm's length will lose interest
		Mob mob = (Mob)entity;
		LivingEntity attackTarget = mob.getTarget();
		if(attackTarget != null)
		{
			AbilityVision.ignoreForTargeted = false;
			double visibility = attackTarget.getVisibilityPercent(mob);
			if(visibility < 1D && mob.distanceToSqr(attackTarget) > mob.getAttributeValue(Attributes.FOLLOW_RANGE) * visibility)
			{
				if(ticksSinceLastSeen++ >= Reference.Values.TICKS_PER_SECOND * 10)
					loseTrackOf(attackTarget, mob);
			}
			else
				ticksSinceLastSeen = 0;
		}
		else
			ticksSinceLastSeen = 0;
	}
	
	private static void loseTrackOf(LivingEntity targetEntity, Mob mobEntity)
	{
		mobEntity.setTarget(null);
		mobEntity.getNavigation().stop();
		if(targetEntity.getType() == EntityType.PLAYER && !mobEntity.getLevel().isClientSide)
			PacketHandler.sendTo((ServerPlayer)targetEntity, new PacketMobLoseTrack());
	}
	
	private int scentTimer = -1;
	private Vec3 prevScentPos = null;
	
	@Inject(method = "updateAITasks()V", at = @At("HEAD"), cancellable = true)
	public void updateAITasks(final CallbackInfo ci)
	{
		Mob entity = (Mob)(Object)this;
		Level world = entity.getLevel();
		if(world.isClientSide) return;
		
		if(entity.isPersistenceRequired() || world.getNearestPlayer(entity, 64D) != null)
		{
			RandomSource rand = world.random;
			if(scentTimer < 0)
				resetScentTimer(rand);
			else if(--scentTimer == 0)
			{
				Vec3 pos = entity.position();
				if(prevScentPos == null)
					prevScentPos = pos;
				else
				{
					ScentsManager manager = ScentsManager.get(world);
					
					List<EnumCreatureType> types = EnumCreatureType.getCreatureTypes(entity);
					types.removeIf(EnumCreatureType.IS_SUBTYPE);
					if(types.isEmpty()) return;
					
					manager.addScentMarker(pos, prevScentPos, types.get(0));
					resetScentTimer(rand);
					
					prevScentPos = pos;
				}
			}
		}
	}
	
	private void resetScentTimer(RandomSource rand)
	{
		scentTimer = (Reference.Values.TICKS_PER_SECOND * 2) + rand.nextInt(Reference.Values.TICKS_PER_SECOND);
	}
}
