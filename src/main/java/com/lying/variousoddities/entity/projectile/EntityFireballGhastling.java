package com.lying.variousoddities.entity.projectile;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class EntityFireballGhastling extends SmallFireball
{
	public EntityFireballGhastling(EntityType<? extends EntityFireballGhastling> typeIn, Level worldIn)
	{
		super(typeIn, worldIn);
	}
	
	public EntityFireballGhastling(Level worldIn, LivingEntity shooter, double accelX, double accelY, double accelZ)
	{
		super(worldIn, shooter, accelX, accelY, accelZ);
	}
	
	protected void onEntityHit(EntityHitResult result)
	{
		if(!getLevel().isClientSide)
		{
			Entity hitEntity = result.getEntity();
			if(!hitEntity.fireImmune())
			{
				Entity shooter = getResponsibleEntity();
				int i = hitEntity.getRemainingFireTicks();
				hitEntity.setSecondsOnFire(3);
				if(!hitEntity.hurt(DamageSource.fireball(this, shooter), 2.0F))
					hitEntity.setRemainingFireTicks(i);
				else if(shooter instanceof LivingEntity)
					doEnchantDamageEffects((LivingEntity)shooter, hitEntity);
			}
		}
	}
	
	protected void onHitBlock(BlockHitResult result)
	{
		super.onHitBlock(result);
		if(!getLevel().isClientSide)
		{
			getLevel().explode((Entity)null, this.getX(), this.getY(), this.getZ(), 0.5F, false, Explosion.BlockInteraction.NONE);
			remove(RemovalReason.KILLED);
		}
	}
	
	protected void func_230299_a_(BlockHitResult result)
	{
		BlockState blockstate = this.level.getBlockState(result.getBlockPos());
		blockstate.onProjectileHit(this.level, blockstate, result, this);
	}
}
