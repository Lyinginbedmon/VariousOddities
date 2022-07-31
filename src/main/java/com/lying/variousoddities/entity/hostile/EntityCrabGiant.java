package com.lying.variousoddities.entity.hostile;

import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.entity.AbstractCrab;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class EntityCrabGiant extends AbstractCrab
{
	public EntityCrabGiant(EntityType<? extends EntityCrabGiant> typeIn, Level worldIn)
	{
		super(typeIn, worldIn);
	}

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
        		.add(Attributes.MAX_HEALTH, 30.0D)
        		.add(Attributes.ARMOR, 10.0D)
        		.add(Attributes.MOVEMENT_SPEED, 0.24D)
        		.add(Attributes.ATTACK_DAMAGE, 6.0D);
    }
	
    public void registerGoals()
    {
    	super.registerGoals();
    	
    	if(ConfigVO.MOBS.aiSettings.isOddityAIEnabled(VOEntities.CRAB_GIANT))
    		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>(this, Player.class, true));
    }
    
    public boolean checkSpawnRules(LevelAccessor world, MobSpawnType reason)
    {
        return world.getDifficulty() != Difficulty.PEACEFUL && super.checkSpawnRules(world, reason);
    }
}
