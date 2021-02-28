package com.lying.variousoddities.entity.passive;

import com.lying.variousoddities.entity.AbstractCrab;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.world.World;

public class EntityCrab extends AbstractCrab
{
	public EntityCrab(EntityType<? extends EntityCrab> typeIn, World worldIn)
	{
		super(typeIn, worldIn);
	}
	
    public static AttributeModifierMap.MutableAttribute getAttributes()
    {
        return MobEntity.func_233666_p_()
        		.createMutableAttribute(Attributes.MAX_HEALTH, 9.0D)
        		.createMutableAttribute(Attributes.ARMOR, 4.0D)
        		.createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.25D)
        		.createMutableAttribute(Attributes.ATTACK_DAMAGE, 1.0D);
    }
    
    public boolean isNoDespawnRequired(){ return true; }
}
