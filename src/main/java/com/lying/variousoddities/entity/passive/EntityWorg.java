package com.lying.variousoddities.entity.passive;

import java.util.Random;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class EntityWorg extends TameableEntity
{
	public EntityWorg(EntityType<? extends EntityWorg> p_i48574_1_, World p_i48574_2_)
	{
		super(p_i48574_1_, p_i48574_2_);
	}
	
	public AgeableEntity func_241840_a(ServerWorld arg0, AgeableEntity arg1)
	{
		return null;
	}
	
    public static AttributeModifierMap.MutableAttribute getAttributes()
    {
        return MobEntity.func_233666_p_()
        		.createMutableAttribute(Attributes.MAX_HEALTH, 30.0D)
        		.createMutableAttribute(Attributes.ARMOR, 4.0D)
        		.createMutableAttribute(Attributes.MOVEMENT_SPEED, (double)0.3F)
        		.createMutableAttribute(Attributes.ATTACK_DAMAGE, 10.0D);
    }
	
    public static boolean canSpawnAt(EntityType<? extends MobEntity> animal, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
        return world.getDifficulty() != Difficulty.PEACEFUL && CreatureEntity.canSpawnOn(animal, world, reason, pos, random);
    }
}
