package com.lying.variousoddities.entity.passive;

import java.util.Random;

import com.lying.variousoddities.entity.AbstractScorpion;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;

public class EntityScorpion extends AbstractScorpion
{
	public EntityScorpion(EntityType<? extends EntityScorpion> type, World worldIn)
	{
		super(type, worldIn);
	}
	
    public static AttributeModifierMap.MutableAttribute getAttributes()
    {
        return MobEntity.func_233666_p_()
        		.createMutableAttribute(Attributes.MAX_HEALTH, 5.0D)
        		.createMutableAttribute(Attributes.ATTACK_DAMAGE, 1.5D);
    }
	
    public static boolean canSpawnAt(EntityType<?> animal, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
    	return world.getHeight(Heightmap.Type.WORLD_SURFACE, pos).getY() <= pos.getY();
    }
    
    public boolean isNoDespawnRequired(){ return true; }
	
	public AbstractScorpion createBaby(World worldIn)
	{
		EntityScorpion baby = VOEntities.SCORPION.create(worldIn);
		baby.setBabies(false);
		baby.setBreed(this.getScorpionType());
		return baby;
	}
}
