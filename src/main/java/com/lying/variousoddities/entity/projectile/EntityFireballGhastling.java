package com.lying.variousoddities.entity.projectile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class EntityFireballGhastling extends SmallFireballEntity 
{
	public EntityFireballGhastling(EntityType<? extends EntityFireballGhastling> typeIn, World worldIn)
	{
		super(typeIn, worldIn);
	}
	
	public EntityFireballGhastling(World worldIn, LivingEntity shooter, double accelX, double accelY, double accelZ)
	{
		super(worldIn, shooter, accelX, accelY, accelZ);
	}
	
	protected void onEntityHit(EntityRayTraceResult result)
	{
		if(!getEntityWorld().isRemote)
		{
			Entity hitEntity = result.getEntity();
			if(!hitEntity.isImmuneToFire())
			{
				Entity shooter = func_234616_v_();
				int i = hitEntity.getFireTimer();
				hitEntity.setFire(3);
				if(!hitEntity.attackEntityFrom(DamageSource.func_233547_a_(this, shooter), 2.0F))
						hitEntity.forceFireTicks(i);
				else if(shooter instanceof LivingEntity)
					applyEnchantments((LivingEntity)shooter, hitEntity);
			}
		}
	}
	
	protected void onImpact(RayTraceResult result)
	{
		super.onImpact(result);
		if(!getEntityWorld().isRemote)
		{
			getEntityWorld().createExplosion((Entity)null, this.getPosX(), this.getPosY(), this.getPosZ(), 0.5F, false, Explosion.Mode.NONE);
			remove();
		}
	}
	
	protected void func_230299_a_(BlockRayTraceResult result)
	{
		BlockState blockstate = this.world.getBlockState(result.getPos());
		blockstate.onProjectileCollision(this.world, blockstate, result, this);
	}
}
