package com.lying.variousoddities.entity.passive;

import com.lying.variousoddities.entity.AbstractCrab;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class EntityCrab extends AbstractCrab
{
	public EntityCrab(EntityType<? extends EntityCrab> typeIn, Level worldIn)
	{
		super(typeIn, worldIn);
	}

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
        		.add(Attributes.MAX_HEALTH, 9.0D)
        		.add(Attributes.ARMOR, 4.0D)
        		.add(Attributes.MOVEMENT_SPEED, 0.25D)
        		.add(Attributes.ATTACK_DAMAGE, 1.0D);
    }
    
    public boolean isNoDespawnRequired(){ return true; }
}
