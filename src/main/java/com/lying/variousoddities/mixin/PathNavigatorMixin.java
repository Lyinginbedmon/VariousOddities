package com.lying.variousoddities.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.condition.Conditions;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.math.vector.Vector3d;

@Mixin(PathNavigator.class)
public class PathNavigatorMixin
{
	@Shadow
	protected MobEntity entity;
	
	@Shadow
	public boolean hasPath() { return false; }
	
	@Shadow
	public Path getPath() { return null; }
	
	@Shadow
	public void clearPath() { }
	
	@Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
	public void tick(final CallbackInfo ci)
	{
		LivingData data = LivingData.forEntity(entity);
		if(data == null || !hasPath())
			return;
		
		List<LivingEntity> terrorisers = data.getMindControlled(Conditions.AFRAID, 8D);
		terrorisers.removeIf((terroriser) -> { return terroriser.getDistance(entity) > 8D || !entity.getEntitySenses().canSee(terroriser); });
		if(terrorisers.isEmpty())
			return;
		
		Vector3d currentPos = entity.getPositionVec();
		Vector3d nodePos = getPath().getPosition(entity);
		Vector3d direction = nodePos.subtract(currentPos).normalize();
		terrorisers.removeIf((terroriser) -> { return terroriser.getDistanceSq(currentPos) < terroriser.getDistanceSq(currentPos.add(direction)); });
		
		if(!terrorisers.isEmpty())
			clearPath();
	}
}
