package com.lying.variousoddities.entity.hostile;

import java.util.Random;

import javax.annotation.Nullable;

import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.entity.AbstractRat;
import com.lying.variousoddities.entity.ai.group.EntityGroup;
import com.lying.variousoddities.entity.ai.group.EntityGroupRat;
import com.lying.variousoddities.entity.ai.group.GroupHandler;

import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class EntityRatGiant extends AbstractRat
{
	public EntityRatGiant(EntityType<? extends EntityRatGiant> type, World worldIn)
	{
		super(type, worldIn, 1);
	}
	
    public static AttributeModifierMap.MutableAttribute getAttributes()
    {
        return MobEntity.func_233666_p_()
        		.createMutableAttribute(Attributes.MAX_HEALTH, 20.0D)
        		.createMutableAttribute(Attributes.ARMOR, 0.0D)
        		.createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.25D)
        		.createMutableAttribute(Attributes.ATTACK_DAMAGE, 4.0D);
    }
	
    public static boolean canSpawnAt(EntityType<? extends MobEntity> animal, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
        return world.getDifficulty() != Difficulty.PEACEFUL && world.getLight(pos) <= 8 && AbstractRat.canSpawnAt(animal, world, reason, pos, random);
    }
    
    public void registerGoals()
    {
    	super.registerGoals();
    	
    	if(ConfigVO.MOBS.aiSettings.isOddityAIEnabled(getType()))
    	{
		    this.targetSelector.addGoal(1, new HurtByTargetGoal(this, AbstractRat.class));
	        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<OcelotEntity>(this, OcelotEntity.class, true));
	        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<CatEntity>(this, CatEntity.class, true));
	        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<PlayerEntity>(this, PlayerEntity.class, true));
    	}
    }
    
	/**
	 *  1 in 5 giant rats are plague rats and deal poison damage.<br>
	 *  The rest are brown rats.
	 */
	public int getRandomBreed()
	{
		return this.getRNG().nextInt(5) == 0 ? 3 : 2;
	}
	
	protected EntitySize getStandingSize()
	{
		return EntitySize.fixed(0.9F, 1.3F);
	}
	
	protected EntitySize getCrouchingSize()
	{
		return EntitySize.fixed(0.9F, 0.5F);
	}
	
	public void setAttackTarget(@Nullable LivingEntity entitylivingbaseIn)
	{
		super.setAttackTarget(entitylivingbaseIn);
		if(entitylivingbaseIn != null)
		{
			EntityGroup group = GroupHandler.getEntityMemberGroup(this);
			if(group == null)
			{
				group = new EntityGroupRat(this);
				group.addTarget(entitylivingbaseIn);
				GroupHandler.addGroup(group);
			}
		}
	}
}
