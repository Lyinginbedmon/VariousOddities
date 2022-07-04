package com.lying.variousoddities.mixin;

import java.util.List;
import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketMobLoseTrack;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.AbilityVision;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.world.savedata.ScentsManager;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

@Mixin(MobEntity.class)
public class MobEntityMixin extends LivingEntityMixin 
{
	@Inject(method = "playAmbientSound()V", at = @At("HEAD"), cancellable = true)
	public void playAmbientSound(final CallbackInfo ci)
	{
		MobEntity entity = (MobEntity)(Object)this;
		if(entity.isPotionActive(VOPotions.SILENCED))
			ci.cancel();
	}
	
	@Inject(method = "playHurtSound", at = @At("HEAD"), cancellable = true)
	public void playHurtSound(final CallbackInfo ci)
	{
		MobEntity entity = (MobEntity)(Object)this;
		if(entity.isPotionActive(VOPotions.SILENCED))
			ci.cancel();
	}
	
	private int ticksSinceLastSeen = 0;
	
	@Inject(method = "updateAITasks()V", at = @At("HEAD"), cancellable = true)
	public void mobAIHalting(final CallbackInfo ci)
	{
		LivingEntity entity = (LivingEntity)(Object)this;
		// Mobs paralysed or dazed do not update their AI tasks
		if(VOPotions.isParalysed(entity) || entity.isPotionActive(VOPotions.DAZED))
			ci.cancel();
		
		// Mobs unable to detect their attack target beyond arm's length will lose interest
		MobEntity mob = (MobEntity)entity;
		LivingEntity attackTarget = mob.getAttackTarget();
		if(attackTarget != null)
		{
			AbilityVision.ignoreForTargeted = false;
			double visibility = attackTarget.getVisibilityMultiplier(mob);
			if(visibility < 1D && mob.getDistance(attackTarget) > mob.getAttributeValue(Attributes.FOLLOW_RANGE) * visibility)
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
	
	private static void loseTrackOf(LivingEntity targetEntity, MobEntity mobEntity)
	{
		mobEntity.setAttackTarget(null);
		mobEntity.getNavigator().clearPath();
		if(targetEntity.getType() == EntityType.PLAYER && !mobEntity.getEntityWorld().isRemote)
			PacketHandler.sendTo((ServerPlayerEntity)targetEntity, new PacketMobLoseTrack());
	}
	
	private int scentTimer = -1;
	private Vector3d prevScentPos = null;
	
	@Inject(method = "updateAITasks()V", at = @At("HEAD"), cancellable = true)
	public void updateAITasks(final CallbackInfo ci)
	{
		MobEntity entity = (MobEntity)(Object)this;
		World world = entity.getEntityWorld();
		if(world.isRemote) return;
		
		if(entity.isNoDespawnRequired() || world.getClosestPlayer(entity, 64D) != null)
		{
			Random rand = world.rand;
			if(scentTimer < 0)
				resetScentTimer(rand);
			else if(--scentTimer == 0)
			{
				Vector3d pos = entity.getPositionVec();
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
	
	private void resetScentTimer(Random rand)
	{
		scentTimer = (Reference.Values.TICKS_PER_SECOND * 2) + rand.nextInt(Reference.Values.TICKS_PER_SECOND);
	}
}
