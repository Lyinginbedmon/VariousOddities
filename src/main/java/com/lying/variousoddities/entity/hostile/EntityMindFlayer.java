package com.lying.variousoddities.entity.hostile;

import java.util.Random;

import com.lying.variousoddities.entity.EntityOddity;
import com.lying.variousoddities.init.VODamageSource;

import net.minecraft.core.BlockPos;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;

public class EntityMindFlayer extends EntityOddity
{
	public EntityMindFlayer(EntityType<? extends EntityOddity> type, Level worldIn)
	{
		super(type, worldIn);
	}
	
	protected void registerGoals()
	{
		super.registerGoals();
		this.goalSelector.addGoal(0, new SwimGoal(this));
		this.goalSelector.addGoal(8, new RandomWalkingGoal(this, 0.6D));
		this.goalSelector.addGoal(9, new LookAtGoal(this, Player.class, 15.0F, 1.0F));
		this.goalSelector.addGoal(10, new LookAtGoal(this, Mob.class, 15.0F));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
	}
	
	public boolean checkSpawnRules(LevelAccessor world, MobSpawnType reason)
    {
        return world.getDifficulty() != Difficulty.PEACEFUL;
    }
	
	/** Returns true if the given itemstack is a wearable skull or head item */
    public static boolean isHead(ItemStack input)
    {
		return input.getItem().getEquipmentSlot(input) == EquipmentSlot.HEAD && input.is(Tags.Items.HEADS);
    }
    
    public boolean hasHead()
    {
    	return isHead(getItemBySlot(EquipmentSlot.HEAD));
    }
    
    public boolean hurt(DamageSource source, float amount)
    {
    	if(hasHead() && !(source == DamageSource.OUT_OF_WORLD || source == VODamageSource.BLUDGEON))
	    	if(source.getDirectEntity() != null && source.getDirectEntity() == source.getEntity() && source.getDirectEntity() instanceof LivingEntity)
	    	{
	    		LivingEntity ent = (LivingEntity)source.getDirectEntity();
	    		HitResult ray = ent.pick(ent.distanceToSqr(this) + 10D, 0F, false);	
	    		if(ray.getType() == Type.ENTITY)
	    		{
	    			EntityHitResult entityRay = (EntityHitResult)ray;
	    			if(entityRay.getEntity() == this)
	    			{
	    				Vec3 hit = ray.getHitVec().subtract(getPosX(), getPosY(), getPosZ());
	    				double hitY = hit.y;
	    				if(hitY >= this.getBbHeight() * 0.888)
	    				{
	    					// Head hit!
	    					ItemStack stack = getItemBySlot(EquipmentSlot.HEAD);
	    					if(stack.isDamageableItem())
	    						stack.hurtAndBreak((int)amount * 3, ent, (player) -> {});
	    					else if(this.getRandom().nextInt(20) == 0)
	    					{
	    						entityDropItem(stack);
	    						setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
	    					}
	    				}
	    			}
	    		}
	    	}
    	return super.hurt(source, amount);
    }
}
