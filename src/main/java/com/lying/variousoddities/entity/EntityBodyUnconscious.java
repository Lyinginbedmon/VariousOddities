package com.lying.variousoddities.entity;

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lying.variousoddities.init.VOEntities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class EntityBodyUnconscious extends AbstractBody
{
	public EntityBodyUnconscious(EntityType<? extends EntityBodyUnconscious> type, World worldIn)
	{
		super(type, worldIn);
	}
    
    public static boolean canSpawnAt(EntityType<?> animal, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
	    return true;
    }
	
	@Nullable
	public static EntityBodyUnconscious createCorpseFrom(@Nonnull LivingEntity living)
	{
		if(living == null) return null;
		EntityBodyUnconscious corpse = new EntityBodyUnconscious(VOEntities.BODY, living.getEntityWorld());
		corpse.copyFrom(living);
		return corpse;
	}
	
    public boolean isNoDespawnRequired(){ return true; }
}
