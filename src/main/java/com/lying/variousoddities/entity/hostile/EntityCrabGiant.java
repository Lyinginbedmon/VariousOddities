package com.lying.variousoddities.entity.hostile;

import java.util.Random;

import com.lying.variousoddities.entity.AbstractCrab;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class EntityCrabGiant extends AbstractCrab
{
	public EntityCrabGiant(EntityType<? extends EntityCrabGiant> typeIn, World worldIn)
	{
		super(typeIn, worldIn);
	}
	
    public static AttributeModifierMap.MutableAttribute getAttributes()
    {
        return MobEntity.func_233666_p_()
        		.createMutableAttribute(Attributes.MAX_HEALTH, 30.0D)
        		.createMutableAttribute(Attributes.ARMOR, 10.0D)
        		.createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.24D)
        		.createMutableAttribute(Attributes.ATTACK_DAMAGE, 6.0D);
    }
	
    public static boolean canSpawnAt(EntityType<? extends MobEntity> animal, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
        return world.getDifficulty() != Difficulty.PEACEFUL && AbstractCrab.canSpawnAt(animal, world, reason, pos, random);
    }
}
