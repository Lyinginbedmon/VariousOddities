package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.Entity;
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
}
