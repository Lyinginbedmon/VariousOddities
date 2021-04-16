package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.types.EnumCreatureType;
import com.lying.variousoddities.world.savedata.TypesManager;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

@Mixin(Entity.class)
public class EntityMixin extends CapabilityProviderMixin
{
	@Shadow public World world;
	
	@Shadow
	public final double getPosX()
	{
		return 0D;
	}
	
	@Shadow
	public final double getPosY()
	{
		return 0D;
	}
	
	@Shadow
	public final double getPosZ()
	{
		return 0D;
	}
	
	@Shadow
	public EntityDataManager getDataManager()
	{
		return null;
	}
	
	@Shadow
	public void setAir(int airIn){ }
	
	@Shadow
	public boolean isSprinting()
	{
		return false;
	}
	
	@Inject(method = "pushOutOfBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;<init>(DDD)V"), cancellable = true)
	public void incorporealPushOutOfBlock(double x, double y, double z, final CallbackInfo ci)
	{
		Entity ent = (Entity)(Object)this;
		if(ent instanceof LivingEntity && EnumCreatureType.canPhase(world, null, (LivingEntity)(Object)this))
			ci.cancel();
	}
	
	@Inject(method = "updateFallState", at = @At("HEAD"))
	public void incorporealFall(final CallbackInfo ci)
	{
		Entity ent = (Entity)(Object)this;
		if(ent instanceof LivingEntity && TypesManager.get(world).isMobOfType((LivingEntity)ent, EnumCreatureType.INCORPOREAL))
			ent.fallDistance = 0F;
	}
	
	@Inject(method = "isSteppingCarefully()Z", at = @At("HEAD"), cancellable = true)
	public void incorporealStepCarefully(final CallbackInfoReturnable<Boolean> ci)
	{
		Entity ent = (Entity)(Object)this;
		if(ent instanceof LivingEntity && TypesManager.get(world).isMobOfType((LivingEntity)ent, EnumCreatureType.INCORPOREAL) && !isSprinting())
			ci.setReturnValue(true);
	}
}
