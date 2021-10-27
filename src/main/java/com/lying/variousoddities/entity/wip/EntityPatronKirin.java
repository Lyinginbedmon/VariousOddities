package com.lying.variousoddities.entity.wip;

import java.util.Random;

import com.lying.variousoddities.entity.EntityOddityAgeable;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IChargeableMob;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(
   value = Dist.CLIENT,
   _interface = IChargeableMob.class
)
public class EntityPatronKirin extends EntityOddityAgeable implements IChargeableMob
{
	public EntityPatronKirin(EntityType<? extends EntityOddityAgeable> type, World worldIn)
	{
		super(type, worldIn);
	}
	
    public static boolean canSpawnAt(EntityType<? extends MobEntity> animal, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
        return CreatureEntity.canSpawnOn(animal, world, reason, pos, random);
    }
    
	public boolean isCharged(){ return true; }
    
    public boolean isNoDespawnRequired(){ return true; }
	
	public AgeableEntity func_241840_a(ServerWorld p_241840_1_, AgeableEntity p_241840_2_)
	{
		return null;
	}
}
