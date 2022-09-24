package com.lying.variousoddities.entity.hostile;

import com.lying.variousoddities.entity.AbstractScorpion;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EntityScorpionGiant extends AbstractScorpion
{
	public EntityScorpionGiant(EntityType<? extends EntityScorpionGiant> type, Level worldIn)
	{
		super(type, worldIn);
	}

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
        		.add(Attributes.MAX_HEALTH, 60.0D)
        		.add(Attributes.ARMOR, 6.0D)
        		.add(Attributes.ATTACK_DAMAGE, 9.0D);
    }
    
    public void registerGoals()
    {
    	super.registerGoals();
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>(this, Player.class, true));
    }
	
	public AbstractScorpion createBaby(Level worldIn)
	{
		EntityScorpionGiant baby = VOEntities.SCORPION_GIANT.get().create(worldIn);
		baby.setBabies(false);
		baby.setBreed(this.getScorpionType());
		return baby;
	}
}
