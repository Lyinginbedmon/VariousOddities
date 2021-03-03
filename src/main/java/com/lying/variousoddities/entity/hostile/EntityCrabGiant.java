package com.lying.variousoddities.entity.hostile;

import java.util.Random;

import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.entity.AbstractCrab;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
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
	
    public void registerGoals()
    {
    	super.registerGoals();
    	
    	if(ConfigVO.MOBS.aiSettings.isOddityAIEnabled(VOEntities.CRAB_GIANT))
    		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<PlayerEntity>(this, PlayerEntity.class, true));
    }
    
    public static boolean canSpawnAt(EntityType<? extends MobEntity> animal, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
        return world.getDifficulty() != Difficulty.PEACEFUL && AbstractCrab.canSpawnAt(animal, world, reason, pos, random);
    }
}
