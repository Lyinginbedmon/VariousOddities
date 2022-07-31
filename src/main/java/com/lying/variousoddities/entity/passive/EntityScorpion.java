package com.lying.variousoddities.entity.passive;

import com.lying.variousoddities.entity.AbstractScorpion;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;

public class EntityScorpion extends AbstractScorpion
{
	public EntityScorpion(EntityType<? extends EntityScorpion> type, Level worldIn)
	{
		super(type, worldIn);
	}

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
        		.add(Attributes.MAX_HEALTH, 5.0D)
        		.add(Attributes.ATTACK_DAMAGE, 1.5D);
    }
	
    public boolean checkSpawnRules(LevelAccessor world, MobSpawnType reason)
    {
    	return world.getHeight(Heightmap.Types.WORLD_SURFACE, blockPosition().getX(), blockPosition().getZ()) <= blockPosition().getY();
    }
    
    public boolean isNoDespawnRequired(){ return true; }
	
	public AbstractScorpion createBaby(Level worldIn)
	{
		EntityScorpion baby = VOEntities.SCORPION.create(worldIn);
		baby.setBabies(false);
		baby.setBreed(this.getScorpionType());
		return baby;
	}
}
