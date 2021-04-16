package com.lying.variousoddities.entity.passive;

import com.lying.variousoddities.entity.AbstractGoblinWolf;
import com.lying.variousoddities.entity.ai.passive.EntityAIWorgFetch;
import com.lying.variousoddities.entity.ai.passive.EntityAIWorgSpook;
import com.lying.variousoddities.entity.mount.EntityWarg;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.BreedGoal;
import net.minecraft.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class EntityWorg extends AbstractGoblinWolf
{
	private static final DataParameter<Boolean> SPOOKED = EntityDataManager.<Boolean>createKey(EntityWorg.class, DataSerializers.BOOLEAN);
	
	public EntityWorg(EntityType<? extends EntityWorg> type, World worldIn)
	{
		super(type, worldIn);
	}
	
	protected void registerData()
	{
		super.registerData();
		getDataManager().register(SPOOKED, false);
	}
	
    public static AttributeModifierMap.MutableAttribute getAttributes()
    {
        return MobEntity.func_233666_p_()
        		.createMutableAttribute(Attributes.MAX_HEALTH, 30.0D)
        		.createMutableAttribute(Attributes.ARMOR, 4.0D)
        		.createMutableAttribute(Attributes.MOVEMENT_SPEED, (double)0.3F)
        		.createMutableAttribute(Attributes.ATTACK_DAMAGE, 10.0D);
    }
    
	public void registerGoals()
	{
		super.registerGoals();
		
		this.goalSelector.addGoal(1, new EntityAIWorgSpook(this, 1.0D));
		this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
		this.goalSelector.addGoal(2, new EntityAIWorgFetch(this, 6D));
		this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
		
	}
    
	public void getAggressiveBehaviours()
	{
		this.addGeneticAI(3, new NearestAttackableTargetGoal<ChickenEntity>(this, ChickenEntity.class, true));
	}
	
    /**
     * Returns the volume for the sounds this mob makes.
     */
    protected float getSoundVolume()
    {
        return 0.4F;
    }
	
    public void spook(){ getDataManager().set(SPOOKED, true); }
    
    public boolean isSpooked(){ return getDataManager().get(SPOOKED).booleanValue(); }
    
    public void unSpook(){ getDataManager().set(SPOOKED, false); }
    
    public ActionResultType func_230254_b_(PlayerEntity player, Hand hand)
    {
    	ItemStack heldItem = player.getHeldItem(hand);
    	if(getEntityWorld().isRemote)
    	{
    		boolean flag = this.isOwner(player) || this.isTamed() || heldItem.getItem() == Items.BONE && !this.isTamed() && getAttackTarget() == null;
    		return flag ? ActionResultType.SUCCESS : ActionResultType.PASS;
    	}
    	else
    	{
	    	if(isTamed())
			{
				if(isFoodItem(heldItem) && getHealth() < getMaxHealth())
	    		{
	    			heal(heldItem.getItem().getFood().getHealing());
		    		if(!player.isCreative())
		    			heldItem.shrink(1);
		    		
		    		return ActionResultType.func_233537_a_(this.world.isRemote);
		    	}
				
				if(heldItem.getItem() == Items.BONE && getGrowingAge() == 0)
				{
	    			setInLove(player);
	    			
	    			if(!player.isCreative())
	    				heldItem.shrink(1);
	    			
		    		return ActionResultType.func_233537_a_(this.world.isRemote);
				}
				
				if(this.isOwner(player))
	    		{
					if(player.isSneaking())
					{
						this.func_233687_w_(!this.isSitting());
						return ActionResultType.SUCCESS;
					}
					else
					{
						player.setHeldItem(hand, getHeldItemMainhand());
						setHeldItem(Hand.MAIN_HAND, heldItem);
					}
	    		}
			}
	    	else if(isTameable() && heldItem.getItem() == Items.BONE && this.getAttackTarget() == null)
	    	{
	    		if(!player.isCreative())
	    			heldItem.shrink(1);
	    		
	    		// Taming
	    		if(getRNG().nextInt(5) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player))
	    		{
	    			this.setTamedBy(player);
	    			this.navigator.clearPath();
	    			this.setAttackTarget(null);
	    			this.func_233687_w_(true);
	    			this.getEntityWorld().setEntityState(this, (byte)7);
	    		}
	    		else
	    			this.getEntityWorld().setEntityState(this, (byte)6);
	    		
	    		return ActionResultType.SUCCESS;
	    	}
	    	
	    	return super.func_230254_b_(player, hand);
    	}
    }
    
	public AgeableEntity func_241840_a(ServerWorld arg0, AgeableEntity arg1)
	{
		if(arg1.getType() == VOEntities.WORG)
		{
			EntityWorg worg2 = (EntityWorg)arg1;
			Genetics genesA = getGenetics();
			Genetics genesB = worg2.getGenetics();
			
			Genetics genesC = Genetics.cross(genesA, genesB, getRNG());
			
			AbstractGoblinWolf offspring;
			if(genesC.gene(6) == false && genesC.gene(7) == false)
			{
				EntityWarg warg = VOEntities.WARG.create(arg0);
				warg.setGenetics(genesC);
				offspring = warg;
			}
			else
			{
				EntityWorg worg = VOEntities.WORG.create(arg0);
				worg.setGenetics(genesC);
				offspring = worg;
			}
			
			offspring.setColor(getRNG().nextBoolean() ? getColor() : worg2.getColor());
			return offspring;
		}
		return null;
	}
}
