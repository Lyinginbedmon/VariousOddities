package com.lying.variousoddities.entity.hostile;

import java.util.Random;

import com.lying.variousoddities.entity.EntityOddity;
import com.lying.variousoddities.init.VODamageSource;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

public class EntityMindFlayer extends EntityOddity
{
	public EntityMindFlayer(EntityType<? extends EntityOddity> type, World worldIn)
	{
		super(type, worldIn);
	}
	
    public static boolean canSpawnAt(EntityType<? extends MobEntity> animal, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
        return CreatureEntity.canSpawnOn(animal, world, reason, pos, random);
    }

	/** Returns true if the given itemstack is a wearable skull or head item */
    public static boolean isHead(ItemStack input)
    {
		return MobEntity.getSlotForItemStack(input) == EquipmentSlotType.HEAD && Tags.Items.HEADS.contains(input.getItem());
    }
    
    public boolean hasHead()
    {
    	return isHead(getItemStackFromSlot(EquipmentSlotType.HEAD));
    }
    
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
    	if(hasHead() && !(source == DamageSource.OUT_OF_WORLD || source == VODamageSource.BLUDGEON))
	    	if(source.getImmediateSource() != null && source.getImmediateSource() == source.getTrueSource() && source.getImmediateSource() instanceof LivingEntity)
	    	{
	    		LivingEntity ent = (LivingEntity)source.getImmediateSource();
	    		RayTraceResult ray = ent.pick(ent.getDistance(this) + 10D, 0F, false);	
	    		if(ray.getType() == Type.ENTITY)
	    		{
	    			EntityRayTraceResult entityRay = (EntityRayTraceResult)ray;
	    			if(entityRay.getEntity() == this)
	    			{
	    				Vector3d hit = ray.getHitVec().subtract(getPosX(), getPosY(), getPosZ());
	    				double hitY = hit.y;
	    				if(hitY >= this.getHeight() * 0.888)
	    				{
	    					// Head hit!
	    					ItemStack stack = getItemStackFromSlot(EquipmentSlotType.HEAD);
	    					if(stack.isDamageable())
	    						stack.damageItem((int)amount * 3, ent, (player) -> {});
	    					else if(this.getRNG().nextInt(20) == 0)
	    					{
	    						entityDropItem(stack);
	    						setItemStackToSlot(EquipmentSlotType.HEAD, ItemStack.EMPTY);
	    					}
	    				}
	    			}
	    		}
	    	}
    	return super.attackEntityFrom(source, amount);
    }
}
