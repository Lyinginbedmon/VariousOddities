package com.lying.variousoddities.entity.hostile;

import java.util.Random;

import javax.annotation.Nullable;

import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.entity.AbstractRat;
import com.lying.variousoddities.entity.ai.group.EntityGroup;
import com.lying.variousoddities.entity.ai.group.EntityGroupRat;
import com.lying.variousoddities.entity.ai.group.GroupHandler;
import com.lying.variousoddities.entity.passive.EntityRat;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EntityRatGiant extends AbstractRat
{
	public EntityRatGiant(EntityType<? extends EntityRatGiant> type, Level worldIn)
	{
		super(type, worldIn, 1);
	}
	
    public static AttributeModifierMap.MutableAttribute getAttributes()
    {
        return Mob.func_233666_p_()
        		.createMutableAttribute(Attributes.MAX_HEALTH, 20.0D)
        		.createMutableAttribute(Attributes.ARMOR, 0.0D)
        		.createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.25D)
        		.createMutableAttribute(Attributes.ATTACK_DAMAGE, 4.0D);
    }
	
    public static boolean canSpawnAt(EntityType<? extends Mob> animal, Level world, SpawnReason reason, BlockPos pos, Random random)
    {
        return world.getDifficulty() != Difficulty.PEACEFUL && world.getLightEmission(pos) < 8 && AbstractRat.canSpawnAt(animal, world, reason, pos, random);
    }
    
    public void registerGoals()
    {
    	super.registerGoals();
    	
    	if(ConfigVO.MOBS.aiSettings.isOddityAIEnabled(getType()))
    	{
		    this.targetSelector.addGoal(1, new HurtByTargetGoal(this, AbstractRat.class));
	        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Ocelot>(this, Ocelot.class, true));
	        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Cat>(this, Cat.class, true));
	        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>(this, Player.class, true));
    	}
    }
    
	/**
	 *  1 in 5 giant rats are plague rats and deal poison damage.<br>
	 *  The rest are brown rats.
	 */
	public int getRandomBreed()
	{
		return this.getRandom().nextInt(5) == 0 ? 3 : 2;
	}
	
	protected EntitySize getStandingSize()
	{
		return EntitySize.fixed(0.9F, 1.3F);
	}
	
	protected EntitySize getCrouchingSize()
	{
		return EntitySize.fixed(0.9F, 0.5F);
	}
	
	public void setTarget(@Nullable LivingEntity entitylivingbaseIn)
	{
		super.setTarget(entitylivingbaseIn);
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
    
    @Nullable
    public ILivingEntityData onInitialSpawn(ServerLevel worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundTag dataTag)
    {
    	super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    	if(reason == SpawnReason.SPAWNER)
	    	for(int i=0; i<getRandom().nextInt(4); i++)
	    	{
	    		EntityRat rat = VOEntities.RAT.create(getLevel());
	    		rat.setMinion(true);
	    		rat.setBreed(getBreed());
	    		rat.setLocationAndAngles(getX(), getY(), getZ(), getRandom().nextFloat() * 360F, 0F);
	    		getLevel().addFreshEntity(rat);
	    	}
		return spawnDataIn;
    }
}
