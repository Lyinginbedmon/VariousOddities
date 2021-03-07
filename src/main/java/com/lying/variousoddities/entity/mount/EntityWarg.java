package com.lying.variousoddities.entity.mount;

import com.lying.variousoddities.entity.AbstractGoblinWolf;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class EntityWarg extends AbstractGoblinWolf
{
	public EntityWarg(EntityType<? extends EntityWarg> type, World worldIn)
	{
		super(type, worldIn);
	}
	
    public static AttributeModifierMap.MutableAttribute getAttributes()
    {
        return MobEntity.func_233666_p_()
        		.createMutableAttribute(Attributes.MAX_HEALTH, 40.0D)
        		.createMutableAttribute(Attributes.ARMOR, 7.0D)
        		.createMutableAttribute(Attributes.MOVEMENT_SPEED, (double)0.3002F)
        		.createMutableAttribute(Attributes.ATTACK_DAMAGE, 12.0D);
    }
	
	public AgeableEntity func_241840_a(ServerWorld arg0, AgeableEntity arg1)
	{
		return null;
	}
}
