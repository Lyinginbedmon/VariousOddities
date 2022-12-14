package com.lying.variousoddities.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.condition.Conditions;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

@Mixin(PathNavigation.class)
public class PathNavigatorMixin
{
	@Shadow
	protected Mob mob;
	
	@Shadow
	public boolean isInProgress() { return false; }
	
	@Shadow
	public Path getPath() { return null; }
	
	@Shadow
	public void stop() { }
	
	@Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
	public void tick(final CallbackInfo ci)
	{
		LivingData data = LivingData.forEntity(mob);
		if(data == null || !isInProgress())
			return;
		
		List<LivingEntity> terrorisers = data.getMindControlled(Conditions.AFRAID.get(), 8D);
		terrorisers.removeIf((terroriser) -> { return terroriser.distanceTo(mob) > 8D || !mob.hasLineOfSight(terroriser); });
		if(terrorisers.isEmpty())
			return;
		
		Vec3 currentPos = mob.position();
		Vec3 nodePos = getPath().getNextEntityPos(mob);
		Vec3 direction = nodePos.subtract(currentPos).normalize();
		terrorisers.removeIf((terroriser) -> { return terroriser.distanceToSqr(currentPos) < terroriser.distanceToSqr(currentPos.add(direction)); });
		
		if(!terrorisers.isEmpty())
			stop();
	}
}
