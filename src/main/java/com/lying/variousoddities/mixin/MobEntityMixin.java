package com.lying.variousoddities.mixin;

import java.util.List;
import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.world.savedata.ScentsManager;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
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
	
	@Inject(method = "updateAITasks", at = @At("HEAD"), cancellable = true)
	public void isMobParalysed(final CallbackInfo ci)
	{
		LivingEntity entity = (LivingEntity)(Object)this;
		if(VOPotions.isParalysed(entity))
			ci.cancel();
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
