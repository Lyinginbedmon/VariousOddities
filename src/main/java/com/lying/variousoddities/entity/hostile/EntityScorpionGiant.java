package com.lying.variousoddities.entity.hostile;

import com.lying.variousoddities.entity.AbstractScorpion;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class EntityScorpionGiant extends AbstractScorpion
{
	public EntityScorpionGiant(EntityType<? extends EntityScorpionGiant> type, World worldIn)
	{
		super(type, worldIn);
	}
	
    public static AttributeModifierMap.MutableAttribute getAttributes()
    {
        return MobEntity.func_233666_p_()
        		.createMutableAttribute(Attributes.MAX_HEALTH, 60.0D)
        		.createMutableAttribute(Attributes.ARMOR, 6.0D)
        		.createMutableAttribute(Attributes.ATTACK_DAMAGE, 9.0D);
    }
    
    public void registerGoals()
    {
    	super.registerGoals();
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<PlayerEntity>(this, PlayerEntity.class, true));
    }
	
	public AbstractScorpion createBaby(World worldIn)
	{
		EntityScorpionGiant baby = VOEntities.SCORPION_GIANT.create(worldIn);
		baby.setBabies(false);
		baby.setBreed(this.getScorpionType());
		return baby;
	}
}
