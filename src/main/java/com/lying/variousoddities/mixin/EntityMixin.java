package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
	
	@Inject(method = "pushOutOfBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;<init>(DDD)V"), cancellable = true)
	protected void pushOutOfBlocks(double x, double y, double z, CallbackInfo ci)
	{
		System.out.println("Pushing out of blocks");
		Entity entity = (Entity)(Object)this;
		if(entity instanceof LivingEntity)
		{
			TypesManager manager = TypesManager.get(entity.getEntityWorld());
			if(manager.isMobOfType((LivingEntity)entity, EnumCreatureType.INCORPOREAL))
				ci.cancel();
		}
	}
}
