package com.lying.variousoddities.entity.passive;

import com.lying.variousoddities.entity.AbstractGoblinWolf;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class EntityWorg extends AbstractGoblinWolf
{
	public EntityWorg(EntityType<? extends EntityWorg> type, World worldIn)
	{
		super(type, worldIn);
	}
	
    public static AttributeModifierMap.MutableAttribute getAttributes()
    {
        return MobEntity.func_233666_p_()
        		.createMutableAttribute(Attributes.MAX_HEALTH, 30.0D)
        		.createMutableAttribute(Attributes.ARMOR, 4.0D)
        		.createMutableAttribute(Attributes.MOVEMENT_SPEED, (double)0.3F)
        		.createMutableAttribute(Attributes.ATTACK_DAMAGE, 10.0D);
    }
    
    /**
     * Returns the volume for the sounds this mob makes.
     */
    protected float getSoundVolume()
    {
        return 0.4F;
    }
	
	public AgeableEntity func_241840_a(ServerWorld arg0, AgeableEntity arg1)
	{
		return null;
	}
}
